package app.repository;

import app.domain.Blob;
import app.domain.Commit;
import app.domain.Tree;
import app.exception.ErrorCode;

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
    public byte[] readRaw(String objectId) {
        Path objectFilePath = buildObjectFilePath(objectId);
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
    public Blob readBlob(String objectId) {
        return new Blob(readRaw(objectId));
    }

    @Override
    public Tree readTree(String objectId) {
        byte[] bytes = readRaw(objectId);
        String content = new String(bytes, StandardCharsets.UTF_8);
        Map<String, String> entries = parseTreeContent(content);
        return new Tree(entries);
    }

    @Override
    public Commit readCommit(String objectId) {
        byte[] bytes = readRaw(objectId);
        String content = new String(bytes, StandardCharsets.UTF_8);
        ParsedCommit parsed = parseCommitContent(content);
        return new Commit(parsed.message, parsed.treeSha, parsed.parentHash, parsed.author);
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
        final String treeSha;
        final String parentHash;
        final String author;
        final String message;

        ParsedCommit(String treeSha, String parentHash, String author, String message) {
            this.treeSha = treeSha;
            this.parentHash = parentHash;
            this.author = author;
            this.message = message;
        }
    }

    private ParsedCommit parseCommitContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.MALFORMED_COMMIT_OBJECT.message());
        }
        String[] lines = content.split("\n");
        String treeSha = null;
        String parentHash = null;
        String author = null;
        int i = 0;
        for (; i < lines.length; i++) {
            String line = lines[i];
            if (line == null || line.isBlank()) {
                i++; 
                break;
            }
            if (line.startsWith("tree ")) {
                treeSha = line.substring("tree ".length()).trim();
                continue;
            }
            if (line.startsWith("parent ")) {
                parentHash = line.substring("parent ".length()).trim();
                continue;
            }
            if (line.startsWith("author ")) {
                author = line.substring("author ".length()).trim();
                continue;
            }
            if (line.startsWith("date ")) {
                
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
        if (treeSha == null || author == null) {
            throw new IllegalArgumentException(ErrorCode.MALFORMED_COMMIT_OBJECT.message());
        }
        return new ParsedCommit(treeSha, parentHash, author, messageBuilder.toString());
    }
}


