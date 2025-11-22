package main.java.app.controller.command.handlers;

import main.java.app.service.MergeService;
import main.java.app.view.OutputView;

import java.util.Objects;

public final class MergeCmd {
    private final MergeService mergeService;
    private final OutputView outputView;

    public MergeCmd(MergeService mergeService, OutputView outputView) {
        this.mergeService = Objects.requireNonNull(mergeService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != 2) {
            outputView.showMergeUsage();
            return;
        }
        String targetBranch = args[1];
        var result = mergeService.merge(targetBranch);
        switch (result) {
            case ALREADY_UP_TO_DATE -> outputView.showMergeAlreadyUpToDate();
            case FAST_FORWARD -> outputView.showMergeFastForward(targetBranch);
            case BRANCH_NOT_FOUND -> outputView.showMergeBranchNotFound(targetBranch);
            case NOT_FAST_FORWARD -> outputView.showMergeNotFastForward();
        }
    }
}




