package main.java.app.controller.command.handlers;

import main.java.app.service.remote.fs.CloneService;
import main.java.app.view.OutputView;

import java.nio.file.Path;
import java.util.Objects;

public final class CloneFsCmd {
    private final CloneService cloneService;
    private final OutputView outputView;

    public CloneFsCmd(CloneService cloneService, OutputView outputView) {
        this.cloneService = Objects.requireNonNull(cloneService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != 3) {
            outputView.showCloneUsage();
            return;
        }
        Path remote = Path.of(args[1]);
        Path target = Path.of(args[2]);
        var result = cloneService.clone(remote, target);
        switch (result) {
            case SUCCESS -> outputView.showCloneSuccess(target.toString());
            case REMOTE_NOT_FOUND -> outputView.showCloneRemoteNotFound(remote.toString());
            case TARGET_EXISTS_NOT_EMPTY -> outputView.showCloneTargetExists(target.toString());
            case REMOTE_NO_COMMITS -> outputView.showCloneRemoteNoCommits();
        }
    }
}


