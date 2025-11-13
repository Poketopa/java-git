package main.java.app.controller;

import main.java.app.service.AddService;
import main.java.app.service.CommitService;
import main.java.app.service.InitService;
import main.java.app.view.Messages;
import main.java.app.util.CommandLineParser;
import main.java.app.view.OutputView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class GitController {
    private interface Command {
        void execute(String[] args);
    }

    private final InitService initService;
    private final AddService addService;
    private final CommitService commitService;
    private final OutputView outputView;
    private final Map<String, Command> commandHandlers;

    public GitController(InitService initService, AddService addService, CommitService commitService) {
        this(initService, addService, commitService, null);
    }

    public GitController(InitService initService, AddService addService, CommitService commitService, OutputView outputView) {
        this.initService = Objects.requireNonNull(initService, "initService");
        this.addService = Objects.requireNonNull(addService, "addService");
        this.commitService = Objects.requireNonNull(commitService, "commitService");
        this.outputView = outputView == null ? null : outputView;

        this.commandHandlers = new HashMap<>();
        this.commandHandlers.put("init", args -> {
            initService.init();
            if (this.outputView != null) {
                this.outputView.showInitSuccess();
                return;
            }
            System.out.println(Messages.INIT_SUCCESS);
        });
        this.commandHandlers.put("add", args -> {
            List<String> filePaths = CommandLineParser.extractPaths(args);
            if (filePaths.isEmpty()) {
                if (this.outputView != null) {
                    this.outputView.showAddMissingPath();
                    return;
                }
                System.err.println(Messages.ADD_MISSING_PATH);
                return;
            }
            addService.add(filePaths);
            if (this.outputView != null) {
                this.outputView.showAddedToIndex(filePaths.size());
                return;
            }
            System.out.printf(Messages.ADDED_TO_INDEX + "%n", filePaths.size());
        });
        this.commandHandlers.put("commit", args -> {
            String message = CommandLineParser.findOptionValue(args, "-m");
            String author = CommandLineParser.findOptionValue(args, "-a");
            if (message == null || message.isBlank() || author == null || author.isBlank()) {
                if (this.outputView != null) {
                    this.outputView.showCommitUsageError();
                    return;
                }
                System.err.println(Messages.COMMIT_USAGE_ERROR);
                return;
            }
            commitService.commit(message, author);
            if (this.outputView != null) {
                this.outputView.showCommitCreated();
                return;
            }
            System.out.println(Messages.COMMIT_CREATED);
        });
    }

    public void run(String[] args) {
        if (args == null || args.length == 0) {
            if (this.outputView != null) {
                this.outputView.showUsage();
            } else {
                System.out.println(Messages.USAGE_HEADER);
                System.out.println(Messages.USAGE_INIT);
                System.out.println(Messages.USAGE_ADD);
                System.out.println(Messages.USAGE_COMMIT);
            }
            return;
        }
        Command handler = commandHandlers.get(args[0]);
        if (handler == null) {
            if (this.outputView != null) {
                this.outputView.showUsage();
            } else {
                System.out.println(Messages.USAGE_HEADER);
                System.out.println(Messages.USAGE_INIT);
                System.out.println(Messages.USAGE_ADD);
                System.out.println(Messages.USAGE_COMMIT);
            }
            return;
        }
        handler.execute(args);
    }

}



