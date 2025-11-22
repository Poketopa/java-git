package app.controller.command.handlers;

import app.service.CommitService;
import app.util.CommandLineParser;
import app.view.OutputView;

import java.util.Objects;

public final class CommitCmd {
    private static final String MESSAGE_OPTION = "-m";
    private static final String AUTHOR_OPTION = "-a";

    private final CommitService commitService;
    private final OutputView outputView;

    public CommitCmd(CommitService commitService, OutputView outputView) {
        this.commitService = Objects.requireNonNull(commitService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        String message = CommandLineParser.findOptionValue(args, MESSAGE_OPTION);
        String author = CommandLineParser.findOptionValue(args, AUTHOR_OPTION);
        if (message == null || message.isBlank() || author == null || author.isBlank()) {
            outputView.showCommitUsageError();
            return;
        }
        commitService.commit(message, author);
        outputView.showCommitCreated();
    }
}
