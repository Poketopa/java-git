package main.java.app.service;

import main.java.app.exception.ErrorCode;
import main.java.app.repository.ObjectReader;
import main.java.app.repository.RefRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LogService {
    public record LogEntry(String oid, String author, String dateTimeIso, String message) { }

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());

    private final RefRepository refRepository;
    private final ObjectReader objectReader;

    public LogService(RefRepository refRepository, ObjectReader objectReader) {
        this.refRepository = Objects.requireNonNull(refRepository, "refRepository");
        this.objectReader = Objects.requireNonNull(objectReader, "objectReader");
    }

    public List<LogEntry> list() {
        String branch = refRepository.readCurrentBranch();
        String oid = refRepository.readBranchHead(branch);
        List<LogEntry> entries = new ArrayList<>();
        while (oid != null && !oid.isBlank()) {
            CommitParsed parsed = parseCommitContent(objectReader.readRaw(oid));
            String dateIso = parsed.dateMillis != null ? ISO_FORMATTER.format(Instant.ofEpochMilli(parsed.dateMillis)) : "";
            String messageFirstLine = firstLine(parsed.message);
            entries.add(new LogEntry(oid, parsed.author, dateIso, messageFirstLine));
            oid = parsed.parentOid;
        }
        return List.copyOf(entries);
    }

    private String firstLine(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }
        int idx = message.indexOf('\n');
        if (idx < 0) {
            return message;
        }
        return message.substring(0, idx);
    }

    private static final class CommitParsed {
        final String treeOid;
        final String parentOid;
        final String author;
        final Long dateMillis;
        final String message;

        CommitParsed(String treeOid, String parentOid, String author, Long dateMillis, String message) {
            this.treeOid = treeOid;
            this.parentOid = parentOid;
            this.author = author;
            this.dateMillis = dateMillis;
            this.message = message;
        }
    }

    private CommitParsed parseCommitContent(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException(ErrorCode.MALFORMED_COMMIT_OBJECT.message());
        }
        String content = new String(bytes, StandardCharsets.UTF_8);
        String[] lines = content.split("\n");
        String treeOid = null;
        String parentOid = null;
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
                treeOid = line.substring("tree ".length()).trim();
                continue;
            }
            if (line.startsWith("parent ")) {
                parentOid = line.substring("parent ".length()).trim();
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
        if (treeOid == null || author == null) {
            throw new IllegalArgumentException(ErrorCode.MALFORMED_COMMIT_OBJECT.message());
        }
        return new CommitParsed(treeOid, parentOid, author, dateMillis, messageBuilder.toString());
    }
}


