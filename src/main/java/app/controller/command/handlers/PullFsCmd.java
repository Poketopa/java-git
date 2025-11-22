package main.java.app.controller.command.handlers;

import main.java.app.service.remote.fs.PullService;
import main.java.app.view.OutputView;

import java.nio.file.Path;
import java.util.Objects;

public final class PullFsCmd {
    private final PullService pullService;
    private final OutputView outputView;

    public PullFsCmd(PullService pullService, OutputView outputView) {
        this.pullService = Objects.requireNonNull(pullService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != 3) {
            outputView.showPullUsage();
            return;
        }
        Path remote = Path.of(args[1]);
        String branch = args[2];
        var result = pullService.pull(remote, branch);
        switch (result) {
            case SUCCESS -> outputView.showPullSuccess(branch);
            case ALREADY_UP_TO_DATE -> outputView.showPullUpToDate();
            case REMOTE_NO_COMMITS -> outputView.showPullRemoteNoCommits();
            case NOT_FAST_FORWARD -> outputView.showPullNotFastForward();
        }
    }
}




