package main.java.app.view;

public final class Messages {
    public static final String INIT_SUCCESS = "저장소를 초기화했습니다.";
    public static final String ADD_MISSING_PATH = "지정된 경로가 없어 추가된 파일이 없습니다.";
    public static final String ADDED_TO_INDEX = "인덱스에 %d개 경로를 추가했습니다.";

    public static final String COMMIT_USAGE_ERROR = "사용법: git commit -m <메시지> -a <작성자>";
    public static final String COMMIT_CREATED = "커밋을 생성했습니다.";

    public static final String USAGE_HEADER = "사용법:";
    public static final String USAGE_INIT = "  git init";
    public static final String USAGE_ADD = "  git add <경로> [<경로>...]";
    public static final String USAGE_COMMIT = "  git commit -m <메시지> -a <작성자>";

    public static final String STATUS_CLEAN = "nothing to commit, working tree clean";
    public static final String STATUS_SECTION_STAGED = "Changes to be committed:";
    public static final String STATUS_SECTION_NOT_STAGED = "Changes not staged for commit:";
    public static final String STATUS_SECTION_UNTRACKED = "Untracked files:";
    public static final String STATUS_INDENT = "  ";
    public static final String STATUS_STAGED_ADDED = "(added)";
    public static final String STATUS_STAGED_MODIFIED = "(modified)";
    public static final String STATUS_STAGED_DELETED = "(deleted)";
    public static final String STATUS_LABEL_MODIFIED = "modified: ";
    public static final String STATUS_LABEL_DELETED = "deleted: ";

    public static final String LOG_NO_COMMITS = "no commits yet";
    public static final String LOG_LABEL_AUTHOR = "author: ";
    public static final String LOG_LABEL_DATE = "date: ";

    private Messages() {
    }
}

