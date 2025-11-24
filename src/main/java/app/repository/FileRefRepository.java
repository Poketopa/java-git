package app.repository;

import app.exception.ErrorCode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public final class FileRefRepository implements RefRepository {
    private static final String DOT_JAVA_GIT = ".javaGit";
    private static final String REFS = "refs";
    private static final String HEADS = "heads";
    private static final String HEAD = "HEAD";
    private static final String REF_PREFIX = "ref: refs/heads/";
    private final Path rootDirectoryPath;

    public FileRefRepository(Path rootDirectoryPath) {
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }


    @Override
    public String readCurrentBranch() {
        Path headFilePath = rootDirectoryPath.resolve(DOT_JAVA_GIT).resolve(HEAD);
        try {
            String content = Files.readString(headFilePath, StandardCharsets.UTF_8).trim();
            return parseBranchName(content);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.HEAD_REF_NULL.message());
        }
    }


    private String parseBranchName(String headContent) {
        if (headContent == null || headContent.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.HEAD_REF_EMPTY.message());
        }
        if (!headContent.startsWith(REF_PREFIX)) {
            throw new IllegalArgumentException(ErrorCode.HEAD_REF_EMPTY.message());
        }
        return headContent.substring(REF_PREFIX.length());
    }


    @Override
    public String readBranchHead(String branchName) {
        Path branchFilePath = branchFilePath(branchName);
        if (!Files.exists(branchFilePath)) {
            return "";
        }
        try {
            return Files.readString(branchFilePath, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message());
        }
    }


    @Override
    public void updateBranchHead(String branchName, String commitSha) {
        Path branchFilePath = branchFilePath(branchName);
        try {
            Path parent = branchFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = branchFilePath.resolveSibling(branchFilePath.getFileName().toString() + ".tmp");
            Files.writeString(tmp, commitSha == null ? "" : commitSha, StandardCharsets.UTF_8);
            Files.move(tmp, branchFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.INDEX_FILE_WRITE_FAILED.message());
        }
    }

    @Override
    public List<String> listBranches() {
        Path refsHeadsDirectoryPath = rootDirectoryPath.resolve(DOT_JAVA_GIT).resolve(REFS).resolve(HEADS);
        if (!Files.exists(refsHeadsDirectoryPath)) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        try (var stream = Files.list(refsHeadsDirectoryPath)) {
            stream.filter(Files::isRegularFile).forEach(p -> names.add(p.getFileName().toString()));
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message());
        }
        names.sort(String::compareTo);
        return List.copyOf(names);
    }

    @Override
    public void createBranch(String branchName, String baseCommitSha) {
        Path file = branchFilePath(branchName);
        if (Files.exists(file)) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 브랜치입니다: " + branchName);
        }
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, baseCommitSha == null ? "" : baseCommitSha, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message());
        }
    }

    @Override
    public void updateCurrentBranch(String branchName) {
        Path headFilePath = rootDirectoryPath.resolve(DOT_JAVA_GIT).resolve(HEAD);
        try {
            Files.writeString(headFilePath, REF_PREFIX + branchName + "\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message());
        }
    }

    private Path branchFilePath(String branchName) {
        Path refsHeadsDirectoryPath = rootDirectoryPath.resolve(DOT_JAVA_GIT).resolve(REFS).resolve(HEADS);
        return refsHeadsDirectoryPath.resolve(branchName);
    }
}
