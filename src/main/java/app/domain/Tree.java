package main.java.app.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import main.java.app.exception.ErrorCode;

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
            String oid = entry.getValue();
            checkEntry(path, oid);
            copy.put(path, oid);
        }
        this.entries = Collections.unmodifiableMap(copy);
    }

    private void validate(Map<String, String> entries) {
        if (entries == null) {
            throw new IllegalArgumentException(ErrorCode.TREE_ENTRIES_NULL.message());
        }
    }

    private void checkEntry(String path, String oid) {
        if (path == null) {
            throw new IllegalArgumentException(ErrorCode.TREE_ENTRY_PATH_NULL.message());
        }
        if (oid == null) {
            throw new IllegalArgumentException(ErrorCode.TREE_ENTRY_OID_NULL.message());
        }
    }

    public Map<String, String> entries() {
        return entries;
    }
}



