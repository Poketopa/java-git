package main.java.app.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import main.java.app.exception.ErrorCode;

public final class Index {
    private final Map<String, String> stagedFiles;

    public Index(Map<String, String> stagedFiles) {
        validate(stagedFiles);
        if (stagedFiles.isEmpty()) {
            this.stagedFiles = Collections.emptyMap();
        } else {
            Map<String, String> copy = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : stagedFiles.entrySet()) {
                String path = e.getKey();
                String oid = e.getValue();
                checkStagedFileNull(path, oid);
                copy.put(path, oid);
            }
            this.stagedFiles = Collections.unmodifiableMap(copy);
        }
    }

    private void validate(Map<String, String> stagedFiles) {
        checkNull(stagedFiles);
    }

    private void checkNull(Map<String, String> stagedFiles) {
        if (stagedFiles == null) {
            throw new IllegalArgumentException(ErrorCode.INDEX_STAGED_FILES_NULL.message());
        }
    }

    private void checkStagedFileNull(String path, String oid) {
        if (path == null) {
            throw new IllegalArgumentException(ErrorCode.INDEX_STAGED_FILE_PATH_NULL.message());
        }
        if (oid == null) {
            throw new IllegalArgumentException(ErrorCode.INDEX_STAGED_FILE_OID_NULL.message());
        }
    }

    public Map<String, String> stagedFiles() {
        return stagedFiles;
    }
}
