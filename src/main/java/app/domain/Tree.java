package app.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import app.exception.ErrorCode;

public final class Tree {
    private final Map<String, String> entries;

    public Tree(Map<String, String> entries) {
        validate(entries);
        if (entries.isEmpty()) {
            this.entries = Collections.emptyMap();
            return;
        }
        Map<String, String> copy = new HashMap<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            String path = entry.getKey();
            String objectId = entry.getValue();
            checkEntry(path, objectId);
            copy.put(path, objectId);
        }
        this.entries = Collections.unmodifiableMap(copy);
    }

    private void validate(Map<String, String> entries) {
        if (entries == null) {
            throw new IllegalArgumentException(ErrorCode.TREE_ENTRIES_NULL.message());
        }
    }

    private void checkEntry(String path, String objectId) {
        if (path == null) {
            throw new IllegalArgumentException(ErrorCode.TREE_ENTRY_PATH_NULL.message());
        }
        if (objectId == null) {
            throw new IllegalArgumentException(ErrorCode.TREE_ENTRY_OID_NULL.message());
        }
    }

    public Map<String, String> entries() {
        return entries;
    }
}



