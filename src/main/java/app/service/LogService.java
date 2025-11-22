package app.service;

import app.exception.ErrorCode;
import app.repository.ObjectReader;
import app.repository.RefRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// 커밋 로그 조회
// - HEAD에서 부모 체인을 따라가며 요약 정보(hash/author/date/message) 생성
// - Commit raw bytes를 파싱하여 필요한 헤더/본문만 사용
public final class LogService {
    public record LogEntry(String hash, String author, String dateTimeIso, String message) { }

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());

    private final RefRepository refRepository;
    private final ObjectReader objectReader;

    public LogService(RefRepository refRepository, ObjectReader objectReader) {
        this.refRepository = Objects.requireNonNull(refRepository, "refRepository");
        this.objectReader = Objects.requireNonNull(objectReader, "objectReader");
    }

    public List<LogEntry> list() {
        // 1) 현재 브랜치 HEAD 커밋부터 시작
        String branch = refRepository.readCurrentBranch();
        String commitHash = refRepository.readBranchHead(branch);
        List<LogEntry> entries = new ArrayList<>();
        // 2) 부모를 따라가며 로그 누적
        while (commitHash != null && !commitHash.isBlank()) {
            CommitParsed parsed = parseCommitContent(objectReader.readRaw(commitHash));
            String dateIso = parsed.dateMillis != null ? ISO_FORMATTER.format(Instant.ofEpochMilli(parsed.dateMillis)) : "";
            String messageFirstLine = firstLine(parsed.message);
            entries.add(new LogEntry(commitHash, parsed.author, dateIso, messageFirstLine));
            commitHash = parsed.parentHash;
        }
        return List.copyOf(entries);
    }

    // 메시지 첫 줄만 추출 (git log 요약 형태)
    private String firstLine(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }
        int index = message.indexOf('\n');
        if (index < 0) {
            return message;
        }
        return message.substring(0, index);
    }

    private static final class CommitParsed {
        final String treeSha;
        final String parentHash;
        final String author;
        final Long dateMillis;
        final String message;

        CommitParsed(String treeSha, String parentHash, String author, Long dateMillis, String message) {
            this.treeSha = treeSha;
            this.parentHash = parentHash;
            this.author = author;
            this.dateMillis = dateMillis;
            this.message = message;
        }
    }

    // Commit 직렬화 포맷을 읽어 필요한 필드만 파싱
    private CommitParsed parseCommitContent(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException(ErrorCode.MALFORMED_COMMIT_OBJECT.message());
        }
        String content = new String(bytes, StandardCharsets.UTF_8);
        String[] lines = content.split("\n");
        String treeSha = null;
        String parentHash = null;
        String author = null;
        Long dateMillis = null;
        int i = 0;
        for (; i < lines.length; i++) {
            String line = lines[i];
            if (line == null || line.isBlank()) {
                i++;
                break;
            }
            if (line.startsWith("tree ")) {
                treeSha = line.substring("tree ".length()).trim();
                continue;
            }
            if (line.startsWith("parent ")) {
                parentHash = line.substring("parent ".length()).trim();
                continue;
            }
            if (line.startsWith("author ")) {
                author = line.substring("author ".length()).trim();
                continue;
            }
            if (line.startsWith("date ")) {
                try {
                    dateMillis = Long.parseLong(line.substring("date ".length()).trim());
                } catch (NumberFormatException e) {
                    dateMillis = null;
                }
            }
        }
        StringBuilder messageBuilder = new StringBuilder();
        for (; i < lines.length; i++) {
            messageBuilder.append(lines[i]);
            if (i < lines.length - 1) {
                messageBuilder.append('\n');
            }
        }
        if (treeSha == null || author == null) {
            throw new IllegalArgumentException(ErrorCode.MALFORMED_COMMIT_OBJECT.message());
        }
        return new CommitParsed(treeSha, parentHash, author, dateMillis, messageBuilder.toString());
    }
}


