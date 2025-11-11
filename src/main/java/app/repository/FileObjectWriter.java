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
    private static final String DOT_JGIT = ".jgit";
    private static final String OBJECTS = "objects";
    private static final int SHA_PREFIX_LENGTH = 2;
    private final Path rootDirectoryPath;

    public FileObjectWriter(Path rootDirectoryPath) {
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    @Override
    public String write(byte[] bytes) {
        String shaHashValue = calculateSha1(bytes);
        Path objectFilePath = buildObjectFilePath(shaHashValue);
        Path objectDirectoryPath = objectFilePath.getParent();
        createObjectDirectory(objectDirectoryPath);
        writeObjectFile(objectFilePath, bytes);
        return shaHashValue;
    }

    private String calculateSha1(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = messageDigest.digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ErrorCode.SHA1_NOT_AVAILABLE.message());
        }
    }

    private Path buildObjectFilePath(String shaHashValue) {
        Path objectsDirectoryPath = rootDirectoryPath.resolve(DOT_JGIT).resolve(OBJECTS);
        String shaPrefixValue = shaHashValue.substring(0, SHA_PREFIX_LENGTH);
        String shaSuffixValue = shaHashValue.substring(SHA_PREFIX_LENGTH);
        Path objectSubDirectoryPath = objectsDirectoryPath.resolve(shaPrefixValue);
        return objectSubDirectoryPath.resolve(shaSuffixValue);
    }

    private void createObjectDirectory(Path objectDirectoryPath) {
        try {
            Files.createDirectories(objectDirectoryPath);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.OBJECT_DIRECTORY_CREATE_FAILED.message());
        }
    }

    private void writeObjectFile(Path objectFilePath, byte[] bytes) {
        if (Files.exists(objectFilePath)) {
            return;
        }
        try {
            Files.write(objectFilePath, bytes);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.OBJECT_FILE_WRITE_FAILED.message());
        }
    }
}


