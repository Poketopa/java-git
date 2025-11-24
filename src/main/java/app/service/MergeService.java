package app.service;

import app.repository.ObjectReader;
import app.repository.RefRepository;
import java.util.Objects;


public final class MergeService {
    private static final String PARENT_PREFIX = "parent ";
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

        if (targetHead == null || targetHead.isBlank()) {
            return MergeResult.BRANCH_NOT_FOUND;
        }

        if (currentHead == null || currentHead.isBlank()) {
            refRepository.updateBranchHead(currentBranch, targetHead);
            return MergeResult.FAST_FORWARD;
        }

        if (currentHead.equals(targetHead)) {
            return MergeResult.ALREADY_UP_TO_DATE;
        }

        if (isAncestor(currentHead, targetHead)) {
            refRepository.updateBranchHead(currentBranch, targetHead);
            return MergeResult.FAST_FORWARD;
        }

        return MergeResult.NOT_FAST_FORWARD;
    }

    private boolean isAncestor(String possibleAncestor, String commit) {
        String cursor = commit;
        while (cursor != null && !cursor.isBlank()) {
            if (cursor.equals(possibleAncestor)) {
                return true;
            }
            String parentHash = parseParent(cursor);
            cursor = parentHash;
        }
        return false;
    }

    private String parseParent(String commitHash) {
        byte[] bytes = objectReader.readRaw(commitHash);

        String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.startsWith(PARENT_PREFIX)) {
                return line.substring(PARENT_PREFIX.length()).trim();
            }
            if (line.isBlank()) {

                break;
            }
        }
        return null;
    }


    public enum MergeResult {
        ALREADY_UP_TO_DATE,
        FAST_FORWARD,
        BRANCH_NOT_FOUND,
        NOT_FAST_FORWARD
    }
}
