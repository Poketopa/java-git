package app.controller.command.handlers;

import app.service.remote.fs.PushService;
import app.view.OutputView;
import java.nio.file.Path;
import java.util.Objects;

public final class PushFsCmd {
    private static final int EXPECTED_ARGUMENTS = 3;
    private static final int REMOTE_PATH_INDEX = 1;
    private static final int BRANCH_NAME_INDEX = 2;

    private final PushService pushService;
    private final OutputView outputView;

    public PushFsCmd(PushService pushService, OutputView outputView) {
        this.pushService = Objects.requireNonNull(pushService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != EXPECTED_ARGUMENTS) {
            outputView.showPushUsage();
            return;
        }
        Path remote = Path.of(args[REMOTE_PATH_INDEX]);
        String branch = args[BRANCH_NAME_INDEX];
        PushService.PushResult result = pushService.push(remote, branch);

        if (result == PushService.PushResult.SUCCESS) {
            outputView.showPushSuccess(branch);
            return;
        }
        if (result == PushService.PushResult.ALREADY_UP_TO_DATE) {
            outputView.showPushUpToDate();
            return;
        }
        if (result == PushService.PushResult.REMOTE_REJECTED_NON_FF) {
            outputView.showPushRejectedNonFastForward();
            return;
        }
        if (result == PushService.PushResult.LOCAL_NO_COMMITS) {
            outputView.showPushLocalNoCommits();
        }
    }
}
