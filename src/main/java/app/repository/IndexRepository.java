package main.java.app.repository;

import main.java.app.domain.Index;

public interface IndexRepository {
    Index read();
    void write(Index index);
}
