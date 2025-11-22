package main.java.app.service.remote.fs;

import main.java.app.remote.FileRemoteClient;
import main.java.app.repository.ObjectReader;
import main.java.app.repository.RefRepository;

import java.nio.file.Path;
import java.util.Objects;

// 로컬 → 원격(디렉토리)으로 푸시 (Fast-forward만)
// - 투박한 버전: 모든 로컬 objects를 원격으로 복사 후, FF 검증 시 브랜치 HEAD 갱신
public final class PushService {
    public enum PushResult {
        SUCCESS,
        ALREADY_UP_TO_DATE,
        REMOTE_REJECTED_NON_FF,
        LOCAL_NO_COMMITS
    }

    private final RefRepository localRefRepository;
    private final ObjectReader localObjectReader;
    private final Path localRoot;
    private static final String PARENT_PREFIX = "parent ";

    public PushService(RefRepository localRefRepository, ObjectReader localObjectReader, Path localRoot) {
        this.localRefRepository = Objects.requireNonNull(localRefRepository, "localRefRepository");
        this.localObjectReader = Objects.requireNonNull(localObjectReader, "localObjectReader");
        this.localRoot = Objects.requireNonNull(localRoot, "localRoot");
    }

    public PushResult push(Path remoteRoot, String branch) {
        FileRemoteClient remote = new FileRemoteClient(remoteRoot);
        remote.ensureInitialized();

        String localHead = localRefRepository.readBranchHead(branch);
        if (localHead == null || localHead.isBlank()) {
            return PushResult.LOCAL_NO_COMMITS;
        }

        String remoteHead = remote.readBranchHead(branch);
        if (remoteHead != null && !remoteHead.isBlank()) {
            if (remoteHead.equals(localHead)) {
                return PushResult.ALREADY_UP_TO_DATE;
            }
            if (!isAncestor(remoteHead, localHead)) {
                return PushResult.REMOTE_REJECTED_NON_FF;
            }
        }

        // 투박한 방식: 로컬 모든 objects를 원격으로 복사
        remote.copyAllLocalObjectsToRemote(localRoot);
        remote.updateBranchHead(branch, localHead);
        return PushResult.SUCCESS;
    }

    private boolean isAncestor(String possibleAncestor, String commit) {
        String cursor = commit;
        while (cursor != null && !cursor.isBlank()) {
            if (cursor.equals(possibleAncestor)) {
                return true;
            }
            String parent = parseParent(cursor);
            cursor = parent;
        }
        return false;
    }

    private String parseParent(String commitHash) {
        byte[] bytes = localObjectReader.readRaw(commitHash);
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
}




