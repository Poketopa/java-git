package main.java.app.controller.command.handlers;

import main.java.app.service.CommitService;
import main.java.app.util.CommandLineParser;
import main.java.app.view.OutputView;

import java.util.Objects;

public final class CommitCmd {
    private final CommitService commitService;
    private final OutputView outputView;

    public CommitCmd(CommitService commitService, OutputView outputView) {
        this.commitService = Objects.requireNonNull(commitService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        String message = CommandLineParser.findOptionValue(args, "-m");
        String author = CommandLineParser.findOptionValue(args, "-a");
        if (message == null || message.isBlank() || author == null || author.isBlank()) {
            outputView.showCommitUsageError();
            return;
        }
        commitService.commit(message, author);
        outputView.showCommitCreated();
    }
}


