package main.java.app.controller;

import main.java.app.service.AddService;
import main.java.app.service.CommitService;
import main.java.app.service.InitService;
import main.java.app.service.StatusService;
import main.java.app.service.LogService;
import main.java.app.service.BranchService;
import main.java.app.service.CheckoutService;
import main.java.app.service.MergeService;
import main.java.app.service.remote.fs.PushService;
import main.java.app.service.remote.fs.PullService;
import main.java.app.service.remote.fs.CloneService;
import main.java.app.service.remote.http.HttpPushService;
import main.java.app.service.remote.http.HttpPullService;
import main.java.app.controller.command.handlers.*;
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
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|\\S+");

    private final InitService initService;
    private final AddService addService;
    private final CommitService commitService;
    private final StatusService statusService;
    private final LogService logService;
    private final BranchService branchService;
    private final CheckoutService checkoutService;
    private final MergeService mergeService;
    private final OutputView outputView;
    private final PushService pushService;
    private final PullService pullService;
    private final CloneService cloneService;
    private final HttpPushService httpPushService;
    private final HttpPullService httpPullService;
    // Handlers
    private final InitCmd initCmd;
    private final AddCmd addCmd;
    private final CommitCmd commitCmd;
    private final StatusCmd statusCmd;
    private final LogCmd logCmd;
    private final BranchCmd branchCmd;
    private final CheckoutCmd checkoutCmd;
    private final MergeCmd mergeCmd;
    private final PushFsCmd pushFsCmd;
    private final PullFsCmd pullFsCmd;
    private final CloneFsCmd cloneFsCmd;
    private final ServeHttpCmd serveHttpCmd;
    private final PushHttpCmd pushHttpCmd;
    private final PullHttpCmd pullHttpCmd;

    public GitController(InitService initService, AddService addService, CommitService commitService, StatusService statusService, LogService logService, BranchService branchService, CheckoutService checkoutService, MergeService mergeService, PushService pushService, PullService pullService, CloneService cloneService, HttpPushService httpPushService, HttpPullService httpPullService, OutputView outputView) {
        this.initService = Objects.requireNonNull(initService, "initService");
        this.addService = Objects.requireNonNull(addService, "addService");
        this.commitService = Objects.requireNonNull(commitService, "commitService");
        this.statusService = Objects.requireNonNull(statusService, "statusService");
        this.logService = Objects.requireNonNull(logService, "logService");
        this.branchService = Objects.requireNonNull(branchService, "branchService");
        this.checkoutService = Objects.requireNonNull(checkoutService, "checkoutService");
        this.mergeService = Objects.requireNonNull(mergeService, "mergeService");
        this.pushService = Objects.requireNonNull(pushService, "pushService");
        this.pullService = Objects.requireNonNull(pullService, "pullService");
        this.cloneService = Objects.requireNonNull(cloneService, "cloneService");
        this.httpPushService = Objects.requireNonNull(httpPushService, "httpPushService");
        this.httpPullService = Objects.requireNonNull(httpPullService, "httpPullService");
        this.outputView = Objects.requireNonNull(outputView, "outputView");
        // init handlers
        this.initCmd = new InitCmd(initService, outputView);
        this.addCmd = new AddCmd(addService, outputView);
        this.commitCmd = new CommitCmd(commitService, outputView);
        this.statusCmd = new StatusCmd(statusService, outputView);
        this.logCmd = new LogCmd(logService, outputView);
        this.branchCmd = new BranchCmd(branchService, outputView);
        this.checkoutCmd = new CheckoutCmd(checkoutService, outputView);
        this.mergeCmd = new MergeCmd(mergeService, outputView);
        this.pushFsCmd = new PushFsCmd(pushService, outputView);
        this.pullFsCmd = new PullFsCmd(pullService, outputView);
        this.cloneFsCmd = new CloneFsCmd(cloneService, outputView);
        this.serveHttpCmd = new ServeHttpCmd(outputView);
        this.pushHttpCmd = new PushHttpCmd(httpPushService, outputView);
        this.pullHttpCmd = new PullHttpCmd(httpPullService, outputView);
    }

    // 한 번에 하나의 명령을 실행
    public void run(String[] args) {
        if (args == null || args.length == 0) {
            showUsage();
            return;
        }
        dispatch(args);
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
        Matcher matcher = TOKEN_PATTERN.matcher(line);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            String quotedDouble = matcher.group(1);
            if (quotedDouble != null) {
                tokens.add(quotedDouble);
                continue;
            }
            String quotedSingle = matcher.group(2);
            if (quotedSingle != null) {
                tokens.add(quotedSingle);
                continue;
            }
            tokens.add(matcher.group());
        }
        return tokens.toArray(new String[0]);
    }

    private void dispatch(String[] args) {
        String cmd = args[0];
        switch (cmd) {
            case "init" -> initCmd.execute(args);
            case "add" -> addCmd.execute(args);
            case "commit" -> commitCmd.execute(args);
            case "status" -> statusCmd.execute(args);
            case "log" -> logCmd.execute(args);
            case "branch" -> branchCmd.execute(args);
            case "checkout" -> checkoutCmd.execute(args);
            case "merge" -> mergeCmd.execute(args);
            case "push" -> pushFsCmd.execute(args);
            case "pull" -> pullFsCmd.execute(args);
            case "clone" -> cloneFsCmd.execute(args);
            case "serve-http" -> serveHttpCmd.execute(args);
            case "push-http" -> pushHttpCmd.execute(args);
            case "pull-http" -> pullHttpCmd.execute(args);
            default -> showUsage();
        }
    }

    private void showUsage() {
        outputView.showUsage();
    }
}



