package app.remote.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import app.exception.ErrorCode;
import app.repository.FileObjectReader;
import app.repository.FileObjectWriter;
import app.repository.FileRefRepository;
import app.repository.ObjectReader;
import app.repository.ObjectWriter;
import app.repository.RefRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

// Minimal HTTP Remote Server
// - GET  /refs                       → plain text: "HEAD <branch>\nref <branch> <sha>\n..."
// - GET  /objects                    → plain text: "<oid>\n" list
// - HEAD /objects/{oid}              → 200 if exists, 404 otherwise
// - GET  /objects/{oid}              → raw bytes
// - POST /objects/{oid}              → raw bytes (idempotent)
// - POST /update-ref                 → plain body: "branch <name>\nold <oldsha>\nnew <newsha>\n"
public final class HttpRemoteServer {
    private static final String DOT_JAVA_GIT = ".javaGit";
    private static final String OBJECTS = "objects";
    private static final String REFS = "refs";
    private static final String HEADS = "heads";
    private static final String HEAD = "HEAD";
    private static final String CONTENT_TYPE_TEXT = "text/plain; charset=utf-8";
    private static final String CONTENT_TYPE_OCTET = "application/octet-stream";

    private final Path root;
    private HttpServer server;

    public HttpRemoteServer(Path root) {
        this.root = Objects.requireNonNull(root, "root");
    }

    public void start(int port) {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message(), e);
        }
        this.server.createContext("/refs", new RefsHandler(root));
        this.server.createContext("/objects", new ObjectsHandler(root));
        this.server.createContext("/update-ref", new UpdateRefHandler(root));
        this.server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        this.server.start();
    }

    public void stop() {
        if (server == null) {
            return;
        }
        server.stop(0);
    }

    private static final class RefsHandler implements HttpHandler {
        private final RefRepository refRepository;
        public RefsHandler(Path root) {
            this.refRepository = new FileRefRepository(root);
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String head = refRepository.readCurrentBranch();
            List<String> branches = refRepository.listBranches();
            StringBuilder sb = new StringBuilder();
            sb.append("HEAD ").append(head == null ? "" : head).append('\n');
            for (String b : branches) {
                String sha = refRepository.readBranchHead(b);
                sb.append("ref ").append(b).append(' ').append(sha == null ? "" : sha).append('\n');
            }
            byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
            Headers hdr = exchange.getResponseHeaders();
            hdr.set("Content-Type", CONTENT_TYPE_TEXT);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }
    }

    private static final class ObjectsHandler implements HttpHandler {
        private final Path root;
        private final ObjectReader objectReader;
        public ObjectsHandler(Path root) {
            this.root = root;
            this.objectReader = new FileObjectReader(root);
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length == 2) {
                if (!"GET".equals(method)) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                handleList(exchange);
                return;
            }
            String oid = parts[2];
            if ("HEAD".equals(method)) {
                handleHead(exchange, oid);
                return;
            }
            if ("GET".equals(method)) {
                handleGet(exchange, oid);
                return;
            }
            if ("POST".equals(method)) {
                handlePost(exchange, oid);
                return;
            }
            exchange.sendResponseHeaders(405, -1);
        }

        private void handleList(HttpExchange exchange) throws IOException {
            Path objs = root.resolve(DOT_JAVA_GIT).resolve(OBJECTS);
            StringBuilder sb = new StringBuilder();
            if (Files.exists(objs)) {
                try (var dirs = Files.list(objs)) {
                    dirs.filter(Files::isDirectory).forEach(dir -> {
                        try (var files = Files.list(dir)) {
                            files.filter(Files::isRegularFile).forEach(f -> {
                                String prefix = dir.getFileName().toString();
                                String suffix = f.getFileName().toString();
                                sb.append(prefix).append(suffix).append('\n');
                            });
                        } catch (IOException ignored) {
                        }
                    });
                }
            }
            byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
            Headers hdr = exchange.getResponseHeaders();
            hdr.set("Content-Type", CONTENT_TYPE_TEXT);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }

        private void handleHead(HttpExchange exchange, String oid) throws IOException {
            Path file = buildObjectPath(oid);
            if (!Files.exists(file)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            exchange.sendResponseHeaders(200, -1);
        }

        private void handleGet(HttpExchange exchange, String oid) throws IOException {
            try {
                byte[] bytes = objectReader.readRaw(oid);
                Headers hdr = exchange.getResponseHeaders();
                hdr.set("Content-Type", CONTENT_TYPE_OCTET);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (IllegalArgumentException e) {
                exchange.sendResponseHeaders(404, -1);
            }
        }

        private void handlePost(HttpExchange exchange, String oid) throws IOException {
            byte[] bytes = exchange.getRequestBody().readAllBytes();
            // 무결성 검증: 바이트 SHA-1이 경로 OID와 일치해야 함
            if (!oidEqualsContentHash(oid, bytes)) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            Path file = buildObjectPath(oid);
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(file)) {
                Files.write(file, bytes);
            }
            exchange.sendResponseHeaders(200, -1);
        }

        private Path buildObjectPath(String oid) {
            String prefix = oid.substring(0, 2);
            String suffix = oid.substring(2);
            return root.resolve(DOT_JAVA_GIT).resolve(OBJECTS).resolve(prefix).resolve(suffix);
        }

        private boolean oidEqualsContentHash(String oid, byte[] bytes) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                byte[] out = digest.digest(bytes);
                String hex = HexFormat.of().formatHex(out);
                return oid.equalsIgnoreCase(hex);
            } catch (NoSuchAlgorithmException e) {
                return false;
            }
        }
    }

    private static final class UpdateRefHandler implements HttpHandler {
        private final RefRepository refRepository;
        private final ObjectReader objectReader;
        public UpdateRefHandler(Path root) {
            this.refRepository = new FileRefRepository(root);
            this.objectReader = new FileObjectReader(root);
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String branch = readField(body, "branch");
            String oldSha = readField(body, "old");
            String newSha = readField(body, "new");
            if (branch == null || newSha == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            String current = refRepository.readBranchHead(branch);
            if (!Objects.equals(current, oldSha)) {
                exchange.sendResponseHeaders(409, -1);
                return;
            }
            // 검증: 새 커밋이 존재해야 함
            try {
                if (newSha != null && !newSha.isBlank()) {
                    objectReader.readRaw(newSha);
                }
            } catch (IllegalArgumentException e) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            refRepository.updateBranchHead(branch, newSha);
            exchange.sendResponseHeaders(200, -1);
        }

        private String readField(String body, String key) {
            String prefix = key + " ";
            for (String line : body.split("\n")) {
                if (!line.startsWith(prefix)) {
                    continue;
                }
                return line.substring(prefix.length()).trim();
            }
            return null;
        }
    }
}


