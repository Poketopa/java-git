package app.remote.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;



public final class HttpRemoteClient {
    private static final String CONTENT_TYPE_TEXT = "text/plain; charset=utf-8";
    private static final String CONTENT_TYPE_OCTET = "application/octet-stream";

    private final HttpClient client;
    private final URI baseUri;

    public HttpRemoteClient(String baseUrl) {
        this.baseUri = URI.create(Objects.requireNonNull(baseUrl, "baseUrl"));
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public Map<String, String> listRefs() {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/refs"))
                .header("Accept", CONTENT_TYPE_TEXT)
                .GET()
                .build();
        try {
            HttpResponse<byte[]> res = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (res.statusCode() != 200) {
                return Map.of();
            }
            String text = new String(res.body(), StandardCharsets.UTF_8);
            return parseRefs(text);
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException("[ERROR] 원격 refs 조회 실패", e);
        }
    }

    public java.util.Set<String> listObjects() {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/objects"))
                .header("Accept", CONTENT_TYPE_TEXT)
                .GET()
                .build();
        try {
            HttpResponse<byte[]> res = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (res.statusCode() != 200) {
                return java.util.Set.of();
            }
            String text = new String(res.body(), StandardCharsets.UTF_8);
            java.util.Set<String> set = new java.util.HashSet<>();
            for (String line : text.split("\n")) {
                String oid = line.trim();
                if (!oid.isEmpty()) {
                    set.add(oid);
                }
            }
            return java.util.Collections.unmodifiableSet(set);
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException("[ERROR] 원격 objects 목록 조회 실패", e);
        }
    }

    public boolean hasObject(String oid) {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/objects/" + oid))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<Void> res = client.send(request, HttpResponse.BodyHandlers.discarding());
            return res.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException("[ERROR] 원격 객체 존재 확인 실패", e);
        }
    }

    public byte[] getObject(String oid) {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/objects/" + oid))
                .GET()
                .build();
        try {
            HttpResponse<byte[]> res = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (res.statusCode() != 200) {
                throw new IllegalArgumentException("[ERROR] 원격 객체 다운로드 실패");
            }
            return res.body();
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException("[ERROR] 원격 객체 다운로드 실패", e);
        }
    }

    public void putObject(String oid, byte[] bytes) {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/objects/" + oid))
                .header("Content-Type", CONTENT_TYPE_OCTET)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();
        try {
            HttpResponse<Void> res = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (res.statusCode() != 200) {
                throw new IllegalArgumentException("[ERROR] 원격 객체 업로드 실패");
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException("[ERROR] 원격 객체 업로드 실패", e);
        }
    }

    public void updateRef(String branch, String oldSha, String newSha) {
        String body = "branch " + branch + "\nold " + (oldSha == null ? "" : oldSha) + "\nnew " + newSha + "\n";
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/update-ref"))
                .header("Content-Type", CONTENT_TYPE_TEXT)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<Void> res = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (res.statusCode() == 200) {
                return;
            }
            if (res.statusCode() == 409) {
                throw new IllegalArgumentException("[ERROR] 원격 ref 업데이트 거부(동시성/비FF)");
            }
            throw new IllegalArgumentException("[ERROR] 원격 ref 업데이트 실패");
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException("[ERROR] 원격 ref 업데이트 실패", e);
        }
    }

    private Map<String, String> parseRefs(String text) {
        Map<String, String> map = new LinkedHashMap<>();
        if (text == null || text.isBlank()) {
            return map;
        }
        for (String line : text.split("\n")) {
            if (line.startsWith("HEAD ")) {
                map.put("HEAD", line.substring("HEAD ".length()).trim());
                continue;
            }
            if (!line.startsWith("ref ")) {
                continue;
            }
            String rest = line.substring("ref ".length());
            int space = rest.indexOf(' ');
            if (space <= 0) {
                continue;
            }
            String branch = rest.substring(0, space);
            String sha = rest.substring(space + 1);
            map.put(branch, sha);
        }
        return map;
    }
}
