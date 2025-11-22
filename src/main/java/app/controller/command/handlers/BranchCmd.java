package app.controller.command.handlers;

import app.service.BranchService;
import app.view.OutputView;

import java.util.Objects;

public final class BranchCmd {
    private static final int LIST_ONLY_ARGS_LENGTH = 1;
    private static final int CREATE_BRANCH_ARGS_LENGTH = 2;
    private static final int BRANCH_NAME_INDEX = 1;

    private final BranchService branchService;
    private final OutputView outputView;

    public BranchCmd(BranchService branchService, OutputView outputView) {
        this.branchService = Objects.requireNonNull(branchService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length == LIST_ONLY_ARGS_LENGTH) {
            outputView.showBranches(branchService.list());
            return;
        }
        if (args.length == CREATE_BRANCH_ARGS_LENGTH) {
            String branchName = args[BRANCH_NAME_INDEX];
            try {
                branchService.create(branchName);
                outputView.showBranchCreated(branchName);
            } catch (IllegalArgumentException e) {
                outputView.showBranchAlreadyExists(branchName);
            }
            return;
        }
        outputView.showUsage();
    }
}
