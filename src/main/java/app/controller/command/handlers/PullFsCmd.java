package app.controller.command.handlers;

import app.service.remote.fs.PullService;
import app.view.OutputView;

import java.nio.file.Path;
import java.util.Objects;

public final class PullFsCmd {
    private static final int EXPECTED_ARGUMENTS = 3;
    private static final int REMOTE_PATH_INDEX = 1;
    private static final int BRANCH_NAME_INDEX = 2;

    private final PullService pullService;
    private final OutputView outputView;

    public PullFsCmd(PullService pullService, OutputView outputView) {
        this.pullService = Objects.requireNonNull(pullService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != EXPECTED_ARGUMENTS) {
            outputView.showPullUsage();
            return;
        }
        Path remote = Path.of(args[REMOTE_PATH_INDEX]);
        String branch = args[BRANCH_NAME_INDEX];
        PullService.PullResult result = pullService.pull(remote, branch);

        if (result == PullService.PullResult.SUCCESS) {
            outputView.showPullSuccess(branch);
            return;
        }
        if (result == PullService.PullResult.ALREADY_UP_TO_DATE) {
            outputView.showPullUpToDate();
            return;
        }
        if (result == PullService.PullResult.REMOTE_NO_COMMITS) {
            outputView.showPullRemoteNoCommits();
            return;
        }
        if (result == PullService.PullResult.NOT_FAST_FORWARD) {
            outputView.showPullNotFastForward();
        }
    }
}
