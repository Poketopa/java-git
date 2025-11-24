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
