package app.remote;

import app.exception.ErrorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Objects;




public final class FileRemoteClient {
    private static final String DOT_JAVA_GIT = ".javaGit";
    private static final String OBJECTS = "objects";
    private static final String REFS = "refs";
    private static final String HEADS = "heads";
    private static final String HEAD = "HEAD";
    private static final String REF_PREFIX = "ref: refs/heads/";
    private static final String DEFAULT_BRANCH = "master";
    private static final String TEMP_SUFFIX = ".tmp";

    private final Path remoteRoot;

    public FileRemoteClient(Path remoteRoot) {
        this.remoteRoot = Objects.requireNonNull(remoteRoot, "remoteRoot");
    }

    public void ensureInitialized() {
        try {
            Files.createDirectories(remoteRoot.resolve(DOT_JAVA_GIT).resolve(OBJECTS));
            Files.createDirectories(remoteRoot.resolve(DOT_JAVA_GIT).resolve(REFS).resolve(HEADS));
            Path head = remoteRoot.resolve(DOT_JAVA_GIT).resolve(HEAD);
            if (Files.exists(head)) {
                return;
            }
            Files.writeString(head, REF_PREFIX + DEFAULT_BRANCH + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message(), e);
        }
    }

    public String readBranchHead(String branchName) {
        Path file = remoteRoot.resolve(DOT_JAVA_GIT).resolve(REFS).resolve(HEADS).resolve(branchName);
        if (!Files.exists(file)) {
            return "";
        }
        try {
            return Files.readString(file, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message(), e);
        }
    }

    public void updateBranchHead(String branchName, String commitSha) {
        Path file = remoteRoot.resolve(DOT_JAVA_GIT).resolve(REFS).resolve(HEADS).resolve(branchName);
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = file.resolveSibling(file.getFileName().toString() + TEMP_SUFFIX);
            Files.writeString(tmp, commitSha == null ? "" : commitSha, StandardCharsets.UTF_8);
            Files.move(tmp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message(), e);
        }
    }

    public void copyAllLocalObjectsToRemote(Path localRoot) {
        Path localObjects = localRoot.resolve(DOT_JAVA_GIT).resolve(OBJECTS);
        Path remoteObjects = remoteRoot.resolve(DOT_JAVA_GIT).resolve(OBJECTS);
        copyAllFiles(localObjects, remoteObjects);
    }

    public void copyAllRemoteObjectsToLocal(Path localRoot) {
        Path localObjects = localRoot.resolve(DOT_JAVA_GIT).resolve(OBJECTS);
        Path remoteObjects = remoteRoot.resolve(DOT_JAVA_GIT).resolve(OBJECTS);
        copyAllFiles(remoteObjects, localObjects);
    }

    private void copyAllFiles(Path fromDir, Path toDir) {
        if (!Files.exists(fromDir)) {
            return;
        }
        try {
            Files.createDirectories(toDir);
            Files.walkFileTree(fromDir, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path relative = fromDir.relativize(dir);
                    Path target = toDir.resolve(relative);
                    Files.createDirectories(target);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relative = fromDir.relativize(file);
                    Path target = toDir.resolve(relative);
                    Path parent = target.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    if (!Files.exists(target)) {
                        Files.copy(file, target);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message(), e);
        }
    }
}
