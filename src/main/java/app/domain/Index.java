package app.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import app.exception.ErrorCode;

public final class Index {
    private final Map<String, String> stagedFiles;

    public Index(Map<String, String> stagedFiles) {
        validate(stagedFiles);
        if (stagedFiles.isEmpty()) {
            this.stagedFiles = Collections.emptyMap();
            return;
        }
        Map<String, String> copy = new HashMap<>();
        for (Map.Entry<String, String> stagedFile : stagedFiles.entrySet()) {
            String path = stagedFile.getKey();
            String objectId = stagedFile.getValue();
            checkStagedFileNull(path, objectId);
            copy.put(path, objectId);
        }
        this.stagedFiles = Collections.unmodifiableMap(copy);
    }

    private void validate(Map<String, String> stagedFiles) {
        checkNull(stagedFiles);
    }

    private void checkNull(Map<String, String> stagedFiles) {
        if (stagedFiles == null) {
            throw new IllegalArgumentException(ErrorCode.INDEX_STAGED_FILES_NULL.message());
        }
    }

    private void checkStagedFileNull(String path, String objectId) {
        if (path == null) {
            throw new IllegalArgumentException(ErrorCode.INDEX_STAGED_FILE_PATH_NULL.message());
        }
        if (objectId == null) {
            throw new IllegalArgumentException(ErrorCode.INDEX_STAGED_FILE_OID_NULL.message());
        }
    }

    public Map<String, String> stagedFiles() {
        return stagedFiles;
    }
}
