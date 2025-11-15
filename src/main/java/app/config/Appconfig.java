package main.java.app.config;

import main.java.app.controller.GitController;
import main.java.app.view.OutputView;
import main.java.app.repository.FileIndexRepository;
import main.java.app.repository.FileObjectReader;
import main.java.app.repository.FileObjectWriter;
import main.java.app.repository.FileRefRepository;
import main.java.app.repository.IndexRepository;
import main.java.app.repository.ObjectReader;
import main.java.app.repository.ObjectWriter;
import main.java.app.repository.RefRepository;
import main.java.app.service.AddService;
import main.java.app.service.CommitService;
import main.java.app.service.FileSystemInitService;
import main.java.app.service.InitService;
import main.java.app.service.StatusService;

import java.nio.file.Path;
import java.util.Objects;

public final class Appconfig {
    private final Path rootDirectoryPath;

    public Appconfig(Path rootDirectoryPath) {
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    public GitController gitController() {
        return new GitController(initService(), addService(), commitService(), statusService(), outputView());
    }

    private InitService initService() {
        return new InitService(new FileSystemInitService(), rootDirectoryPath);
    }

    private AddService addService() {
        IndexRepository indexRepository = new FileIndexRepository(rootDirectoryPath);
        ObjectWriter objectWriter = new FileObjectWriter(rootDirectoryPath);
        return new AddService(objectWriter, indexRepository, rootDirectoryPath);
    }

    private CommitService commitService() {
        IndexRepository indexRepository = new FileIndexRepository(rootDirectoryPath);
        ObjectWriter objectWriter = new FileObjectWriter(rootDirectoryPath);
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        return new CommitService(indexRepository, objectWriter, refRepository, rootDirectoryPath);
    }

    private StatusService statusService() {
        IndexRepository indexRepository = new FileIndexRepository(rootDirectoryPath);
        RefRepository refRepository = new FileRefRepository(rootDirectoryPath);
        ObjectReader objectReader = new FileObjectReader(rootDirectoryPath);
        return new StatusService(indexRepository, refRepository, objectReader, rootDirectoryPath);
    }

    private OutputView outputView() {
        return new OutputView();
    }
}


