package app.service;

import app.domain.Commit;
import app.domain.Tree;
import app.exception.ErrorCode;
import app.repository.IndexRepository;
import app.repository.ObjectReader;
import app.repository.RefRepository;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HexFormat;

// 작업 트리(Working) ↔ Index ↔ HEAD(Tree) 3-way 비교
// - 스테이징된 추가/수정/삭제
// - 아직 스테이징되지 않은 수정/삭제
// - Untracked 파일
public final class StatusService {
    public record StatusResult(
            Map<String, String> stagedAdded,
            Map<String, String> stagedModified,
            Set<String> stagedDeleted,
            Set<String> modifiedNotStaged,
            Set<String> deletedNotStaged,
            Set<String> untracked
    ) { }

    private final IndexRepository indexRepository;
    private final RefRepository refRepository;
    private final ObjectReader objectReader;
    private final Path rootDirectoryPath;

    public StatusService(IndexRepository indexRepository, RefRepository refRepository, ObjectReader objectReader, Path rootDirectoryPath) {
        this.indexRepository = Objects.requireNonNull(indexRepository, "indexRepository");
        this.refRepository = Objects.requireNonNull(refRepository, "refRepository");
        this.objectReader = Objects.requireNonNull(objectReader, "objectReader");
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    public StatusResult status() {
        // 1) 현재 스냅샷 수집
        Map<String, String> working = scanWorkingTree();
        Map<String, String> index = indexRepository.read().stagedFiles();
        Map<String, String> headTree = readHeadTreeSnapshot();

        // 2) Index 기준으로 Staged Added/Modified/Deleted 계산
        Map<String, String> stagedAdded = new LinkedHashMap<>();
        Map<String, String> stagedModified = new LinkedHashMap<>();
        Set<String> stagedDeleted = new LinkedHashSet<>();
        for (Map.Entry<String, String> e : index.entrySet()) {
            String path = e.getKey();
            String sha = e.getValue();
            if (!headTree.containsKey(path)) {
                stagedAdded.put(path, sha);
                continue;
            }
            if (!Objects.equals(sha, headTree.get(path))) {
                stagedModified.put(path, sha);
            }
        }
        for (String path : headTree.keySet()) {
            if (!index.containsKey(path)) {
                stagedDeleted.add(path);
            }
        }

        // 3) Working ↔ Index 비교로 Not Staged(수정/삭제) 계산
        Set<String> modifiedNotStaged = new LinkedHashSet<>();
        Set<String> deletedNotStaged = new LinkedHashSet<>();
        for (Map.Entry<String, String> e : index.entrySet()) {
            String path = e.getKey();
            String indexSha = e.getValue();
            String workingSha = working.get(path);
            if (workingSha == null) {
                deletedNotStaged.add(path);
                continue;
            }
            if (!Objects.equals(indexSha, workingSha)) {
                modifiedNotStaged.add(path);
            }
        }

        // 4) Untracked 파일 계산 (Working에만 존재)
        Set<String> untracked = new LinkedHashSet<>();
        for (String path : working.keySet()) {
            if (!index.containsKey(path)) {
                untracked.add(path);
            }
        }

        return new StatusResult(
                stagedAdded,
                stagedModified,
                stagedDeleted,
                modifiedNotStaged,
                deletedNotStaged,
                untracked
        );
    }

    // HEAD 커밋의 Tree 스냅샷을 Map 형태로 반환
    // - 초기 상태(부모 없음)면 빈 Map
    private Map<String, String> readHeadTreeSnapshot() {
        String branch = refRepository.readCurrentBranch();
        String headCommitHash = refRepository.readBranchHead(branch);
        if (headCommitHash == null || headCommitHash.isBlank()) {
            return Map.of();
        }
        Commit commit = objectReader.readCommit(headCommitHash);
        Tree tree = objectReader.readTree(commit.treeOid());
        return tree.entries();
    }

    // 워킹 트리를 순회하며 파일별 SHA-1을 계산
    // - .javaGit 디렉토리는 제외
    private Map<String, String> scanWorkingTree() {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            Files.walkFileTree(rootDirectoryPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.getFileName() != null && ".javaGit".equals(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!attrs.isRegularFile()) {
                        return FileVisitResult.CONTINUE;
                    }
                    Path relative = rootDirectoryPath.relativize(file);
                    String relPath = relative.toString().replace('\\', '/');
                    byte[] bytes;
                    try {
                        bytes = Files.readAllBytes(file);
                    } catch (IOException e) {
                        throw new IllegalArgumentException(ErrorCode.FILE_READ_FAILED.message());
                    }
                    String sha = sha1(bytes);
                    map.put(relPath, sha);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_IO_ERROR.message());
        }
        return map;
    }

    // SHA-1 해시 계산
    private String sha1(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] out = digest.digest(content);
            return HexFormat.of().formatHex(out);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(ErrorCode.SHA1_NOT_AVAILABLE.message());
        }
    }
}


