package app.view;

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

    public static final String BRANCH_LIST_HEADER = "Branches:";
    public static final String BRANCH_CREATED = "브랜치를 생성했습니다: ";
    public static final String BRANCH_ALREADY_EXISTS = "이미 존재하는 브랜치입니다: ";
    public static final String BRANCH_NAME_INVALID = "브랜치 이름이 유효하지 않습니다.";

    public static final String CHECKOUT_USAGE = "사용법: git checkout <브랜치>";
    public static final String CHECKOUT_SUCCESS = "브랜치를 전환했습니다: ";
    public static final String CHECKOUT_DIRTY = "작업 트리가 깨끗하지 않습니다. 변경 사항을 정리하세요.";
    public static final String CHECKOUT_NOT_FOUND = "존재하지 않는 브랜치입니다: ";

    
    public static final String MERGE_USAGE = "사용법: git merge <브랜치>";
    public static final String MERGE_ALREADY_UP_TO_DATE = "Already up to date.";
    public static final String MERGE_FAST_FORWARD = "Fast-forward: ";
    public static final String MERGE_BRANCH_NOT_FOUND = "존재하지 않는 브랜치입니다: ";
    public static final String MERGE_NOT_FAST_FORWARD = "이 병합은 fast-forward로 처리할 수 없습니다.";

    
    public static final String PUSH_USAGE = "사용법: git push <remote-dir> <브랜치>";
    public static final String PUSH_SUCCESS = "푸시 완료: ";
    public static final String PUSH_UP_TO_DATE = "원격이 이미 최신 상태입니다.";
    public static final String PUSH_REJECTED_NON_FF = "푸시 거부: fast-forward가 아닙니다.";
    public static final String PUSH_LOCAL_NO_COMMITS = "로컬에 푸시할 커밋이 없습니다.";

    public static final String PULL_USAGE = "사용법: git pull <remote-dir> <브랜치>";
    public static final String PULL_SUCCESS = "풀 완료: ";
    public static final String PULL_UP_TO_DATE = "이미 최신 상태입니다.";
    public static final String PULL_REMOTE_NO_COMMITS = "원격에 가져올 커밋이 없습니다.";
    public static final String PULL_NOT_FAST_FORWARD = "풀 실패: fast-forward로 가져올 수 없습니다.";

    
    public static final String CLONE_USAGE = "사용법: git clone <remote-dir> <target-dir>";
    public static final String CLONE_SUCCESS = "클론 완료: ";
    public static final String CLONE_REMOTE_NOT_FOUND = "원격 디렉터리를 찾을 수 없습니다: ";
    public static final String CLONE_TARGET_EXISTS = "대상 디렉터리가 비어있지 않습니다: ";
    public static final String CLONE_REMOTE_NO_COMMITS = "원격에 가져올 커밋이 없습니다.";

    
    public static final String SERVE_HTTP_USAGE = "사용법: git serve-http <port>";
    public static final String SERVE_HTTP_STARTED = "HTTP 원격 서버가 시작되었습니다. 포트: ";

    public static final String PUSH_HTTP_USAGE = "사용법: git push-http <base-url> <브랜치>";
    public static final String PUSH_HTTP_SUCCESS = "HTTP 푸시 완료: ";
    public static final String PUSH_HTTP_UP_TO_DATE = "원격이 이미 최신 상태입니다.(HTTP)";
    public static final String PUSH_HTTP_REJECTED_NON_FF = "HTTP 푸시 거부: fast-forward가 아닙니다.";
    public static final String PUSH_HTTP_LOCAL_NO_COMMITS = "로컬에 푸시할 커밋이 없습니다.(HTTP)";

    public static final String PULL_HTTP_USAGE = "사용법: git pull-http <base-url> <브랜치>";
    public static final String PULL_HTTP_SUCCESS = "HTTP 풀 완료: ";
    public static final String PULL_HTTP_UP_TO_DATE = "이미 최신 상태입니다.(HTTP)";
    public static final String PULL_HTTP_REMOTE_NO_COMMITS = "원격에 가져올 커밋이 없습니다.(HTTP)";
    public static final String PULL_HTTP_NOT_FAST_FORWARD = "HTTP 풀 실패: fast-forward로 가져올 수 없습니다.";

    public static final String REPL_WELCOME = "Interactive mode started. Type 'help' for usage, 'exit' to quit.";
    public static final String REPL_PROMPT = "java-git> ";
    public static final String REPL_BYE = "Bye.";
    public static final String REPL_REQUIRE_GIT_PREFIX = "명령은 'git'으로 시작해야 합니다. 예: git init";
    public static final String REPL_INPUT_READ_ERROR = "[ERROR] 입력을 읽는 데 실패했습니다: ";

    private Messages() {
    }
}

