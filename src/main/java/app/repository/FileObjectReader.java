package main.java.app.repository;

import main.java.app.domain.Blob;
import main.java.app.domain.Commit;
import main.java.app.domain.Tree;
import main.java.app.exception.ErrorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class FileObjectReader implements ObjectReader {
    private static final String DOT_JAVA_GIT = ".javaGit";
    private static final String OBJECTS = "objects";
    private static final int SHA_PREFIX_LENGTH = 2;
    private final Path rootDirectoryPath;

    public FileObjectReader(Path rootDirectoryPath) {
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    @Override
    public byte[] readRaw(String oid) {
        Path objectFilePath = buildObjectFilePath(oid);
        if (!Files.exists(objectFilePath)) {
            throw new IllegalArgumentException(ErrorCode.OBJECT_FILE_NOT_FOUND.message());
        }
        try {
            return Files.readAllBytes(objectFilePath);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.OBJECT_FILE_READ_FAILED.message());
        }
    }

    @Override
    public Blob readBlob(String oid) {
        return new Blob(readRaw(oid));
    }

    @Override
    public Tree readTree(String oid) {
        byte[] bytes = readRaw(oid);
        String content = new String(bytes, StandardCharsets.UTF_8);
        Map<String, String> entries = parseTreeContent(content);
        return new Tree(entries);
    }

    @Override
    public Commit readCommit(String oid) {
        byte[] bytes = readRaw(oid);
        String content = new String(bytes, StandardCharsets.UTF_8);
        ParsedCommit parsed = parseCommitContent(content);
        return new Commit(parsed.message, parsed.treeOid, parsed.parentOid, parsed.author);
    }

    private Path buildObjectFilePath(String objectHash) {
        Path objectsDirectoryPath = rootDirectoryPath.resolve(DOT_JAVA_GIT).resolve(OBJECTS);
        String hashPrefix = objectHash.substring(0, SHA_PREFIX_LENGTH);
        String hashSuffix = objectHash.substring(SHA_PREFIX_LENGTH);
        Path objectSubDirectoryPath = objectsDirectoryPath.resolve(hashPrefix);
        return objectSubDirectoryPath.resolve(hashSuffix);
    }

    private Map<String, String> parseTreeContent(String content) {
        Map<String, String> map = new LinkedHashMap<>();
        if (content == null || content.isBlank()) {
            return map;
        }
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            // "blob <sha> <path>"
            int firstSpace = line.indexOf(' ');
            if (firstSpace <= 0) {
                throw new IllegalArgumentException(ErrorCode.MALFORMED_TREE_OBJECT.message());
            }
            String kind = line.substring(0, firstSpace);
            if (!"blob".equals(kind)) {
                throw new IllegalArgumentException(ErrorCode.MALFORMED_TREE_OBJECT.message());
            }
            int secondSpace = line.indexOf(' ', firstSpace + 1);
            if (secondSpace <= firstSpace + 1) {
                throw new IllegalArgumentException(ErrorCode.MALFORMED_TREE_OBJECT.message());
            }
            String sha = line.substring(firstSpace + 1, secondSpace);
            String path = line.substring(secondSpace + 1);
            map.put(path, sha);
        }
        return map;
    }

    private static final class ParsedCommit {
        final String treeOid;
        final String parentOid;
        final String author;
        final String message;

        ParsedCommit(String treeOid, String parentOid, String author, String message) {
            this.treeOid = treeOid;
            this.parentOid = parentOid;
            this.author = author;
            this.message = message;
        }
    }

    private ParsedCommit parseCommitContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.MALFORMED_COMMIT_OBJECT.message());
        }
        String[] lines = content.split("\n");
        String treeOid = null;
        String parentOid = null;
        String author = null;
        int i = 0;
        for (; i < lines.length; i++) {
            String line = lines[i];
            if (line == null || line.isBlank()) {
                i++; // skip blank line
                break;
            }
            if (line.startsWith("tree ")) {
                treeOid = line.substring("tree ".length()).trim();
                continue;
            }
            if (line.startsWith("parent ")) {
                parentOid = line.substring("parent ".length()).trim();
                continue;
            }
            if (line.startsWith("author ")) {
                author = line.substring("author ".length()).trim();
                continue;
            }
            if (line.startsWith("date ")) {
                // createdAtMillis는 Commit 생성자에서 현재시간으로 세팅되므로 여기서는 소비만 함
                continue;
            }
        }
        StringBuilder messageBuilder = new StringBuilder();
        for (; i < lines.length; i++) {
            messageBuilder.append(lines[i]);
            if (i < lines.length - 1) {
                messageBuilder.append('\n');
            }
        }
        if (treeOid == null || author == null) {
            throw new IllegalArgumentException(ErrorCode.MALFORMED_COMMIT_OBJECT.message());
        }
        return new ParsedCommit(treeOid, parentOid, author, messageBuilder.toString());
    }
}


