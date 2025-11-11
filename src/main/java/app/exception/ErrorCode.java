package main.java.app.exception;

public enum ErrorCode {
    EMPTY_PATHS("[ERROR] 추가할 경로가 비어 있습니다."),
    SHA1_NOT_AVAILABLE("[ERROR] SHA-1 알고리즘을 사용할 수 없습니다."),
    FILE_IO_ERROR("[ERROR] 파일 시스템 작업 중 오류가 발생했습니다."),
    INDEX_READ_ERROR("[ERROR] 인덱스 파일을 읽는 도중 오류가 발생했습니다."),
    INDEX_WRITE_ERROR("[ERROR] 인덱스 파일을 쓰는 도중 오류가 발생했습니다."),
    BLOB_FILE_NULL("[ERROR] Blob 내용은 null일 수 없습니다."),
    BLOB_FILE_EMPTY("[ERROR] Blob 내용은 비어있을 수 없습니다."),
    INDEX_STAGED_FILES_NULL("[ERROR] Index staged files는 null일 수 없습니다."),
    INDEX_STAGED_FILE_PATH_NULL("[ERROR] Index staged file의 경로는 null일 수 없습니다."),
    INDEX_STAGED_FILE_OID_NULL("[ERROR] Index staged file의 OID는 null일 수 없습니다."),
    INDEX_FILE_NOT_FOUND("[ERROR] Index 파일을 찾을 수 없습니다."),
    INDEX_FILE_READ_FAILED("[ERROR] Index 파일 읽기에 실패했습니다."),
    INDEX_FILE_WRITE_FAILED("[ERROR] Index 파일 쓰기에 실패했습니다."),
    OBJECT_DIRECTORY_CREATE_FAILED("[ERROR] 객체 디렉토리 생성에 실패했습니다."),
    OBJECT_FILE_WRITE_FAILED("[ERROR] 객체 파일 쓰기에 실패했습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}


