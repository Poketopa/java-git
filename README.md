## Java Git - 순수 Java로 구현한 Git

순수 Java로 Git의 핵심 개념(Blob / Tree / Commit / Ref / Index)을 직접 구현한 **CLI Git 클론 프로젝트**입니다.  
실제 Git의 `.git` 디렉터리 대신 **`.javaGit` 디렉터리**를 사용하여, 객체 저장 구조와 브랜치/HEAD 관리 방식을 최대한 그대로 재현하는 것을 목표로 하였습니다.

---

## 주요 특징 

- **순수 Java 기반 Git 내부 구조 구현**
  - **Blob / Tree / Commit / Index / Head** 를 별도 도메인 객체로 정의하고, 각 객체를 SHA-1 해시 기반으로 `.javaGit/objects`에 저장합니다.
  - 모든 도메인은 **불변(Immutable)** 을 지향하며, 생성 시점에 검증을 수행합니다.

- **파일 시스템 기반 Git 저장소**
  - 실제 Git의 `.git` 과 유사한 구조를 **`.javaGit`** 으로 구현합니다.
  - `objects`, `refs/heads`, `HEAD`, `index` 파일을 직접 생성/관리합니다.

- **계층형 아키텍처 (Layered Architecture)**
  - **Application / Controller / Service / Repository / Domain / View** 로 레이어를 분리하여, Git의 동작을 책임 단위로 나눠 이해할 수 있게 설계했습니다.

- **로컬 디렉터리 및 HTTP 기반 Remote 지원**
  - 로컬 디렉터리를 remote 처럼 사용하는 **`git push` / `git pull` / `git clone`** 을 지원합니다.
  - HTTP 서버를 띄우고, HTTP API를 통해 **`git push-http` / `git pull-http` (Fast-Forward only)** 를 수행할 수 있습니다.

- **에러 포맷과 도메인 검증**
  - `[ERROR] ...` 형식의 **`ErrorCode` enum** 으로 에러 메시지를 일관되게 관리합니다.
  - Domain/Service/Repository 각 계층에서 **명시적인 예외 처리**를 수행합니다.

---

## 지원 명령어

### 기본 저장소 관리

- **`git init`**
  - 현재 디렉터리를 기준으로 `.javaGit` 저장소를 초기화합니다.
  - `objects`, `refs/heads`, `HEAD`, `index` 파일을 생성합니다.

- **`git add <path>...`**
  - 인자로 전달된 파일들을 읽어 **Blob** 으로 저장하고, 그 해시를 `.javaGit/index` 에 기록합니다.
  - Git의 **스테이징 영역(Index)** 을 그대로 재현합니다.

- **`git commit`**
  - Index 에 스테이징된 파일들을 기반으로 **Tree → Commit** 을 생성하고, 현재 브랜치 HEAD 를 새 커밋으로 이동시킵니다.

### 상태 조회 및 히스토리

- **`git status`**
  - 워킹 트리, Index, HEAD 커밋을 비교하여 **추가/수정/삭제/Untracked 파일** 상태를 계산해 출력합니다.

- **`git log`**
  - HEAD 커밋부터 부모 체인을 따라가며 **커밋 히스토리**를 시간 순서대로 출력합니다.

### 브랜치 및 병합

- **`git branch`**
  - 새 브랜치를 생성하거나, 존재하는 브랜치 목록을 조회합니다.

- **`git checkout`**
  - 브랜치를 전환합니다.
  - 전환 전 `StatusService` 로 워킹 트리가 깨끗한지 검증한 뒤, HEAD 및 브랜치 ref 를 업데이트합니다.

- **`git merge` (Fast-Forward only)**
  - **Fast-Forward 가능한 경우에만** 병합을 수행합니다.
  - 현재 브랜치 HEAD 가 대상 브랜치의 조상인 경우, 단순히 HEAD 를 대상 브랜치 최신 커밋으로 이동시키는 방식으로 동작합니다.

