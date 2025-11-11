package main.java.app.service;

import main.java.app.domain.Blob;
import main.java.app.domain.Index;
import main.java.app.exception.ErrorCode;
import main.java.app.repository.IndexRepository;
import main.java.app.repository.ObjectRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AddServiceImpl implements AddService {
    private final ObjectRepository objectRepository;
    private final IndexRepository indexRepository;
    private final Path rootDirectoryPath;

    public AddServiceImpl(ObjectRepository objectRepository, IndexRepository indexRepository, Path rootDirectoryPath) {
        this.objectRepository = Objects.requireNonNull(objectRepository, "objectRepository");
        this.indexRepository = Objects.requireNonNull(indexRepository, "indexRepository");
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    @Override
    public void add(List<String> paths) {
        validatePaths(paths);
        Index currentIndex = indexRepository.read();
        Map<String, String> stagedFilesMap = new LinkedHashMap<>(currentIndex.stagedFiles());
        addFilesToIndex(paths, stagedFilesMap);
        indexRepository.write(new Index(stagedFilesMap));
    }

    private void validatePaths(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.EMPTY_PATHS.message());
        }
    }

    private void addFilesToIndex(List<String> paths, Map<String, String> stagedFilesMap) {
        for (String path : paths) {
            addFileToIndex(path, stagedFilesMap);
        }
    }

    private void addFileToIndex(String path, Map<String, String> stagedFilesMap) {
        Path filePath = rootDirectoryPath.resolve(path);
        if (!isValidFile(filePath)) {
            return;
        }
        byte[] fileContent = readFileContent(filePath);
        String shaHashValue = objectRepository.writeObject(new Blob(fileContent));
        stagedFilesMap.put(path, shaHashValue);
    }

    private boolean isValidFile(Path filePath) {
        return Files.exists(filePath) && !Files.isDirectory(filePath);
    }

    private byte[] readFileContent(Path filePath) {
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_READ_FAILED.message());
        }
    }
}

