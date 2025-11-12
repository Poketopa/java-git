package main.java.app.repository;

import main.java.app.exception.ErrorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class FileRefRepository implements main.java.app.repository.RefRepository {
    private static final String DOT_JGIT = ".jgit";
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
        Path headFilePath = rootDirectoryPath.resolve(DOT_JGIT).resolve(HEAD);
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
            Files.writeString(branchFilePath, commitSha, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.INDEX_FILE_WRITE_FAILED.message());
        }
    }

    private Path branchFilePath(String branchName) {
        Path refsHeadsDirectoryPath = rootDirectoryPath.resolve(DOT_JGIT).resolve(REFS).resolve(HEADS);
        return refsHeadsDirectoryPath.resolve(branchName);
    }
}



