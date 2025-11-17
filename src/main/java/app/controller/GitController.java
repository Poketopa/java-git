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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// CLI 컨트롤러
// - 단일 명령 실행(run)과 인터랙티브 콘솔(runConsole) 제공
// - 명령 파싱 후 Service에 위임
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

    // 한 번에 하나의 명령을 실행
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

    // 인터랙티브 콘솔 (REPL)
    // - 'git' 프리픽스 강제
    // - help/usage, exit/quit 지원
    public void runConsole() {
        outputView.showWelcome();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                outputView.showPrompt();
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (equalsIgnoreCaseAny(line, "exit", "quit")) {
                    outputView.showBye();
                    break;
                }
                if (equalsIgnoreCaseAny(line, "help", "usage")) {
                    showUsage();
                    continue;
                }
                String[] parsedArgs = tokenize(line);
                if (parsedArgs.length == 0) {
                    continue;
                }
                if (!"git".equals(parsedArgs[0])) {
                    outputView.showRequireGitPrefix();
                    continue;
                }
                if (parsedArgs.length == 1) {
                    showUsage();
                    continue;
                }
                String[] commandArgs = Arrays.copyOfRange(parsedArgs, 1, parsedArgs.length);
                run(commandArgs);
            }
        } catch (IOException e) {
            outputView.showInputReadError(e.getMessage());
        }
    }

    private static boolean equalsIgnoreCaseAny(String input, String a, String b) {
        return input.equalsIgnoreCase(a) || input.equalsIgnoreCase(b);
    }

    // 간단한 토크나이저
    // - 공백 기준 분리
    // - 따옴표("..."/'...')로 감싼 구간은 하나의 토큰으로 취급
    private static String[] tokenize(String line) {
        // Splits by spaces but keeps quoted segments ("..."/'...') together
        Pattern tokenPattern = Pattern.compile("\"([^\"]*)\"|'([^']*)'|\\S+");
        Matcher matcher = tokenPattern.matcher(line);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            String token;
            if (matcher.group(1) != null) {
                token = matcher.group(1);
            } else if (matcher.group(2) != null) {
                token = matcher.group(2);
            } else {
                token = matcher.group();
            }
            tokens.add(token);
        }
        return tokens.toArray(new String[0]);
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



