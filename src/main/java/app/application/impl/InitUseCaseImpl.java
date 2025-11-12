package main.java.app.application.impl;

import main.java.app.application.InitUseCase;
import main.java.app.service.FileSystemInitService;

import java.nio.file.Path;
import java.util.Objects;

public final class InitUseCaseImpl implements InitUseCase {
    private final FileSystemInitService fileSystemInitService;
    private final Path rootDirectoryPath;

    public InitUseCaseImpl(FileSystemInitService fileSystemInitService, Path rootDirectoryPath) {
        this.fileSystemInitService = Objects.requireNonNull(fileSystemInitService, "fileSystemInitService");
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    @Override
    public void init() {
        fileSystemInitService.initRepository(rootDirectoryPath);
    }
}



