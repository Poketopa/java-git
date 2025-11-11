package app.service;

import app.domain.Blob;
import app.domain.Index;
import app.exception.ErrorCode;
import app.repository.IndexRepository;
import app.repository.ObjectRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AddServiceImpl implements AddService {
    private final ObjectRepository objectRepository;
    private final IndexRepository indexRepository;
    private final Path rootDir;

    public AddServiceImpl(ObjectRepository objectRepository, IndexRepository indexRepository, Path rootDir) {
        this.objectRepository = Objects.requireNonNull(objectRepository, "objectRepository");
        this.indexRepository = Objects.requireNonNull(indexRepository, "indexRepository");
        this.rootDir = Objects.requireNonNull(rootDir, "rootDir");
    }

    @Override
    public void add(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.EMPTY_PATHS.message());
        }
        Index current = indexRepository.read();
        Map<String, String> map = new LinkedHashMap<>(current.stagedFiles());
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

