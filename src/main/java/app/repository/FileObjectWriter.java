package app.repository;

import app.exception.ErrorCode;

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

    // 바이트 배열을 받고 해시값 생성
    private String calculateSha1(byte[] objectContent) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = messageDigest.digest(objectContent);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ErrorCode.SHA1_NOT_AVAILABLE.message());
        }
    }

    // 해시값을 받고 경로 생성
    // /Users/lhs/my-project/.javaGit/objects/a1/b2c3d4e5f6789012345678901234567890abcd
    private Path buildObjectFilePath(String objectHash) {
        Path objectsDirectoryPath = rootDirectoryPath.resolve(DOT_JAVA_GIT).resolve(OBJECTS);
        String hashPrefix = objectHash.substring(0, SHA_PREFIX_LENGTH);
        String hashSuffix = objectHash.substring(SHA_PREFIX_LENGTH);
        Path objectSubDirectoryPath = objectsDirectoryPath.resolve(hashPrefix);
        return objectSubDirectoryPath.resolve(hashSuffix);
    }

    // 해시값 앞의 2개 뜯어서 폴더 만들기
    private void createObjectDirectory(Path objectDirectoryPath) {
        try {
            Files.createDirectories(objectDirectoryPath);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.OBJECT_DIRECTORY_CREATE_FAILED.message());
        }
    }

    // 만들어진 해시값 2자리 폴더에 값 넣기
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


