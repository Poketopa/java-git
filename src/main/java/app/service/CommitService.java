package main.java.app.service;

import main.java.app.domain.Commit;
import main.java.app.domain.Index;
import main.java.app.domain.Tree;
import main.java.app.exception.ErrorCode;
import main.java.app.repository.IndexRepository;
import main.java.app.repository.ObjectWriter;
import main.java.app.repository.RefRepository;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public final class CommitServiceImpl implements CommitService {
    private final IndexRepository indexRepository;
    private final ObjectWriter objectWriter;
    private final RefRepository refRepository;
    private final Path rootDirectoryPath;

    public CommitServiceImpl(IndexRepository indexRepository, ObjectWriter objectWriter, RefRepository refRepository, Path rootDirectoryPath) {
        this.indexRepository = Objects.requireNonNull(indexRepository, "indexRepository");
        this.objectWriter = Objects.requireNonNull(objectWriter, "objectWriter");
        this.refRepository = Objects.requireNonNull(refRepository, "refRepository");
        this.rootDirectoryPath = Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
    }

    @Override
    public void commit(String message, String author) {
        validate(message, author);
        Index index = indexRepository.read();
        ensureHasStagedFiles(index);
        Tree tree = new Tree(index.stagedFiles());
        String treeSha = writeTree(tree);
        String parentSha = readCurrentHeadCommit();
        String commitSha = writeCommit(new Commit(message, treeSha, emptyToNull(parentSha), author));
        updateHead(commitSha);
        clearIndex();
    }

    private void validate(String message, String author) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.COMMIT_MESSAGE_EMPTY.message());
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.COMMIT_AUTHOR_EMPTY.message());
        }
    }

    private void ensureHasStagedFiles(Index index) {
        Map<String, String> staged = index.stagedFiles();
        if (staged == null || staged.isEmpty()) {
            throw new IllegalArgumentException("Nothing to commit");
        }
    }

    private String writeTree(Tree tree) {
        byte[] content = buildTreeContent(tree).getBytes(StandardCharsets.UTF_8);
        return objectWriter.write(content);
    }

    private String buildTreeContent(Tree tree) {
        StringBuilder contentBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : tree.entries().entrySet()) {
            appendTreeEntry(contentBuilder, entry);
        }
        return contentBuilder.toString();
    }

    private void appendTreeEntry(StringBuilder contentBuilder, Map.Entry<String, String> entry) {
        contentBuilder.append("blob ").append(entry.getValue()).append(' ').append(entry.getKey()).append('\n');
    }

    private String readCurrentHeadCommit() {
        String branch = refRepository.readCurrentBranch();
        return refRepository.readBranchHead(branch);
    }

    private String writeCommit(Commit commit) {
        byte[] content = buildCommitContent(commit).getBytes(StandardCharsets.UTF_8);
        return objectWriter.write(content);
    }

    private String buildCommitContent(Commit commit) {
        StringBuilder sb = new StringBuilder();
        sb.append("tree ").append(commit.treeOid()).append('\n');
        if (commit.parentOid() != null && !commit.parentOid().isBlank()) {
            sb.append("parent ").append(commit.parentOid()).append('\n');
        }
        sb.append("author ").append(commit.author()).append('\n');
        sb.append("date ").append(commit.createdAtMillis()).append('\n');
        sb.append('\n');
        sb.append(commit.message()).append('\n');
        return sb.toString();
    }

    private void updateHead(String commitSha) {
        String branch = refRepository.readCurrentBranch();
        refRepository.updateBranchHead(branch, commitSha);
    }

    private void clearIndex() {
        indexRepository.write(new Index(Map.of()));
    }

    private String emptyToNull(String s) {
        if (s == null) {
            return null;
        }
        if (s.isBlank()) {
            return null;
        }
        return s;
    }
}

