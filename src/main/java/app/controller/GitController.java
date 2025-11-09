package main.java.app.controller;

import main.java.app.application.AddUseCase;
import main.java.app.application.InitUseCase;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class GitController {
    private final InitUseCase initUseCase;
    private final AddUseCase addUseCase;

    public GitController(InitUseCase initUseCase, AddUseCase addUseCase) {
        this.initUseCase = Objects.requireNonNull(initUseCase, "initUseCase");
        this.addUseCase = Objects.requireNonNull(addUseCase, "addUseCase");
    }

    public void run(String[] args) {
        if (args == null || args.length == 0) {
            printUsage();
            return;
        }
        String cmd = args[0];
        switch (cmd) {
            case "init" -> {
                initUseCase.init();
                System.out.println("Initialized empty repository.");
            }
            case "add" -> {
                if (args.length < 2) {
                    System.err.println("Nothing specified, nothing added.");
                    return;
                }
                List<String> paths = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
                addUseCase.add(paths);
                System.out.println("Added " + paths.size() + " path(s) to index.");
            }
            default -> printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  git init");
        System.out.println("  git add <path> [<path>...]");
    }
}


