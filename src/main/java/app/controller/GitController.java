package main.java.app.controller;

import main.java.app.service.AddService;
import main.java.app.service.CommitService;
import main.java.app.service.InitService;
import main.java.app.service.StatusService;
import main.java.app.service.LogService;
import main.java.app.service.BranchService;
import main.java.app.service.CheckoutService;
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
    private final StatusService statusService;
    private final LogService logService;
    private final BranchService branchService;
    private final CheckoutService checkoutService;
    private final OutputView outputView;
    private final Map<String, Command> commandHandlers;

    public GitController(InitService initService, AddService addService, CommitService commitService, StatusService statusService, LogService logService, BranchService branchService, CheckoutService checkoutService, OutputView outputView) {
        this.initService = Objects.requireNonNull(initService, "initService");
        this.addService = Objects.requireNonNull(addService, "addService");
        this.commitService = Objects.requireNonNull(commitService, "commitService");
        this.statusService = Objects.requireNonNull(statusService, "statusService");
        this.logService = Objects.requireNonNull(logService, "logService");
        this.branchService = Objects.requireNonNull(branchService, "branchService");
        this.checkoutService = Objects.requireNonNull(checkoutService, "checkoutService");
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
        this.commandHandlers.put("log", args -> {
            java.util.List<LogService.LogEntry> entries = logService.list();
            outputView.showLog(entries);
        });
        this.commandHandlers.put("branch", args -> {
            if (args.length == 1) {
                outputView.showBranches(branchService.list());
                return;
            }
            if (args.length == 2) {
                try {
                    branchService.create(args[1]);
                    outputView.showBranchCreated(args[1]);
                } catch (IllegalArgumentException e) {
                    outputView.showBranchAlreadyExists(args[1]);
                }
                return;
            }
            showUsage();
        });
        this.commandHandlers.put("checkout", args -> {
            if (args.length != 2) {
                outputView.showCheckoutUsage();
                return;
            }
            String branch = args[1];
            CheckoutService.CheckoutResult result = checkoutService.switchBranch(branch);
            switch (result) {
                case SUCCESS -> outputView.showCheckoutSuccess(branch);
                case BRANCH_NOT_FOUND -> outputView.showCheckoutNotFound(branch);
                case WORKING_TREE_NOT_CLEAN -> outputView.showCheckoutDirty();
            }
        });
    }

    private void showUsage() {
        outputView.showUsage();
    }
}



