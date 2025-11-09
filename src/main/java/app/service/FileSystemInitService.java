package main.java.app.service;

import main.java.app.domain.port.RepositoryInitializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class FileSystemInitService implements RepositoryInitializer {
    private static final String DOT_JGIT = ".jgit";

    @Override
    public void initRepository(Path rootDir) {
        Objects.requireNonNull(rootDir, "rootDir");
        Path dot = rootDir.resolve(DOT_JGIT);
        Path objects = dot.resolve("objects");
        Path refsHeads = dot.resolve("refs").resolve("heads");
        Path head = dot.resolve("HEAD");
        Path index = dot.resolve("index");
        try {
            Files.createDirectories(objects);
            Files.createDirectories(refsHeads);
            if (!Files.exists(head)) {
                Files.writeString(head, "ref: refs/heads/master\n", StandardCharsets.UTF_8);
            }
            if (!Files.exists(refsHeads.resolve("master"))) {
                Files.writeString(refsHeads.resolve("master"), "", StandardCharsets.UTF_8);
            }
            if (!Files.exists(index)) {
                Files.writeString(index, "", StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
        }
    }
}


