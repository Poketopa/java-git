package main.java.app.service.remote.fs;

import main.java.app.exception.ErrorCode;
import main.java.app.remote.FileRemoteClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

// 로컬 디렉터리 remote에서 현재 워킹 디렉터리 외의 타겟 경로로 clone
// - 매우 단순: 원격 .javaGit/objects와 refs/heads, HEAD 파일만 복제
// - 워킹 트리 파일 체크아웃은 하지 않음(후속 단계)
public final class CloneService {
    public enum CloneResult {
        SUCCESS,
        REMOTE_NOT_FOUND,
        TARGET_EXISTS_NOT_EMPTY,
        REMOTE_NO_COMMITS
    }

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

        // 원격 객체 복제
        FileRemoteClient remote = new FileRemoteClient(remoteRoot);
        remote.ensureInitialized();
        // HEAD가 없다면 비정상 원격
        if (!Files.exists(remoteRoot.resolve(DOT_JAVA_GIT).resolve(HEAD))) {
            return CloneResult.REMOTE_NO_COMMITS;
        }

        // 타겟 .javaGit 기본 디렉토리 준비
        Path targetJavaGit = targetRoot.resolve(DOT_JAVA_GIT);
        Path targetRefsHeads = targetJavaGit.resolve(REFS).resolve(HEADS);
        try {
            Files.createDirectories(targetJavaGit);
            Files.createDirectories(targetRefsHeads);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message(), e);
        }

        // objects 복사
        remote.copyAllRemoteObjectsToLocal(targetRoot);
        // refs/heads 전체 복사
        copyDirectory(remoteRoot.resolve(DOT_JAVA_GIT).resolve(REFS).resolve(HEADS), targetRefsHeads);
        // HEAD 복사
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
}


