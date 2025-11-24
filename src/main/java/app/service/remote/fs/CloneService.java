package app.service.remote.fs;

import app.exception.ErrorCode;
import app.remote.FileRemoteClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;


public final class CloneService {
    private static final String DOT_JAVA_GIT = ".javaGit";
    private static final String REFS = "refs";
    private static final String HEADS = "heads";
    private static final String HEAD = "HEAD";

    public CloneResult clone(Path remoteRoot, Path targetRoot) {
        Objects.requireNonNull(remoteRoot, "remoteRoot");
        Objects.requireNonNull(targetRoot, "targetRoot");

        if (!Files.exists(remoteRoot)) {
            return CloneResult.REMOTE_NOT_FOUND;
        }
        if (Files.exists(targetRoot) && !isDirectoryEmpty(targetRoot)) {
            return CloneResult.TARGET_EXISTS_NOT_EMPTY;
        }
        try {
            Files.createDirectories(targetRoot);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message(), e);
        }

        FileRemoteClient remote = new FileRemoteClient(remoteRoot);
        remote.ensureInitialized();

        if (!Files.exists(remoteRoot.resolve(DOT_JAVA_GIT).resolve(HEAD))) {
            return CloneResult.REMOTE_NO_COMMITS;
        }

        Path targetJavaGit = targetRoot.resolve(DOT_JAVA_GIT);
        Path targetRefsHeads = targetJavaGit.resolve(REFS).resolve(HEADS);
        try {
            Files.createDirectories(targetJavaGit);
            Files.createDirectories(targetRefsHeads);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message(), e);
        }

        remote.copyAllRemoteObjectsToLocal(targetRoot);

        copyDirectory(remoteRoot.resolve(DOT_JAVA_GIT).resolve(REFS).resolve(HEADS), targetRefsHeads);

        copyHead(remoteRoot.resolve(DOT_JAVA_GIT).resolve(HEAD), targetJavaGit.resolve(HEAD));

        return CloneResult.SUCCESS;
    }

    private boolean isDirectoryEmpty(Path dir) {
        try (var stream = Files.list(dir)) {
            return stream.findAny().isEmpty();
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message(), e);
        }
    }

    private void copyDirectory(Path from, Path to) {
        if (!Files.exists(from)) {
            return;
        }
        try {
            Files.createDirectories(to);
            try (var walk = Files.walk(from)) {
                walk.forEach(path -> {
                    try {
                        Path relative = from.relativize(path);
                        Path dest = to.resolve(relative);
                        if (Files.isDirectory(path)) {
                            Files.createDirectories(dest);
                            return;
                        }
                        Path parent = dest.getParent();
                        if (parent != null) {
                            Files.createDirectories(parent);
                        }
                        if (!Files.exists(dest)) {
                            Files.copy(path, dest);
                        }
                    } catch (IOException ioe) {
                        throw new RuntimeException(ioe);
                    }
                });
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message(), e);
        }
    }

    private void copyHead(Path from, Path to) {
        try {
            String content = Files.readString(from, StandardCharsets.UTF_8);
            Files.writeString(to, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message(), e);
        }
    }

    public enum CloneResult {
        SUCCESS,
        REMOTE_NOT_FOUND,
        TARGET_EXISTS_NOT_EMPTY,
        REMOTE_NO_COMMITS
    }
}
