package main.java.app.domain;

import java.util.Arrays;
import java.util.Objects;

public final class Blob {
    private final byte[] content;

    public Blob(byte[] content) {
        Objects.requireNonNull(content, "content");
        this.content = Arrays.copyOf(content, content.length);
    }

    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }
}
