package app.service;

import java.nio.file.Path;
import java.util.Objects;

public final class InitService {
    private final FileSystemInitService fileSystemInitService;
    private final Path rootDirectoryPath;

    public InitService(FileSystemInitService fileSystemInitService, Path rootDirectoryPath) {
        this.fileSystemInitService = Objects.requireNonNull(fileSystemInitService, "fileSystemInitService");
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    public void init() {
        fileSystemInitService.initRepository(rootDirectoryPath);
    }
}