### 로컬 디렉터리 기반 Remote

- **`git push` / `git pull` / `git clone`**
  - 별도의 로컬 디렉터리를 **remote 저장소처럼 간주**하여 `.javaGit` 메타데이터(객체, refs)를 복사하는 방식으로 동기화를 수행합니다.
  - `git clone` 은 `.javaGit` 메타데이터만 복제하며, 워킹 트리 checkout 은 이후에 수행합니다.

### HTTP 기반 Remote

- **`git serve-http`**
  - HTTP 서버를 띄워, 다른 클라이언트가 HTTP API 를 통해 객체/refs 를 주고받을 수 있도록 합니다.

- **`git push-http` / `git pull-http` (Fast-Forward only)**
  - HTTP 프로토콜 위에서 push/pull 을 수행하며, 이때도 **Fast-Forward만 허용**합니다.

---

## 실행 방법

### 요구 사항

- **Java 21 이상**
- **Gradle** (wrapper 포함)

### 빌드

- **JAR 빌드**
  - `./gradlew clean build`
  - 결과물: `build/libs/java-git-0.1.0.jar`

### 실행

- **단일 명령 실행**
  - `java -jar build/libs/java-git-0.1.0.jar git <command> [options]`
  - 예시:
    - `java -jar build/libs/java-git-0.1.0.jar git init`
    - `java -jar build/libs/java-git-0.1.0.jar git add src/Main.java`
    - `java -jar build/libs/java-git-0.1.0.0.jar git commit -m "Initial commit"`

- **인터랙티브 모드**
  - `java -jar build/libs/java-git-0.1.0.jar`
  - 프롬프트에서 계속해서 `git ...` 명령을 입력할 수 있습니다.
  - **`exit` / `quit`** 으로 종료, **`help` / `usage`** 로 사용법을 확인할 수 있습니다.

### 인자 파싱 규칙

- 공백이 포함된 경로나 메시지는 **따옴표로 감싸서** 사용할 수 있습니다.
  - 예: `git commit -m "fix: handle merge conflict properly"`
  - 예: `git add "src/main/java/My Class.java"`

---

## 아키텍처 개요 

### Application 계층

- **`Main`**
  - 프로그램 진입점으로, 현재 작업 디렉터리(`user.dir`)를 구해 `Appconfig` 에 전달합니다.
  - 전달된 인자가 있으면 **단일 명령 모드**, 없으면 **인터랙티브 모드**를 실행합니다.

- **`Appconfig`**
  - 수동 DI 컨테이너 역할을 하며, `GitController`, Service, Repository, Remote Service 인스턴스를 조립합니다.

### Controller / View 계층

- **`GitController`**
  - CLI 입력을 받아 정규식 기반 토크나이징을 수행하고, 명령어에 따라 적절한 **Command Handler** 로 분기합니다.
  - `runConsole()` 을 통해 인터랙티브 모드를 지원합니다.

- **Command Handlers (`command.handlers.*`)**
  - `InitCmd`, `AddCmd`, `CommitCmd`, `BranchCmd`, `CheckoutCmd`, `MergeCmd`, `StatusCmd`, `LogCmd`,  
    `PushFsCmd`, `PullFsCmd`, `CloneFsCmd`, `ServeHttpCmd`, `PushHttpCmd`, `PullHttpCmd` 등
  - 각 Git 명령어에 대응하는 **얇은 어댑터 레이어**로, 입력 파싱과 Service 호출을 담당합니다.

- **`OutputView`**
  - 모든 출력 메시지(성공/실패/상태/로그)를 담당하는 View 레이어입니다.

### Service 계층

