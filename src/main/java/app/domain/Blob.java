package app.domain;

import java.util.Arrays;
import app.exception.ErrorCode;

public final class Blob {
    private final byte[] file;

    public Blob(byte[] file) {
        validate(file);
        this.file = Arrays.copyOf(file, file.length);
    }

    private void validate(byte[] file) {
        checkNull(file);
        checkEmpty(file);
    }

    private void checkNull(byte[] file) {
        if (file == null) {
            throw new IllegalArgumentException(ErrorCode.BLOB_FILE_NULL.message());
        }
    }

    private void checkEmpty(byte[] file) {
        if (file.length == 0) {
            throw new IllegalArgumentException(ErrorCode.BLOB_FILE_EMPTY.message());
        }
    }

    public byte[] file() {
        return Arrays.copyOf(file, file.length);
    }
}


