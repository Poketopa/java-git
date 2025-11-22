# 자바 깃허브 구현

## 구현 목표
- 로컬에서 CLI로 동작하는 git을 순수 Java로 구현한다.
- Git의 내부 구조(Blob/Tree/Commit/Ref/Index)를 이해하고 직접 구현한다.
- `.javaGit` 디렉토리에 Git 메타데이터를 저장한다.
- 실제 Git과 호환 가능한 SHA-1 기반 객체 저장 구조를 따른다.
- 이전 프리코스 미션 원칙(도메인 검증, 일관된 에러 포맷 등)을 준수한다.

## 기능 목록

### 메인 로직
- [x] 저장소 초기화 기능 (git init)
    - [x] `.javaGit` 디렉토리 생성
    - [x] `objects` 디렉토리 생성
    - [x] `refs/heads` 디렉토리 생성
    - [x] `HEAD` 파일 생성 및 기본 브랜치 참조 설정
    - [x] `index` 파일 생성 (스테이징 영역)
- [x] 파일 스테이징 기능 (git add)
    - [x] 파일 경로 검증
    - [x] 파일 존재 여부 확인
    - [x] 디렉토리 제외 (파일만 추가)
    - [x] 파일 내용을 Blob 객체로 변환
    - [x] SHA-1 해시 계산 및 객체 저장
    - [x] Index에 파일 경로와 해시 매핑 저장
- [x] Git 객체 저장
    - [x] Blob/Tree/Commit 객체를 `.javaGit/objects` 디렉토리에 저장
    - [x] SHA-1 해시 기반 디렉토리 구조 (앞 2자리/나머지)
    - [x] Index 파일 읽기/쓰기

### 입력 및 출력
- [x] CLI 명령어 파싱
    - [x] `git init` 명령어 처리
    - [x] `git add <path> [<path>...]` 명령어 처리
    - [x] 잘못된 명령어 입력 시 사용법 출력
- [x] 기본 출력 메시지
    - [x] 저장소 초기화 완료 메시지 출력
    - [x] 파일 추가 완료 메시지 출력
    - [x] 에러 메시지 출력

### 예외처리
- [x] ErrorCode enum 구현
    - [x] "[ERROR] ..." 형식의 표준화된 에러 메시지 정의
- [x] Domain 객체 검증
    - [x] Blob 객체: null 및 빈 파일 검증
    - [x] Index 객체: null 및 빈 staged files 검증
    - [x] Index 내부 entry: path 및 oid null 검증
- [x] Repository 예외 처리
    - [x] Index 파일 읽기 실패 시 `IllegalArgumentException` 발생
    - [x] Index 파일 쓰기 실패 시 `IllegalArgumentException` 발생
    - [x] 객체 디렉토리 생성 실패 시 `IllegalArgumentException` 발생
    - [x] 객체 파일 쓰기 실패 시 `IllegalArgumentException` 발생
    - [x] SHA-1 알고리즘 사용 불가 시 `IllegalStateException` 발생
- [x] Service 예외 처리
    - [x] 저장소 초기화 실패 시 `IllegalArgumentException` 발생
    - [x] 파일 읽기 실패 시 `IllegalArgumentException` 발생
    - [x] 빈 경로 리스트 입력 시 `IllegalArgumentException` 발생

## 명령어 구현 목표
- [x] git init
- [x] git add
- [x] git commit
- [x] git status
- [x] git log
- [x] git branch
- [x] git checkout
- [x] git merge (Fast-Forward only)
- [x] git push (로컬 디렉터리를 remote로 사용하는 push)
- [x] git pull (로컬 디렉터리를 remote로 사용하는 pull)
- [x] git clone (로컬 디렉터리 remote로부터 메타데이터만 clone)
- [x] git serve-http (HTTP 원격 서버 실행)
- [x] git push-http (HTTP 원격 저장소로 push, FF-only)
- [x] git pull-http (HTTP 원격 저장소에서 pull, FF-only)

## 기능 구현 현황

### 필요 객체 구현
- [ ] Object 객체 구현
- [x] Blob 객체 구현
- [x] Tree 객체 구현
- [x] Commit 객체 구현
- [x] Index 객체 구현
- [x] Head 객체 구현

### 명령어 구현
- [x] init: 저장소 초기화 (`.javaGit` 디렉토리 생성)
- [x] add: 파일을 스테이징 영역에 추가
- [x] commit: 스테이징된 파일들을 커밋
- [x] status: 현재 상태 확인
- [x] log: 커밋 히스토리 출력
- [x] branch: 브랜치 생성/조회
- [x] checkout: 브랜치 전환
- [x] merge: 브랜치 병합 (Fast-Forward only)
- [x] push: 로컬 디렉터리를 remote로 사용하는 push (Fast-Forward only)
- [x] pull: 로컬 디렉터리를 remote로 사용하는 pull (Fast-Forward only)
- [x] clone: 로컬 디렉터리 remote로부터 `.javaGit` 메타데이터만 복제 (워킹 트리 체크아웃 없음)

