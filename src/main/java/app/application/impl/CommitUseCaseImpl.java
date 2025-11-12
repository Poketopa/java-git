package main.java.app.application.impl;

import main.java.app.application.CommitUseCase;
import main.java.app.service.CommitService;

import java.util.Objects;

public final class CommitUseCaseImpl implements CommitUseCase {
    private final CommitService commitService;

    public CommitUseCaseImpl(CommitService commitService) {
        this.commitService = Objects.requireNonNull(commitService, "commitService");
    }

    @Override
    public void commit(String message, String author) {
        commitService.commit(message, author);
    }
}


