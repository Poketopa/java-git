package app.domain;

import java.util.Arrays;
import java.util.Objects;

public final class Blob {
    private final byte[] file;

    public Blob(byte[] content) {
        Objects.requireNonNull(content, "content");
        this.file = Arrays.copyOf(content, content.length);
    }

    public byte[] content() {
        return Arrays.copyOf(file, file.length);
    }
}
