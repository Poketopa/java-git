package app.repository;

import app.domain.Index;
import app.exception.ErrorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;



public final class FileIndexRepository implements IndexRepository {
    private static final String DOT_JAVA_GIT = ".javaGit";
    private static final String INDEX = "index";
    
    private final Path rootDirectoryPath;

    public FileIndexRepository(Path rootDirectoryPath) {
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    
    
    @Override
    public Index read() {
        Path indexFilePath = getIndexFilePath();
        if (!Files.exists(indexFilePath)) {
            return new Index(Map.of());
        }
        return readIndexFile(indexFilePath);
    }

    
    
    
    @Override
    public void write(Index index) {
        Path indexFilePath = getIndexFilePath();
        Path indexDirectoryPath = indexFilePath.getParent();
        createDirectoryIfNotExists(indexDirectoryPath);
        String indexContent = buildIndexContent(index);
        writeIndexFile(indexFilePath, indexContent);
    }

    
    private Path getIndexFilePath() {
        return rootDirectoryPath.resolve(DOT_JAVA_GIT).resolve(INDEX);
    }

    
    private Index readIndexFile(Path indexFilePath) {
        try {
            List<String> indexFileLines = Files.readAllLines(indexFilePath, StandardCharsets.UTF_8);
            Map<String, String> stagedFilesMap = parseIndexLines(indexFileLines);
            return new Index(stagedFilesMap);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.INDEX_FILE_READ_FAILED.message());
        }
    }

    
    private Map<String, String> parseIndexLines(List<String> indexFileLines) {
        Map<String, String> stagedFilesMap = new LinkedHashMap<>();
        for (String line : indexFileLines) {
            if (isBlankLine(line)) {
                continue;
            }
            parseIndexLine(line, stagedFilesMap);
        }
        return stagedFilesMap;
    }

    
    private boolean isBlankLine(String line) {
        return line == null || line.isBlank();
    }

    
    
    private void parseIndexLine(String line, Map<String, String> stagedFilesMap) {
        int spaceIndex = line.indexOf(' ');
        if (spaceIndex <= 0) {
            return;
        }
        String shaHashValue = extractObjectHash(line, spaceIndex);
        String filePath = extractFilePath(line, spaceIndex);
        stagedFilesMap.put(filePath, shaHashValue);
    }

    private String extractObjectHash(String line, int spaceIndex) {
        return line.substring(0, spaceIndex);
    }

    private String extractFilePath(String line, int spaceIndex) {
        return line.substring(spaceIndex + 1);
    }

    private void createDirectoryIfNotExists(Path indexDirectoryPath) {
        try {
            Files.createDirectories(indexDirectoryPath);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.INDEX_FILE_WRITE_FAILED.message());
        }
    }

    private String buildIndexContent(Index index) {
        StringBuilder contentBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : index.stagedFiles().entrySet()) {
            appendIndexEntry(contentBuilder, entry);
        }
        return contentBuilder.toString();
    }

    private void appendIndexEntry(StringBuilder contentBuilder, Map.Entry<String, String> entry) {
        contentBuilder.append(entry.getValue()).append(' ').append(entry.getKey()).append('\n');
    }

    private void writeIndexFile(Path indexFilePath, String indexContent) {
        try {
            Files.writeString(indexFilePath, indexContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.INDEX_FILE_WRITE_FAILED.message());
        }
    }
}
