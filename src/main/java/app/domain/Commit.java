package app.domain;

import app.exception.ErrorCode;

public final class Commit {
    private final String message;
    private final String treeOid;
    private final String parentOid;
    private final String author;
    private final long createdAtMillis;

    public Commit(String message, String treeOid, String parentOid, String author) {
        validate(message, treeOid, author);
        this.message = message;
        this.treeOid = treeOid;
        this.parentOid = parentOid;
        this.author = author;
        this.createdAtMillis = System.currentTimeMillis();
    }

    private void validate(String message, String treeOid, String author) {
        checkMessage(message);
        checkTreeOid(treeOid);
        checkAuthor(author);
    }

    private void checkMessage(String message) {
        if (message == null) {
            throw new IllegalArgumentException(ErrorCode.COMMIT_MESSAGE_NULL.message());
        }
        if (message.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.COMMIT_MESSAGE_EMPTY.message());
        }
    }

    private void checkTreeOid(String treeOid) {
        if (treeOid == null) {
            throw new IllegalArgumentException(ErrorCode.COMMIT_TREE_OID_NULL.message());
        }
    }

    private void checkAuthor(String author) {
        if (author == null) {
            throw new IllegalArgumentException(ErrorCode.COMMIT_AUTHOR_NULL.message());
        }
        if (author.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.COMMIT_AUTHOR_EMPTY.message());
        }
    }

    public String message() {
        return message;
    }

    public String treeOid() {
        return treeOid;
    }

    public String parentOid() {
        return parentOid;
    }

    public String author() {
        return author;
    }

    public long createdAtMillis() {
        return createdAtMillis;
    }
}
