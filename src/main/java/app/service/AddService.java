package app.service;

import app.domain.Index;
import app.exception.ErrorCode;
import app.repository.IndexRepository;
import app.repository.ObjectWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;




public final class AddService {
    private final ObjectWriter objectWriter;
    private final IndexRepository indexRepository;
    private final Path rootDirectoryPath;

    public AddService(ObjectWriter objectWriter, IndexRepository indexRepository, Path rootDirectoryPath) {
        this.objectWriter = Objects.requireNonNull(objectWriter, "objectWriter");
        this.indexRepository = Objects.requireNonNull(indexRepository, "indexRepository");
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    public void add(List<String> filePaths) {
        
        if (filePaths == null || filePaths.isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.EMPTY_PATHS.message());
        }

        
        Index currentIndex = indexRepository.read();
        Map<String, String> stagedFilesMap = new LinkedHashMap<>(currentIndex.stagedFiles());

        
        for (String filePath : filePaths) {
            Path absoluteFilePath = rootDirectoryPath.resolve(filePath);
            if (!Files.exists(absoluteFilePath) || Files.isDirectory(absoluteFilePath)) {
                
                continue;
            }

            byte[] fileContent = readFileContent(absoluteFilePath);
            String objectHash = objectWriter.write(fileContent);
            stagedFilesMap.put(filePath, objectHash);
        }

        
        indexRepository.write(new Index(stagedFilesMap));
    }

    
    
    private byte[] readFileContent(Path filePath) {
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_READ_FAILED.message());
        }
    }
}


