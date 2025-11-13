package main.java.app.service;

import main.java.app.exception.ErrorCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class FileSystemInitService {
    private static final String DOT_JAVA_GIT = ".javaGit";
    private static final String OBJECTS = "objects";
    private static final String REFS = "refs";
    private static final String HEADS = "heads";
    private static final String HEAD = "HEAD";
    private static final String INDEX = "index";
    private static final String MASTER = "master";
    private static final String MASTER_REF = "ref: refs/heads/master\n";

    public void initRepository(Path rootDirectoryPath) {
        Objects.requireNonNull(rootDirectoryPath, "rootDirectoryPath");
        Path jgitDirectoryPath = rootDirectoryPath.resolve(DOT_JAVA_GIT);
        createRepositoryDirectories(jgitDirectoryPath);
        createRepositoryFiles(jgitDirectoryPath);
    }

    private void createRepositoryDirectories(Path jgitDirectoryPath) {
        Path objectsDirectoryPath = jgitDirectoryPath.resolve(OBJECTS);
        Path refsHeadsDirectoryPath = jgitDirectoryPath.resolve(REFS).resolve(HEADS);
        try {
            Files.createDirectories(objectsDirectoryPath);
            Files.createDirectories(refsHeadsDirectoryPath);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.REPOSITORY_INIT_FAILED.message());
        }
    }

    private void createRepositoryFiles(Path jgitDirectoryPath) {
        createHeadFile(jgitDirectoryPath);
        createMasterBranchFile(jgitDirectoryPath);
        createIndexFile(jgitDirectoryPath);
    }

    private void createHeadFile(Path jgitDirectoryPath) {
        Path headFilePath = jgitDirectoryPath.resolve(HEAD);
        if (Files.exists(headFilePath)) {
            return;
        }
        try {
            Files.writeString(headFilePath, MASTER_REF, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.REPOSITORY_INIT_FAILED.message());
        }
    }

    private void createMasterBranchFile(Path jgitDirectoryPath) {
        Path masterBranchFilePath = jgitDirectoryPath.resolve(REFS).resolve(HEADS).resolve(MASTER);
        if (Files.exists(masterBranchFilePath)) {
            return;
        }
        try {
            Files.writeString(masterBranchFilePath, "", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.REPOSITORY_INIT_FAILED.message());
        }
    }

    private void createIndexFile(Path jgitDirectoryPath) {
        Path indexFilePath = jgitDirectoryPath.resolve(INDEX);
        if (Files.exists(indexFilePath)) {
            return;
        }
        try {
            Files.writeString(indexFilePath, "", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ErrorCode.REPOSITORY_INIT_FAILED.message());
        }
    }
}

