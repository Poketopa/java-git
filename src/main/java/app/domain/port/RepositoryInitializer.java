package main.java.app.domain.port;

import java.nio.file.Path;

public interface RepositoryInitializer {
    void initRepository(Path rootDir);
}
