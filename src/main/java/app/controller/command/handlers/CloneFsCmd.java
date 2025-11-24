package app.controller.command.handlers;

import app.service.remote.fs.CloneService;
import app.view.OutputView;
import java.nio.file.Path;
import java.util.Objects;

public final class CloneFsCmd {
    private static final int EXPECTED_ARGUMENTS = 3;
    private static final int REMOTE_PATH_INDEX = 1;
    private static final int TARGET_PATH_INDEX = 2;

    private final CloneService cloneService;
    private final OutputView outputView;

    public CloneFsCmd(CloneService cloneService, OutputView outputView) {
        this.cloneService = Objects.requireNonNull(cloneService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != EXPECTED_ARGUMENTS) {
            outputView.showCloneUsage();
            return;
        }
        Path remote = Path.of(args[REMOTE_PATH_INDEX]);
        Path target = Path.of(args[TARGET_PATH_INDEX]);
        CloneService.CloneResult result = cloneService.clone(remote, target);

        if (result == CloneService.CloneResult.SUCCESS) {
            outputView.showCloneSuccess(target.toString());
            return;
        }
        if (result == CloneService.CloneResult.REMOTE_NOT_FOUND) {
            outputView.showCloneRemoteNotFound(remote.toString());
            return;
        }
        if (result == CloneService.CloneResult.TARGET_EXISTS_NOT_EMPTY) {
            outputView.showCloneTargetExists(target.toString());
            return;
        }
        if (result == CloneService.CloneResult.REMOTE_NO_COMMITS) {
            outputView.showCloneRemoteNoCommits();
        }
    }
}