- Git 비즈니스 로직을 담당하는 계층으로, Repository/Domain 을 조합하여 실제 동작을 구현합니다.
- 주요 서비스:
  - **`FileSystemInitService`**: `.javaGit` 디렉터리 구조, `HEAD`, `refs/heads/master`, `index` 파일을 초기화합니다.
  - **`InitService`**: 현재 작업 디렉터리를 기준으로 저장소를 초기화합니다.
  - **`AddService`**: 파일을 Blob 으로 변환해 저장하고, Index 를 갱신합니다.
  - **`CommitService`**: Index → Tree → Commit 생성, 현재 브랜치 HEAD 를 새 커밋으로 이동시킵니다.
  - **`StatusService`**: 워킹 트리 / Index / HEAD Tree 를 비교해 상태를 계산합니다.
  - **`LogService`**: HEAD 에서 시작해 부모 체인을 따라 커밋 로그를 생성합니다.
  - **`BranchService` / `CheckoutService` / `MergeService`**: 브랜치 생성/조회/전환 및 Fast-Forward 병합을 제공합니다.
  - **`PushService` / `PullService` / `CloneService`**: 파일 시스템 기반 remote 와의 동기화를 담당합니다.
  - **`HttpPushService` / `HttpPullService`**: HTTP 기반 remote 와의 Fast-Forward push/pull 을 구현합니다.

### Repository / Remote 계층

- **`FileObjectWriter` / `FileObjectReader`**
  - `.javaGit/objects/{sha[0:2]}/{sha[2:]}` 형태로 Git 객체를 저장/조회합니다.

- **`FileIndexRepository`**
  - `.javaGit/index` 파일을 읽고/쓰며, `Index` 도메인과 상호 변환합니다.

- **`FileRefRepository`**
  - `.javaGit/HEAD`, `.javaGit/refs/heads/{branch}` 파일을 통해 브랜치 HEAD 를 관리합니다.

- **Remote**
  - **`FileRemoteClient`**: 로컬 디렉터리 remote 와의 통신을 담당합니다.
  - **`HttpRemoteClient` / `HttpRemoteServer`**: HTTP 기반 remote 와의 통신을 담당합니다.

### Domain 계층

- Git 의 핵심 개념을 표현하는 **불변 도메인 객체들**입니다.
  - **`Blob`**: 파일 내용을 `byte[]` 로 보관하며, null/빈 배열을 허용하지 않고 방어적 복사를 수행합니다.
  - **`Tree`**: 디렉터리 스냅샷을 `Map<String, String>` (`path → objectId`) 구조로 표현합니다.
  - **`Commit`**: `message`, `treeOid`, `parentOid`, `author`, `createdAtMillis` 를 보유합니다.
  - **`Index`**: 스테이징 영역을 `Map<String, String>` (`path → blob SHA`) 로 표현합니다.
  - **`Head`**: 현재 체크아웃된 브랜치 참조(`refs/heads/master` 등)를 나타냅니다.

---

## .javaGit 저장 구조

- **`.javaGit/objects/aa/bb...`**: SHA-1 해시를 앞 2글자/나머지로 나눠 객체 파일을 저장합니다.
- **`.javaGit/index`**: `<sha> <path>` 텍스트 형식으로 Index 상태를 보관합니다.
- **`.javaGit/HEAD`**: `ref: refs/heads/master` 와 같은 현재 브랜치 참조를 저장합니다.
- **`.javaGit/refs/heads/<branch>`**: 브랜치 HEAD 커밋 SHA 를 담는 파일입니다.

이 구조를 통해 실제 Git 과 유사한 방식으로 객체 스토리지와 브랜치 관리를 경험할 수 있습니다.

---

## 아래 내용은 개발 과정에서 기능 구현 현황과 구조를 정리해 둔 README 기록입니다.

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

### 필요 객체 구현
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
- [x] serve-http: HTTP 원격 서버 실행
- [x] push-http: HTTP 원격 저장소에 푸시 (Fast-Forward only)
- [x] pull-http: HTTP 원격 저장소에서 풀 (Fast-Forward only)

## 입력 및 출력
- [x] CLI 명령어 파싱
- [x] 기본 출력 메시지
- [x] 상세한 상태 출력 (status)
- [x] 커밋 로그 포맷팅 (log)

## 예외처리
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
