package main.java.app.repository;

import main.java.app.exception.ErrorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Ref git에서 특정 커밋을 가리키는 포인터
// HEAD → refs/heads/master → a1b2c3d4...
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

    // .javaGit/HEAD 헤드 파일 경로 ㅅ애성
    // 파일 읽기
    // resole -> 경로 합치기
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

    // 커밋이 누구를 가르키는지 리턴
    // ref: refs/heads/master 인 경우 master 리턴
    private String parseBranchName(String headContent) {
        if (headContent == null || headContent.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.HEAD_REF_EMPTY.message());
        }
        if (!headContent.startsWith(REF_PREFIX)) {
            throw new IllegalArgumentException(ErrorCode.HEAD_REF_EMPTY.message());
        }
        return headContent.substring(REF_PREFIX.length());
    }

    // 헤드를 리턴함, 부모가 없을 경우 (첫 커밋일 경우) 빈 문자열 리턴
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

    // 현재 바라보고 있는 커밋을 heads/master 로 설정
    @Override
    public void updateBranchHead(String branchName, String commitSha) {
        Path branchFilePath = branchFilePath(branchName);
        try {
            Files.writeString(branchFilePath, commitSha, StandardCharsets.UTF_8);
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

    private Path branchFilePath(String branchName) {
        Path refsHeadsDirectoryPath = rootDirectoryPath.resolve(DOT_JAVA_GIT).resolve(REFS).resolve(HEADS);
        return refsHeadsDirectoryPath.resolve(branchName);
    }
}



