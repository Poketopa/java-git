package app.service.remote.http;

import app.remote.http.HttpRemoteClient;
import app.repository.ObjectReader;
import app.repository.RefRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;


public final class HttpPushService {
    private static final String DOT_JAVA_GIT = ".javaGit";
    private static final String OBJECTS = "objects";
    private static final String PARENT_PREFIX = "parent ";
    private final RefRepository localRefRepository;
    private final ObjectReader localObjectReader;
    private final Path localRoot;
    public HttpPushService(RefRepository localRefRepository, ObjectReader localObjectReader, Path localRoot) {
        this.localRefRepository = Objects.requireNonNull(localRefRepository, "localRefRepository");
        this.localObjectReader = Objects.requireNonNull(localObjectReader, "localObjectReader");
        this.localRoot = Objects.requireNonNull(localRoot, "localRoot");
    }

    public Result push(String remoteBaseUrl, String branch) {
        HttpRemoteClient remote = new HttpRemoteClient(remoteBaseUrl);
        String localHead = localRefRepository.readBranchHead(branch);
        if (localHead == null || localHead.isBlank()) {
            return Result.LOCAL_NO_COMMITS;
        }
        Map<String, String> refs = remote.listRefs();
        String remoteHead = refs.get(branch);
        if (Objects.equals(remoteHead, localHead)) {
            return Result.ALREADY_UP_TO_DATE;
        }
        if (remoteHead != null && !remoteHead.isBlank() && !isAncestor(remoteHead, localHead)) {
            return Result.REMOTE_REJECTED_NON_FF;
        }

        java.util.Set<String> remoteObjects = remote.listObjects();
        Path objectsDir = localRoot.resolve(DOT_JAVA_GIT).resolve(OBJECTS);
        if (Files.exists(objectsDir)) {
            try (var dirs = Files.list(objectsDir)) {
                dirs.filter(Files::isDirectory).forEach(dir -> {
                    String prefix = dir.getFileName().toString();
                    try (var files = Files.list(dir)) {
                        files.filter(Files::isRegularFile).forEach(f -> {
                            String suffix = f.getFileName().toString();
                            String oid = prefix + suffix;
                            if (remoteObjects.contains(oid)) {
                                return;
                            }
                            try {
                                byte[] bytes = Files.readAllBytes(f);
                                remote.putObject(oid, bytes);
                            } catch (IOException e) {
                                throw new IllegalArgumentException("[ERROR] 로컬 객체 읽기 실패", e);
                            }
                        });
                    } catch (IOException e) {
                        throw new IllegalArgumentException("[ERROR] 로컬 objects 열기 실패", e);
                    }
                });
            } catch (IOException e) {
                throw new IllegalArgumentException("[ERROR] 로컬 objects 디렉터리 열기 실패", e);
            }
        }
        remote.updateRef(branch, remoteHead, localHead);
        return Result.SUCCESS;
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

    public enum Result {
        SUCCESS,
        ALREADY_UP_TO_DATE,
        REMOTE_REJECTED_NON_FF,
        LOCAL_NO_COMMITS
    }
}
