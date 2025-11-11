package main.java.app.domain;

import main.java.app.exception.ErrorCode;

public final class Head {
    private final String refName;

    public Head(String refName) {
        validate(refName);
        this.refName = refName;
    }

    private void validate(String refName) {
        if (refName == null) {
            throw new IllegalArgumentException(ErrorCode.HEAD_REF_NULL.message());
        }
        if (refName.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.HEAD_REF_EMPTY.message());
        }
    }

    public String refName() {
        return refName;
    }
}


