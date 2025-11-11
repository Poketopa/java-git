package app.config;

import app.controller.GitController;
import main.java.app.repository.FileIndexRepository;
import main.java.app.repository.FileObjectRepository;
import main.java.app.repository.FileObjectWriter;
import main.java.app.repository.FileRefRepository;
import main.java.app.repository.IndexRepository;
import main.java.app.repository.ObjectRepository;
import main.java.app.repository.ObjectWriter;
import main.java.app.repository.RefRepository;
import main.java.app.service.AddService;
import main.java.app.service.AddServiceImpl;
import main.java.app.service.CommitService;
import main.java.app.service.CommitServiceImpl;
import main.java.app.service.FileSystemInitService;
import main.java.app.service.InitService;
import main.java.app.service.InitServiceImpl;

import java.nio.file.Path;
import java.util.Objects;

public final class Appconfig {
    private final Path rootDir;

    public Appconfig(Path rootDir) {
        this.rootDir = Objects.requireNonNull(rootDir, "rootDir");
    }

    public GitController gitController() {
        return new GitController(initService(), addService(), commitService());
    }

    private InitService initService() {
        return new InitServiceImpl(new FileSystemInitService(), rootDir);
    }

    private AddService addService() {
        ObjectRepository objectRepository = new FileObjectRepository(rootDir);
        IndexRepository indexRepository = new FileIndexRepository(rootDir);
        return new AddServiceImpl(objectRepository, indexRepository, rootDir);
    }

    private CommitService commitService() {
        IndexRepository indexRepository = new FileIndexRepository(rootDir);
        ObjectWriter objectWriter = new FileObjectWriter(rootDir);
        RefRepository refRepository = new FileRefRepository(rootDir);
        return new CommitServiceImpl(indexRepository, objectWriter, refRepository, rootDir);
    }
}
