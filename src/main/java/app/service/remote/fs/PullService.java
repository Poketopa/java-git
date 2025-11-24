package app.service.remote.fs;

import app.remote.FileRemoteClient;
import app.repository.ObjectReader;
import app.repository.RefRepository;
import java.nio.file.Path;
import java.util.Objects;


public final class PullService {
    private final RefRepository localRefRepository;
    private final ObjectReader localObjectReader;
    private final Path localRoot;
    public PullService(RefRepository localRefRepository, ObjectReader localObjectReader, Path localRoot) {
        this.localRefRepository = Objects.requireNonNull(localRefRepository, "localRefRepository");
        this.localObjectReader = Objects.requireNonNull(localObjectReader, "localObjectReader");
        this.localRoot = Objects.requireNonNull(localRoot, "localRoot");
    }

    public PullResult pull(Path remoteRoot, String branch) {
        FileRemoteClient remote = new FileRemoteClient(remoteRoot);
        remote.ensureInitialized();

        String remoteHead = remote.readBranchHead(branch);
        if (remoteHead == null || remoteHead.isBlank()) {
            return PullResult.REMOTE_NO_COMMITS;
        }

        String localHead = localRefRepository.readBranchHead(branch);
        if (remoteHead.equals(localHead)) {
            return PullResult.ALREADY_UP_TO_DATE;
        }

        remote.copyAllRemoteObjectsToLocal(localRoot);

        if (localHead == null || localHead.isBlank() || isAncestor(localHead, remoteHead)) {
            localRefRepository.updateBranchHead(branch, remoteHead);
            return PullResult.SUCCESS;
        }
        return PullResult.NOT_FAST_FORWARD;
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
            if (line.startsWith("parent ")) {
                return line.substring("parent ".length()).trim();
            }
            if (line.isBlank()) {
                break;
            }
        }
        return null;
    }

    public enum PullResult {
        SUCCESS,
        ALREADY_UP_TO_DATE,
        REMOTE_NO_COMMITS,
        NOT_FAST_FORWARD
    }
}
