package main.java.app.repository;

import main.java.app.exception.ErrorCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class FileObjectWriter implements ObjectWriter {
    private static final String DOT_JAVA_GIT = ".javaGit";
    private static final String OBJECTS = "objects";
    private static final int SHA_PREFIX_LENGTH = 2;
    private final Path rootDirectoryPath;

    public FileObjectWriter(Path rootDirectoryPath) {
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    @Override
    public String write(byte[] objectContent) {
        String objectHash = calculateSha1(objectContent);
        Path objectFilePath = buildObjectFilePath(objectHash);
        Path objectDirectoryPath = objectFilePath.getParent();
        createObjectDirectory(objectDirectoryPath);
        writeObjectFile(objectFilePath, objectContent);
        return objectHash;
    }

    private String calculateSha1(byte[] objectContent) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = messageDigest.digest(objectContent);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ErrorCode.SHA1_NOT_AVAILABLE.message());
        }
    }

    private Path buildObjectFilePath(String objectHash) {
        Path objectsDirectoryPath = rootDirectoryPath.resolve(DOT_JAVA_GIT).resolve(OBJECTS);
        String hashPrefix = objectHash.substring(0, SHA_PREFIX_LENGTH);
        String hashSuffix = objectHash.substring(SHA_PREFIX_LENGTH);
        Path objectSubDirectoryPath = objectsDirectoryPath.resolve(hashPrefix);
        return objectSubDirectoryPath.resolve(hashSuffix);
    }

    private void createObjectDirectory(Path objectDirectoryPath) {
        try {
            Files.createDirectories(objectDirectoryPath);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.OBJECT_DIRECTORY_CREATE_FAILED.message());
        }
    }

    private void writeObjectFile(Path objectFilePath, byte[] objectContent) {
        if (Files.exists(objectFilePath)) {
            return;
        }
        try {
            Files.write(objectFilePath, objectContent);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.OBJECT_FILE_WRITE_FAILED.message());
        }
    }
}


