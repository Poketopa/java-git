package main.java.app.service;

import java.nio.file.Path;
import java.util.Objects;

public final class InitServiceImpl implements InitService {
    private final RepositoryInitializer repositoryInitializer;
    private final Path rootDirectoryPath;

    public InitServiceImpl(RepositoryInitializer repositoryInitializer, Path rootDirectoryPath) {
        this.repositoryInitializer = Objects.requireNonNull(repositoryInitializer, "repositoryInitializer");
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    @Override
    public void init() {
        repositoryInitializer.initRepository(rootDirectoryPath);
    }
}

