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

// 파일을 스테이징(Index)에 추가
// - 파일 내용을 Blob으로 저장(ObjectWriter)
// - path -> sha 매핑을 Index에 반영
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
        // 1) 입력 검증
        if (filePaths == null || filePaths.isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.EMPTY_PATHS.message());
        }

        // 2) 현재 Index를 읽고 Map으로 작업
        Index currentIndex = indexRepository.read();
        Map<String, String> stagedFilesMap = new LinkedHashMap<>(currentIndex.stagedFiles());

        // 3) 각 파일을 읽어 Blob으로 저장한 뒤, path -> sha 매핑
        for (String filePath : filePaths) {
            Path absoluteFilePath = rootDirectoryPath.resolve(filePath);
            if (!Files.exists(absoluteFilePath) || Files.isDirectory(absoluteFilePath)) {
                // 존재하지 않거나 디렉토리는 스킵
                continue;
            }

            byte[] fileContent = readFileContent(absoluteFilePath);
            String objectHash = objectWriter.write(fileContent);
            stagedFilesMap.put(filePath, objectHash);
        }

        // 4) 변경된 Index를 저장
        indexRepository.write(new Index(stagedFilesMap));
    }

    // 파일 내용을 읽어 바이트 배열로 반환
    // - IO 실패 시 표준화된 메시지로 예외 전환
    private byte[] readFileContent(Path filePath) {
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.FILE_READ_FAILED.message());
        }
    }
}


