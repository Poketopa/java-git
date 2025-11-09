package main.java.app.config;

import main.java.app.application.AddUseCase;
import main.java.app.application.InitUseCase;
import main.java.app.application.impl.AddUseCaseImpl;
import main.java.app.application.impl.InitUseCaseImpl;
import main.java.app.controller.GitController;
import main.java.app.repository.FileIndexRepository;
import main.java.app.repository.FileObjectRepository;
import main.java.app.service.FileSystemInitService;
import main.java.app.repository.IndexRepository;
import main.java.app.repository.ObjectRepository;

import java.nio.file.Path;
import java.util.Objects;

public final class Appconfig {
    private final Path rootDir;

    public Appconfig(Path rootDir) {
        this.rootDir = Objects.requireNonNull(rootDir, "rootDir");
    }

    public GitController gitController() {
        return new GitController(initUseCase(), addUseCase());
    }

    private InitUseCase initUseCase() {
        return new InitUseCaseImpl(new FileSystemInitService(), rootDir);
    }

    private AddUseCase addUseCase() {
        ObjectRepository objectRepository = new FileObjectRepository(rootDir);
        IndexRepository indexRepository = new FileIndexRepository(rootDir);
        return new AddUseCaseImpl(objectRepository, indexRepository, rootDir);
    }
}


