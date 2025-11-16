package main.java.app.repository;

import main.java.app.domain.Blob;
import main.java.app.domain.Commit;
import main.java.app.domain.Tree;

public interface ObjectReader {
    byte[] readRaw(String objectId);
    Blob readBlob(String objectId);
    Tree readTree(String objectId);
    Commit readCommit(String objectId);
}


