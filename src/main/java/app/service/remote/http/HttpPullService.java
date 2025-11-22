package main.java.app.service.remote.http;

import main.java.app.remote.http.HttpRemoteClient;
import main.java.app.repository.ObjectReader;
import main.java.app.repository.ObjectWriter;
import main.java.app.repository.RefRepository;
import main.java.app.repository.FileObjectWriter;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

// HTTP Remote Pull (FF-only, 투박한 전송)
public final class HttpPullService {
    public enum Result {
        SUCCESS,
        ALREADY_UP_TO_DATE,
        REMOTE_NO_COMMITS,
        NOT_FAST_FORWARD
    }

    private final RefRepository localRefRepository;
    private final ObjectReader localObjectReader;
    private final ObjectWriter localObjectWriter;

    public HttpPullService(RefRepository localRefRepository, ObjectReader localObjectReader, Path localRoot) {
        this.localRefRepository = Objects.requireNonNull(localRefRepository, "localRefRepository");
        this.localObjectReader = Objects.requireNonNull(localObjectReader, "localObjectReader");
        this.localObjectWriter = new FileObjectWriter(Objects.requireNonNull(localRoot, "localRoot"));
    }

    public Result pull(String remoteBaseUrl, String branch) {
        HttpRemoteClient remote = new HttpRemoteClient(remoteBaseUrl);
        Map<String, String> refs = remote.listRefs();
        String remoteHead = refs.get(branch);
        if (remoteHead == null || remoteHead.isBlank()) {
            return Result.REMOTE_NO_COMMITS;
        }
        String localHead = localRefRepository.readBranchHead(branch);
        if (Objects.equals(localHead, remoteHead)) {
            return Result.ALREADY_UP_TO_DATE;
        }
        if (localHead != null && !localHead.isBlank() && !isAncestor(localHead, remoteHead)) {
            return Result.NOT_FAST_FORWARD;
        }

        downloadCommitChain(remote, remoteHead);
        localRefRepository.updateBranchHead(branch, remoteHead);
        return Result.SUCCESS;
    }

    private void downloadCommitChain(HttpRemoteClient remote, String commitSha) {
        String cursor = commitSha;
        while (cursor != null && !cursor.isBlank()) {
            ensureObject(remote, cursor);
            String parent = parseParent(cursor);
            String treeSha = parseTree(cursor);
            ensureObject(remote, treeSha);
            String treeContent = new String(localObjectReader.readRaw(treeSha), java.nio.charset.StandardCharsets.UTF_8);
            for (String line : treeContent.split("\n")) {
                if (!line.startsWith("blob ")) {
                    continue;
                }
                int firstSpace = line.indexOf(' ');
                int secondSpace = line.indexOf(' ', firstSpace + 1);
                if (firstSpace <= 0 || secondSpace <= firstSpace + 1) {
                    continue;
                }
                String blobSha = line.substring(firstSpace + 1, secondSpace);
                ensureObject(remote, blobSha);
            }
            cursor = parent;
        }
    }

    private void ensureObject(HttpRemoteClient remote, String oid) {
        try {
            localObjectReader.readRaw(oid);
        } catch (IllegalArgumentException e) {
            byte[] bytes = remote.getObject(oid);
            localObjectWriter.write(bytes);
        }
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
        String prefix = "parent ";
        byte[] bytes = localObjectReader.readRaw(commitHash);
        String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
            if (line.isBlank()) {
                break;
            }
        }
        return null;
    }

    private String parseTree(String commitHash) {
        String prefix = "tree ";
        byte[] bytes = localObjectReader.readRaw(commitHash);
        String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
            if (line.isBlank()) {
                break;
            }
        }
        return null;
    }
}




