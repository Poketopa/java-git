package main.java.app.service;

import java.nio.file.Path;

public interface RepositoryInitializer {
    void initRepository(Path rootDir);
}

