package app.repository;

import app.domain.Blob;
import app.domain.Commit;
import app.domain.Tree;

public interface ObjectReader {
    byte[] readRaw(String objectId);
    Blob readBlob(String objectId);
    Tree readTree(String objectId);
    Commit readCommit(String objectId);
}
