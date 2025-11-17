package main.java.app.service;

import main.java.app.repository.ObjectReader;
import main.java.app.repository.RefRepository;

import java.util.Objects;

// Merge (Fast-forward only)
// 대상 브랜치가 현재 브랜치의 후손이면 Fast-forward 수행 (현재 브랜치 HEAD를 대상 HEAD로 이동)
// 동일 커밋이면 Already up to date
// 그 외(분기된 상태)는 Not fast-forward (추후 3-way로 확장 가능)
public final class MergeService {
    public enum MergeResult {
        ALREADY_UP_TO_DATE,
        FAST_FORWARD,
        BRANCH_NOT_FOUND,
        NOT_FAST_FORWARD
    }

    private final RefRepository refRepository;
    private final ObjectReader objectReader;

    public MergeService(RefRepository refRepository, ObjectReader objectReader) {
        this.refRepository = Objects.requireNonNull(refRepository, "refRepository");
        this.objectReader = Objects.requireNonNull(objectReader, "objectReader");
    }

    public MergeResult merge(String targetBranch) {
        if (targetBranch == null || targetBranch.isBlank()) {
            return MergeResult.BRANCH_NOT_FOUND;
        }
        String currentBranch = refRepository.readCurrentBranch();
        String currentHead = refRepository.readBranchHead(currentBranch);
        String targetHead = refRepository.readBranchHead(targetBranch);

        // 대상 브랜치가 없거나 HEAD가 비어 있으면 병합 불가
        if (targetHead == null || targetHead.isBlank()) {
            return MergeResult.BRANCH_NOT_FOUND;
        }

        // current가 비어 있으면(초기 브랜치) 단순 Fast-forward
        if (currentHead == null || currentHead.isBlank()) {
            refRepository.updateBranchHead(currentBranch, targetHead);
            return MergeResult.FAST_FORWARD;
        }

        // 동일 커밋이면 할 일 없음
        if (currentHead.equals(targetHead)) {
            return MergeResult.ALREADY_UP_TO_DATE;
        }

        // Fast-forward 가능한가? (current가 target의 조상인지 검사)
        if (isAncestor(currentHead, targetHead)) {
            refRepository.updateBranchHead(currentBranch, targetHead);
            return MergeResult.FAST_FORWARD;
        }

        return MergeResult.NOT_FAST_FORWARD;
    }

    // possibleAncestor가 commit의 조상인지 부모 체인을 따라 확인
    private boolean isAncestor(String possibleAncestor, String commit) {
        String cursor = commit;
        while (cursor != null && !cursor.isBlank()) {
            if (cursor.equals(possibleAncestor)) {
                return true;
            }
            var parsed = parseParent(cursor);
            cursor = parsed;
        }
        return false;
    }

    // Commit raw에서 parent 해시만 추출 (ObjectReader의 파서와 동일 포맷 가정)
    private String parseParent(String commitHash) {
        byte[] bytes = objectReader.readRaw(commitHash);
        // 매우 단순 파서: "parent <sha>\n" 라인을 찾아 반환
        String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.startsWith("parent ")) {
                return line.substring("parent ".length()).trim();
            }
            if (line.isBlank()) {
                // 헤더 종료 이후는 메시지이므로 중단
                break;
            }
        }
        return null;
    }
}


