# 자바 깃허브 구현

## 구현 목표
- 로컬에서 CLI로 동작하는 git을 순수 Java로 구현한다.
- Git의 내부 구조를 이해하고 구현
- `.jgit` 디렉토리에 Git 메타데이터 저장
- 실제 Git과 호환 가능하도록 구현 목표
- Clean Architecture 원칙 준수
- Hexagonal Architecture 패턴 적용 (Port & Adapter)
- 불변 객체 및 방어적 복사로 안전성 보장

## 기능 목록

### 메인 로직
- [x] 저장소 초기화 기능 (git init)
  - [x] `.jgit` 디렉토리 생성
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
  - [x] Blob 객체를 `.jgit/objects` 디렉토리에 저장
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
- [ ] git commit
- [ ] git push
- [ ] git pull
- [ ] git status
- [ ] git log
- [ ] git branch
- [ ] git checkout
- [ ] git merge
- [ ] git clone

## 기능 구현 현황

### 필요 객체 구현
- [ ] Object 객체 구현
- [x] Blob 객체 구현
- [ ] Tree 객체 구현
- [ ] Commit 객체 구현
- [x] Index 객체 구현
- [ ] Head 객체 구현

### 명령어 구현
- [x] init: 저장소 초기화 (.jgit 디렉토리 생성)
- [x] add: 파일을 스테이징 영역에 추가
- [ ] commit: 스테이징된 파일들을 커밋
- [ ] status: 현재 상태 확인
- [ ] log: 커밋 히스토리 출력
- [ ] branch: 브랜치 생성/조회
- [ ] checkout: 브랜치 전환
- [ ] merge: 브랜치 병합
- [ ] push: 원격 저장소에 푸시
- [ ] pull: 원격 저장소에서 풀
- [ ] clone: 원격 저장소 클론

### 입력 및 출력
- [x] CLI 명령어 파싱
- [x] 기본 출력 메시지
- [ ] 상세한 상태 출력 (status)
- [ ] 커밋 로그 포맷팅 (log)

### 예외처리
- [x] ErrorCode enum 구현
- [x] 커스텀 예외 처리 (IllegalArgumentException)
- [x] Domain 객체 검증 (Blob, Index)
- [x] Repository 예외 처리
- [x] Service 예외 처리

## 프로젝트 구조

### Application
#### Main:
- 프로그램 시작점
- 현재 작업 디렉토리 경로 획득
- Appconfig로 객체 생성 후 GitController.run() 실행

### Config
#### Appconfig:
- 수동 DI 구성
- Repository 및 Service 인스턴스 생성/주입
- GitController 생성 및 의존성 주입

### Controller
#### GitController:
- CLI 명령어 파싱 및 처리
- init, add 명령어 처리
- 서비스 호출 및 결과 출력
- 사용법 출력

### Service
#### InitService:
- 저장소 초기화 인터페이스
#### InitServiceImpl:
- RepositoryInitializer를 사용하여 저장소 초기화
#### FileSystemInitService:
- 파일 시스템에 `.jgit` 디렉토리 구조 생성
- objects, refs/heads 디렉토리 생성
- HEAD, index 파일 생성
#### AddService:
- 파일 스테이징 인터페이스
#### AddServiceImpl:
- 파일 경로 검증
- 파일을 Blob으로 변환하여 ObjectRepository에 저장
- Index에 파일 경로와 SHA-1 해시 매핑 저장
#### RepositoryInitializer:
- 저장소 초기화 전략 인터페이스

### Domain
#### Blob:
- Git Blob 객체 (파일 내용)
- byte[] 형태로 파일 내용 저장
- null 및 빈 파일 검증
- 방어적 복사로 불변성 보장
#### Index:
- Git Index (스테이징 영역)
- 파일 경로와 SHA-1 해시의 매핑 저장
- Map<String, String> 형태 (path -> sha)
- null 및 빈 entry 검증
- 방어적 복사로 불변성 보장

### Repository
#### ObjectRepository:
- Blob 객체 저장소 인터페이스
- writeObject(Blob): SHA-1 해시 반환
#### FileObjectRepository:
- 파일 시스템 기반 Blob 저장소 구현
- `.jgit/objects/{sha[0:2]}/{sha[2:]}` 경로에 저장
- SHA-1 해시 계산 및 저장
#### IndexRepository:
- Index 저장소 인터페이스
- read(): Index 읽기
- write(Index): Index 쓰기
#### FileIndexRepository:
- 파일 시스템 기반 Index 저장소 구현
- `.jgit/index` 파일에 저장
- 파일 형식: `{sha} {path}\n`
- 파일 파싱 및 직렬화

### Exception
#### ErrorCode(enum):
- "[ERROR] ..." 형식의 표준화된 에러 메시지 정의
- Domain, Repository, Service 관련 에러 코드 정의
- message() 메서드로 에러 메시지 반환
