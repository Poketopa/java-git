package app.controller.command.handlers;

import app.service.MergeService;
import app.view.OutputView;

import java.util.Objects;

public final class MergeCmd {
    private static final int EXPECTED_ARGUMENTS = 2;
    private static final int TARGET_BRANCH_INDEX = 1;

    private final MergeService mergeService;
    private final OutputView outputView;

    public MergeCmd(MergeService mergeService, OutputView outputView) {
        this.mergeService = Objects.requireNonNull(mergeService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != EXPECTED_ARGUMENTS) {
            outputView.showMergeUsage();
            return;
        }
        String targetBranch = args[TARGET_BRANCH_INDEX];
        MergeService.MergeResult result = mergeService.merge(targetBranch);

        if (result == MergeService.MergeResult.ALREADY_UP_TO_DATE) {
            outputView.showMergeAlreadyUpToDate();
            return;
        }
        if (result == MergeService.MergeResult.FAST_FORWARD) {
            outputView.showMergeFastForward(targetBranch);
            return;
        }
        if (result == MergeService.MergeResult.BRANCH_NOT_FOUND) {
            outputView.showMergeBranchNotFound(targetBranch);
            return;
        }
        if (result == MergeService.MergeResult.NOT_FAST_FORWARD) {
            outputView.showMergeNotFastForward();
        }
    }
}
