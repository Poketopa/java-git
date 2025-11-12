package main.java.app.config;

import main.java.app.application.AddUseCase;
import main.java.app.application.CommitUseCase;
import main.java.app.application.InitUseCase;
import main.java.app.application.impl.AddUseCaseImpl;
import main.java.app.application.impl.CommitUseCaseImpl;
import main.java.app.application.impl.InitUseCaseImpl;
import main.java.app.controller.GitController;
import main.java.app.repository.FileIndexRepository;
import main.java.app.repository.FileObjectRepository;
import main.java.app.repository.FileObjectWriter;
import main.java.app.repository.FileRefRepository;
import main.java.app.repository.IndexRepository;
import main.java.app.repository.ObjectRepository;
import main.java.app.repository.ObjectWriter;
import main.java.app.repository.RefRepository;
import main.java.app.service.CommitService;
import main.java.app.service.FileSystemInitService;

import java.nio.file.Path;
import java.util.Objects;

public final class Appconfig {
    private final Path rootDirectoryPath;

    public Appconfig(Path rootDirectoryPath) {
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    public GitController gitController() {
        return new GitController(initUseCase(), addUseCase(), commitUseCase());
    }

    private InitUseCase initUseCase() {
        return new InitUseCaseImpl(new FileSystemInitService(), rootDirectoryPath);
    }

    private AddUseCase addUseCase() {
        ObjectRepository objectRepository = new FileObjectRepository(rootDirectoryPath);
        IndexRepository indexRepository = new FileIndexRepository(rootDirectoryPath);
        return new AddUseCaseImpl(objectRepository, indexRepository, rootDirectoryPath);
    }

    private CommitUseCase commitUseCase() {
        IndexRepository indexRepository = new FileIndexRepository(rootDirectoryPath);
        ObjectWriter objectWriter = new FileObjectWriter(rootDirectoryPath);
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        CommitService commitService = new CommitService(indexRepository, objectWriter, refRepository, rootDirectoryPath);
        return new CommitUseCaseImpl(commitService);
    }
}


