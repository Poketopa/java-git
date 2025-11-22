package main.java.app.controller.command.handlers;

import main.java.app.service.BranchService;
import main.java.app.view.OutputView;

import java.util.Objects;

public final class BranchCmd {
    private final BranchService branchService;
    private final OutputView outputView;

    public BranchCmd(BranchService branchService, OutputView outputView) {
        this.branchService = Objects.requireNonNull(branchService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length == 1) {
            outputView.showBranches(branchService.list());
            return;
        }
        if (args.length == 2) {
            try {
                branchService.create(args[1]);
                outputView.showBranchCreated(args[1]);
            } catch (IllegalArgumentException e) {
                outputView.showBranchAlreadyExists(args[1]);
            }
            return;
        }
        outputView.showUsage();
    }
}




