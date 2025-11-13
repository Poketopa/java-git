package main.java.app.repository;

import main.java.app.domain.Index;
import main.java.app.exception.ErrorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Index를 파일로 저장 / 읽기

public final class FileIndexRepository implements IndexRepository {
    private static final String DOT_JAVA_GIT = ".javaGit";
    private static final String INDEX = "index";
    // 저장소 루트 (DI로 주입됨)
    private final Path rootDirectoryPath;

    public FileIndexRepository(Path rootDirectoryPath) {
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    // Index 폴더의 파일들을 읽는다
    // 존재하지 않으면 빈 Index를 반환
    @Override
    public Index read() {
        Path indexFilePath = getIndexFilePath();
        if (!Files.exists(indexFilePath)) {
            return new Index(Map.of());
        }
        return readIndexFile(indexFilePath);
    }

    // index 폴더 없으면 생성, 있으면 파일 저장
    // my-project/.javaGit/index
    // 저장 문자열 예시: a1b2c3d4e5f6... src/Main.java
    @Override
    public void write(Index index) {
        Path indexFilePath = getIndexFilePath();
        Path indexDirectoryPath = indexFilePath.getParent();
        createDirectoryIfNotExists(indexDirectoryPath);
        String indexContent = buildIndexContent(index);
        writeIndexFile(indexFilePath, indexContent);
    }

    // Index 파일 경로를 생성한다
    private Path getIndexFilePath() {
        return rootDirectoryPath.resolve(DOT_JAVA_GIT).resolve(INDEX);
    }

    // Index 폴더 내용을 한줄 씩 읽고 String으로 파싱하여 Map으로 저장
    private Index readIndexFile(Path indexFilePath) {
        try {
            List<String> indexFileLines = Files.readAllLines(indexFilePath, StandardCharsets.UTF_8);
            Map<String, String> stagedFilesMap = parseIndexLines(indexFileLines);
            return new Index(stagedFilesMap);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.INDEX_FILE_READ_FAILED.message());
        }
    }

    // 읽기
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

    // 빈 라인 체크
    private boolean isBlankLine(String line) {
        return line == null || line.isBlank();
    }

    // 해시값과 파일 경로를 분리
    // a1b2c3d4e5f6... src/Main.java
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


