package main.java.app.config;

import main.java.app.controller.GitController;
import main.java.app.repository.FileIndexRepository;
import main.java.app.repository.FileObjectRepository;
import main.java.app.repository.FileObjectWriter;
import main.java.app.repository.FileRefRepository;
import main.java.app.repository.IndexRepository;
import main.java.app.repository.ObjectRepository;
import main.java.app.repository.ObjectWriter;
import main.java.app.repository.RefRepository;
import main.java.app.service.AddService;
import main.java.app.service.CommitService;
import main.java.app.service.FileSystemInitService;
import main.java.app.service.InitService;

import java.nio.file.Path;
import java.util.Objects;

public final class Appconfig {
    private final Path rootDirectoryPath;

    public Appconfig(Path rootDirectoryPath) {
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    public GitController gitController() {
        return new GitController(initService(), addService(), commitService());
    }

    private InitService initService() {
        return new InitService(new FileSystemInitService(), rootDirectoryPath);
    }

    private AddService addService() {
        ObjectRepository objectRepository = new FileObjectRepository(rootDirectoryPath);
        IndexRepository indexRepository = new FileIndexRepository(rootDirectoryPath);
        return new AddService(objectRepository, indexRepository, rootDirectoryPath);
    }

    private CommitService commitService() {
        IndexRepository indexRepository = new FileIndexRepository(rootDirectoryPath);
        ObjectWriter objectWriter = new FileObjectWriter(rootDirectoryPath);
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        return new CommitService(indexRepository, objectWriter, refRepository, rootDirectoryPath);
    }
}


