package main.java.app.controller;

import main.java.app.service.AddService;
import main.java.app.service.CommitService;
import main.java.app.service.InitService;
import main.java.app.service.StatusService;
import main.java.app.util.CommandLineParser;
import main.java.app.view.ConsoleOutputView;

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
    private final StatusService statusService;
    private final ConsoleOutputView outputView;
    private final Map<String, Command> commandHandlers;

    public GitController(InitService initService, AddService addService, CommitService commitService, StatusService statusService, ConsoleOutputView outputView) {
        this.initService = Objects.requireNonNull(initService, "initService");
        this.addService = Objects.requireNonNull(addService, "addService");
        this.commitService = Objects.requireNonNull(commitService, "commitService");
        this.statusService = Objects.requireNonNull(statusService, "statusService");
        this.outputView = Objects.requireNonNull(outputView, "outputView");
        this.commandHandlers = new HashMap<>();
        registerCommandHandlers();
    }

    public void run(String[] args) {
        if (args == null || args.length == 0) {
            showUsage();
            return;
        }
        Command handler = commandHandlers.get(args[0]);
        if (handler == null) {
            showUsage();
            return;
        }
        handler.execute(args);
    }

    private void registerCommandHandlers() {
        this.commandHandlers.put("init", args -> {
            initService.init();
            outputView.showInitSuccess();
        });
        this.commandHandlers.put("add", args -> {
            List<String> filePaths = CommandLineParser.extractPaths(args);
            if (filePaths.isEmpty()) {
                outputView.showAddMissingPath();
                return;
            }
            addService.add(filePaths);
            outputView.showAddedToIndex(filePaths.size());
        });
        this.commandHandlers.put("commit", args -> {
            String message = CommandLineParser.findOptionValue(args, "-m");
            String author = CommandLineParser.findOptionValue(args, "-a");
            if (message == null || message.isBlank() || author == null || author.isBlank()) {
                outputView.showCommitUsageError();
                return;
            }
            commitService.commit(message, author);
            outputView.showCommitCreated();
        });
        this.commandHandlers.put("status", args -> {
            StatusService.StatusResult result = statusService.status();
            outputView.showStatus(result);
        });
    }

    private void showUsage() {
        outputView.showUsage();
    }
}



