package main.java.app.application.impl;

import main.java.app.application.InitUseCase;
import main.java.app.domain.port.RepositoryInitializer;

import java.nio.file.Path;
import java.util.Objects;

public final class InitUseCaseImpl implements InitUseCase {
    private final RepositoryInitializer repositoryInitializer;
    private final Path rootDir;

    public InitUseCaseImpl(RepositoryInitializer repositoryInitializer, Path rootDir) {
        this.repositoryInitializer = Objects.requireNonNull(repositoryInitializer, "repositoryInitializer");
        this.rootDir = Objects.requireNonNull(rootDir, "rootDir");
    }

    @Override
    public void init() {
        repositoryInitializer.initRepository(rootDir);
    }
}


