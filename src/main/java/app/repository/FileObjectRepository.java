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
        byte[] fileContent = blob.file();
        String shaHashValue = calculateSha1(fileContent);
        Path objectFilePath = buildObjectFilePath(shaHashValue);
        Path objectDirectoryPath = objectFilePath.getParent();
        createObjectDirectory(objectDirectoryPath);
        writeObjectFile(objectFilePath, fileContent);
        return shaHashValue;
    }

    private String calculateSha1(byte[] fileContent) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = messageDigest.digest(fileContent);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ErrorCode.SHA1_NOT_AVAILABLE.message());
        }
    }

    private Path buildObjectFilePath(String shaHashValue) {
        Path objectsDirectoryPath = rootDirectoryPath.resolve(DOT_JGIT).resolve(OBJECTS);
        String shaPrefixValue = extractShaPrefix(shaHashValue);
        String shaSuffixValue = extractShaSuffix(shaHashValue);
        Path objectSubDirectoryPath = objectsDirectoryPath.resolve(shaPrefixValue);
        return objectSubDirectoryPath.resolve(shaSuffixValue);
    }

    private String extractShaPrefix(String shaHashValue) {
        return shaHashValue.substring(0, SHA_PREFIX_LENGTH);
    }

    private String extractShaSuffix(String shaHashValue) {
        return shaHashValue.substring(SHA_PREFIX_LENGTH);
    }

    private void createObjectDirectory(Path objectDirectoryPath) {
        try {
            Files.createDirectories(objectDirectoryPath);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.OBJECT_DIRECTORY_CREATE_FAILED.message());
        }
    }

    private void writeObjectFile(Path objectFilePath, byte[] fileContent) {
        if (Files.exists(objectFilePath)) {
            return;
        }
        try {
            Files.write(objectFilePath, fileContent);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.OBJECT_FILE_WRITE_FAILED.message());
        }
    }
}


