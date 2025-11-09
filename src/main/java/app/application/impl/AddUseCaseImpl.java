package main.java.app.application.impl;

import main.java.app.application.AddUseCase;
import main.java.app.domain.Blob;
import main.java.app.domain.Index;
import main.java.app.repository.IndexRepository;
import main.java.app.repository.ObjectRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AddUseCaseImpl implements AddUseCase {
    private final ObjectRepository objectRepository;
    private final IndexRepository indexRepository;
    private final Path rootDir;

    public AddUseCaseImpl(ObjectRepository objectRepository, IndexRepository indexRepository, Path rootDir) {
        this.objectRepository = Objects.requireNonNull(objectRepository, "objectRepository");
        this.indexRepository = Objects.requireNonNull(indexRepository, "indexRepository");
        this.rootDir = Objects.requireNonNull(rootDir, "rootDir");
    }

    @Override
    public void add(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            throw new IllegalArgumentException("paths must not be empty");
        }
        Index current = indexRepository.read();
        Map<String, String> map = new LinkedHashMap<>(current.entries());
        for (String p : paths) {
            Path filePath = rootDir.resolve(p);
            try {
                if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                    continue;
                }
                byte[] bytes = Files.readAllBytes(filePath);
                String sha = objectRepository.writeObject(new Blob(bytes));
                map.put(p, sha);
            } catch (Exception ignored) {
            }
        }
        indexRepository.write(new Index(map));
    }
}


