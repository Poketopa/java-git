package app.repository;

import app.domain.Index;

public interface IndexRepository {
    Index read();
    void write(Index index);
}
