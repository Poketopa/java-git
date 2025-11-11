package app.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Index {
    private final Map<String, String> entries;

    public Index(Map<String, String> entries) {
        if (entries == null || entries.isEmpty()) {
            this.entries = Collections.emptyMap();
        } else {
            Map<String, String> copy = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : entries.entrySet()) {
                String path = Objects.requireNonNull(e.getKey(), "path");
                String oid = Objects.requireNonNull(e.getValue(), "oid");
                copy.put(path, oid);
            }
            this.entries = Collections.unmodifiableMap(copy);
        }
    }

    public Map<String, String> entries() {
        return entries;
    }
}
