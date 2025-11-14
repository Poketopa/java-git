package main.java.app.repository;

import main.java.app.domain.Blob;
import main.java.app.domain.Commit;
import main.java.app.domain.Tree;

public interface ObjectReader {
    byte[] readRaw(String oid);
    Blob readBlob(String oid);
    Tree readTree(String oid);
    Commit readCommit(String oid);
}