### 입력 및 출력
- [x] CLI 명령어 파싱
- [x] 기본 출력 메시지
- [x] 상세한 상태 출력 (status)
- [x] 커밋 로그 포맷팅 (log)

### 예외처리
- [x] ErrorCode enum 구현
- [x] 커스텀 예외 처리 (IllegalArgumentException)
- [x] Domain 객체 검증 (Blob, Index)
- [x] Repository 예외 처리
- [x] Service 예외 처리

## 프로젝트 구조

### Application / Config
#### Main:
- 프로그램 시작점
- 현재 작업 디렉토리 경로(Path) 획득
- `Appconfig`로 객체 생성 후 `GitController.run()` 실행

#### Appconfig:
- 수동 DI 구성
- Repository, Service, Remote Service 인스턴스 생성/주입
- `GitController` 생성 및 의존성 주입

### Controller
#### GitController:
- CLI 명령어 파싱 및 처리
- Command Handler(`InitCmd`, `AddCmd`, `CommitCmd`, `BranchCmd`, `CheckoutCmd`, `MergeCmd`, `StatusCmd`, `LogCmd`, `PushFsCmd`, `PullFsCmd`, `CloneFsCmd`, `ServeHttpCmd`, `PushHttpCmd`, `PullHttpCmd`) 위임
- 서비스/리모트 서비스 호출 및 결과 출력
- 사용법 출력

### Service
#### FileSystemInitService:
- 파일 시스템에 `.javaGit` 디렉토리 구조 생성
- `objects`, `refs/heads` 디렉토리 생성
- `HEAD`, `index` 파일 생성
#### InitService:
- 작업 디렉토리 기준으로 `FileSystemInitService`를 호출하여 저장소 초기화
#### AddService:
- 파일 경로 검증 및 실제 파일 시스템 검사
- 파일을 Blob으로 변환하여 `ObjectWriter`에 저장
- `IndexRepository`에 파일 경로와 SHA-1 해시 매핑 저장
#### CommitService:
- Index에서 스테이징된 파일들을 읽어 Tree/Commit 생성 및 저장
- 현재 브랜치의 HEAD를 새 Commit으로 갱신
- 커밋 후 Index 초기화
#### StatusService:
- 워킹 트리 / Index / HEAD 간 차이를 비교해 status 정보 생성
#### LogService:
- HEAD부터 부모 체인을 따라 커밋 로그 생성
#### BranchService / CheckoutService / MergeService:
- 브랜치 생성/조회, 안전한 브랜치 전환, Fast-Forward 기반 브랜치 병합 제공
#### PushService / PullService / CloneService:
- 로컬 디렉터리를 remote로 사용하는 push/pull/clone 기능 제공
#### HttpPushService / HttpPullService:
- HTTP 기반 원격 저장소와의 push/pull (Fast-Forward only) 구현

### Domain
#### Blob:
- Git Blob 객체 (파일 내용)
- byte[] 형태로 파일 내용 저장
- null 및 빈 파일 검증
- 방어적 복사로 불변성 보장
#### Tree:
- Git Tree 객체 (디렉터리 스냅샷)
- 파일/디렉터리 경로와 객체 OID 매핑 저장
#### Commit:
- Git Commit 객체
- tree OID, parent OID, author, message, timestamp 보유
#### Index:
- Git Index (스테이징 영역)
- 파일 경로와 SHA-1 해시의 매핑 저장
- Map<String, String> 형태 (path -> sha)
- null 및 빈 entry 검증
- 방어적 복사로 불변성 보장
#### Head:
- 현재 체크아웃된 브랜치 참조(예: `refs/heads/master`)

### Repository
#### ObjectWriter / ObjectReader:
- Blob/Tree/Commit 등 Git 객체를 `.javaGit/objects/{sha[0:2]}/{sha[2:]}` 구조로 읽고 쓰는 추상화
#### FileObjectWriter / FileObjectReader:
- 파일 시스템 기반 ObjectWriter/ObjectReader 구현
#### IndexRepository / FileIndexRepository:
- `.javaGit/index` 파일을 기반으로 Index를 읽고/쓰는 저장소
#### RefRepository / FileRefRepository:
- `.javaGit/HEAD`, `.javaGit/refs/heads/{branch}` 파일을 통해 브랜치 HEAD를 읽고/쓰는 저장소

### Exception
#### ErrorCode(enum):
- "[ERROR] ..." 형식의 표준화된 에러 메시지 정의
- Domain, Repository, Service 관련 에러 코드 정의
- message() 메서드로 에러 메시지 반환
