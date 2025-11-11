package app.repository;

import app.domain.Blob;

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
    private final Path rootDir;

    public FileObjectRepository(Path rootDir) {
        this.rootDir = Objects.requireNonNull(rootDir, "rootDir");
    }

    @Override
    public String writeObject(Blob blob) {
        byte[] file = blob.file();
        String sha = sha1Hex(file);
        Path objectsDir = rootDir.resolve(DOT_JGIT).resolve(OBJECTS);
        Path dir = objectsDir.resolve(sha.substring(0, 2));
        Path filePath = dir.resolve(sha.substring(2));
        try {
            Files.createDirectories(dir);
            if (!Files.exists(filePath)) {
                Files.write(filePath, file);
            }
        } catch (IOException ignored) {
        }
        return sha;
    }

    private static String sha1Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 not available", e);
        }
    }
}


