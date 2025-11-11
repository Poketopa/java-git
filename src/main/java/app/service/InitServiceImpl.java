package app.service;

import app.domain.port.RepositoryInitializer;

import java.nio.file.Path;
import java.util.Objects;

public final class InitServiceImpl implements InitService {
    private final RepositoryInitializer repositoryInitializer;
    private final Path rootDir;

    public InitServiceImpl(RepositoryInitializer repositoryInitializer, Path rootDir) {
        this.repositoryInitializer = Objects.requireNonNull(repositoryInitializer, "repositoryInitializer");
        this.rootDir = Objects.requireNonNull(rootDir, "rootDir");
    }

    @Override
    public void init() {
        repositoryInitializer.initRepository(rootDir);
    }
}

