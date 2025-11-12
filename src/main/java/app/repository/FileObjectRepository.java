package main.java.app.repository;

import main.java.app.domain.Blob;
import main.java.app.exception.ErrorCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class FileObjectRepository implements ObjectRepository {
    private static final String DOT_JGIT = ".jgit";
    private static final String OBJECTS = "objects";
    private static final int SHA_PREFIX_LENGTH = 2;
    private final Path rootDirectoryPath;

    public FileObjectRepository(Path rootDirectoryPath) {
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    @Override
    public String writeObject(Blob blob) {
        byte[] blobContent = blob.file();
        String objectHash = calculateSha1(blobContent);
        Path objectFilePath = buildObjectFilePath(objectHash);
        Path objectDirectoryPath = objectFilePath.getParent();
        createObjectDirectory(objectDirectoryPath);
        writeObjectFile(objectFilePath, blobContent);
        return objectHash;
    }

    private String calculateSha1(byte[] blobContent) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = messageDigest.digest(blobContent);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ErrorCode.SHA1_NOT_AVAILABLE.message());
        }
    }

    private Path buildObjectFilePath(String objectHash) {
        Path objectsDirectoryPath = rootDirectoryPath.resolve(DOT_JGIT).resolve(OBJECTS);
        String hashPrefix = extractShaPrefix(objectHash);
        String hashSuffix = extractShaSuffix(objectHash);
        Path objectSubDirectoryPath = objectsDirectoryPath.resolve(hashPrefix);
        return objectSubDirectoryPath.resolve(hashSuffix);
    }

    private String extractShaPrefix(String objectHash) {
        return objectHash.substring(0, SHA_PREFIX_LENGTH);
    }

    private String extractShaSuffix(String objectHash) {
        return objectHash.substring(SHA_PREFIX_LENGTH);
    }

    private void createObjectDirectory(Path objectDirectoryPath) {
        try {
            Files.createDirectories(objectDirectoryPath);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.OBJECT_DIRECTORY_CREATE_FAILED.message());
        }
    }

    private void writeObjectFile(Path objectFilePath, byte[] blobContent) {
        if (Files.exists(objectFilePath)) {
            return;
        }
        try {
            Files.write(objectFilePath, blobContent);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.OBJECT_FILE_WRITE_FAILED.message());
        }
    }
}

