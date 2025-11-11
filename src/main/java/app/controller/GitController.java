package app.controller;

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
        String cmd = args[0];
        if ("init".equals(cmd)) {
            initService.init();
            System.out.println("Initialized empty repository.");
            return;
        }
        if ("add".equals(cmd)) {
            if (args.length < 2) {
                System.err.println("Nothing specified, nothing added.");
                return;
            }
            List<String> paths = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
            addService.add(paths);
            System.out.println("Added " + paths.size() + " path(s) to index.");
            return;
        }
        if ("commit".equals(cmd)) {
            String message = parseCommitMessage(args);
            if (message == null) {
                System.err.println("commit message is required. usage: git commit -m \"message\"");
                return;
            }
            String author = System.getProperty("user.name", "anonymous");
            commitService.commit(message, author);
            System.out.println("Committed: " + message);
            return;
        }
        printUsage();
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  git init");
        System.out.println("  git add <path> [<path>...]");
        System.out.println("  git commit -m \"message\"");
    }

    private static String parseCommitMessage(String[] args) {
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if ("-m".equals(arg) && i + 1 < args.length) {
                return args[i + 1];
            }
        }
        return null;
    }
}


