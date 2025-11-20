package main.java.app.controller.command.handlers;

import main.java.app.service.remote.fs.PushService;
import main.java.app.view.OutputView;

import java.nio.file.Path;
import java.util.Objects;

public final class PushFsCmd {
    private final PushService pushService;
    private final OutputView outputView;

    public PushFsCmd(PushService pushService, OutputView outputView) {
        this.pushService = Objects.requireNonNull(pushService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != 3) {
            outputView.showPushUsage();
            return;
        }
        Path remote = Path.of(args[1]);
        String branch = args[2];
        var result = pushService.push(remote, branch);
        switch (result) {
            case SUCCESS -> outputView.showPushSuccess(branch);
            case ALREADY_UP_TO_DATE -> outputView.showPushUpToDate();
            case REMOTE_REJECTED_NON_FF -> outputView.showPushRejectedNonFastForward();
            case LOCAL_NO_COMMITS -> outputView.showPushLocalNoCommits();
        }
    }
}


