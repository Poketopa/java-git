package main.java.app.controller;

import main.java.app.service.AddService;
import main.java.app.service.CommitService;
import main.java.app.service.InitService;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class GitController {
    private final InitService initService;
    private final AddService addService;
    private final CommitService commitService;

    public GitController(InitService initService, AddService addService, CommitService commitService) {
        this.initService = Objects.requireNonNull(initService, "initService");
        this.addService = Objects.requireNonNull(addService, "addService");
        this.commitService = Objects.requireNonNull(commitService, "commitService");
    }

    public void run(String[] args) {
        if (args == null || args.length == 0) {
            printUsage();
            return;
        }
        String command = args[0];
        switch (command) {
            case "init" -> {
                initService.init();
                System.out.println("Initialized empty repository.");
            }
            case "add" -> {
                if (args.length < 2) {
                    System.err.println("Nothing specified, nothing added.");
                    return;
                }
                List<String> filePaths = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
                addService.add(filePaths);
                System.out.println("Added " + filePaths.size() + " path(s) to index.");
            }
            case "commit" -> {
                String message = findOptionValue(args, "-m");
                String author = findOptionValue(args, "-a");
                if (message == null || message.isBlank() || author == null || author.isBlank()) {
                    System.err.println("Usage: git commit -m <message> -a <author>");
                    return;
                }
                commitService.commit(message, author);
                System.out.println("Created commit.");
            }
            default -> printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  git init");
        System.out.println("  git add <path> [<path>...]");
        System.out.println("  git commit -m <message> -a <author>");
    }

    private static String findOptionValue(String[] args, String option) {
        if (args == null || option == null) {
            return null;
        }
        for (int i = 1; i < args.length - 1; i++) {
            if (option.equals(args[i])) {
                return args[i + 1];
            }
        }
        return null;
    }
}



