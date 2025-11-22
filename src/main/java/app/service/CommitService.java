package main.java.app.service;

import main.java.app.domain.Commit;
import main.java.app.domain.Index;
import main.java.app.domain.Tree;
import main.java.app.exception.ErrorCode;
import main.java.app.repository.IndexRepository;
import main.java.app.repository.ObjectWriter;
import main.java.app.repository.RefRepository;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

// 커밋 생성 플로우
// - Index 스냅샷으로 Tree 작성 → Tree 저장
// - HEAD가 가리키는 부모 커밋 조회
// - Commit 작성 및 저장 → HEAD 업데이트
// - 커밋 완료 후 Index 비우기
public final class CommitService {
    private final IndexRepository indexRepository;
    private final ObjectWriter objectWriter;
    private final RefRepository refRepository;
    private final Path rootDirectoryPath;

    public CommitService(IndexRepository indexRepository, ObjectWriter objectWriter, RefRepository refRepository, Path rootDirectoryPath) {
        this.indexRepository = Objects.requireNonNull(indexRepository, "indexRepository");
        this.objectWriter = Objects.requireNonNull(objectWriter, "objectWriter");
        this.refRepository = Objects.requireNonNull(refRepository, "refRepository");
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    public void commit(String message, String author) {
        // 1) 입력 검증
        validate(message, author);
        // 2) 스테이징된 파일 존재 확인
        Index index = indexRepository.read();
        ensureHasStagedFiles(index);
        // 3) Tree 작성 및 저장
        Tree tree = new Tree(index.stagedFiles());
        String treeHash = writeTree(tree);
        // 4) 부모 커밋 조회
        String parentCommitHash = readCurrentHeadCommit();
        // 5) Commit 작성 및 저장
        String commitHash = writeCommit(new Commit(message, treeHash, emptyToNull(parentCommitHash), author));
        // 6) HEAD 업데이트
        updateHead(commitHash);
        // 7) Index 초기화
        // 7) Index 초기화 (제거: Index는 커밋 후에도 유지되어야 함)
        // clearIndex();
    }

    // 커밋 메시지/작성자 필수값 검증
    private void validate(String message, String author) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.COMMIT_MESSAGE_EMPTY.message());
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.COMMIT_AUTHOR_EMPTY.message());
        }
    }

    // 스테이징된 파일이 없으면 커밋 불가
    private void ensureHasStagedFiles(Index index) {
        Map<String, String> stagedFiles = index.stagedFiles();
        if (stagedFiles == null || stagedFiles.isEmpty()) {
            throw new IllegalArgumentException("Nothing to commit");
        }
    }

    private String writeTree(Tree tree) {
        byte[] treeContent = buildTreeContent(tree).getBytes(StandardCharsets.UTF_8);
        return objectWriter.write(treeContent);
    }

    // Tree 직렬화 포맷
    // - "blob <sha> <path>\n" 라인 반복
    private String buildTreeContent(Tree tree) {
        StringBuilder contentBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : tree.entries().entrySet()) {
            appendTreeEntry(contentBuilder, entry);
        }
        return contentBuilder.toString();
    }

    private void appendTreeEntry(StringBuilder contentBuilder, Map.Entry<String, String> entry) {
        contentBuilder.append("blob ").append(entry.getValue()).append(' ').append(entry.getKey()).append('\n');
    }

    // 현재 브랜치의 HEAD 커밋 해시 조회 (없으면 빈 문자열)
    private String readCurrentHeadCommit() {
        String branch = refRepository.readCurrentBranch();
        return refRepository.readBranchHead(branch);
    }

    private String writeCommit(Commit commit) {
        byte[] commitContent = buildCommitContent(commit).getBytes(StandardCharsets.UTF_8);
        return objectWriter.write(commitContent);
    }

    // Commit 직렬화 포맷
    // - 헤더: tree/parent/author/date
    // - 빈 줄 후 메시지
    private String buildCommitContent(Commit commit) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("tree ").append(commit.treeOid()).append('\n');
        if (commit.parentOid() != null && !commit.parentOid().isBlank()) {
            contentBuilder.append("parent ").append(commit.parentOid()).append('\n');
        }
        contentBuilder.append("author ").append(commit.author()).append('\n');
        contentBuilder.append("date ").append(commit.createdAtMillis()).append('\n');
        contentBuilder.append('\n');
        contentBuilder.append(commit.message()).append('\n');
        return contentBuilder.toString();
    }

    private void updateHead(String commitHash) {
        String branch = refRepository.readCurrentBranch();
        refRepository.updateBranchHead(branch, commitHash);
    }

    private void clearIndex() {
        indexRepository.write(new Index(Map.of()));
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }
        if (value.isBlank()) {
            return null;
        }
        return value;
    }
}

