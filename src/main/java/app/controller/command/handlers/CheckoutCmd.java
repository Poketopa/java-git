package main.java.app.controller.command.handlers;

import main.java.app.service.CheckoutService;
import main.java.app.view.OutputView;

import java.util.Objects;

public final class CheckoutCmd {
    private static final int EXPECTED_ARGUMENTS = 2;
    private static final int BRANCH_NAME_INDEX = 1;

    private final CheckoutService checkoutService;
    private final OutputView outputView;

    public CheckoutCmd(CheckoutService checkoutService, OutputView outputView) {
        this.checkoutService = Objects.requireNonNull(checkoutService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != EXPECTED_ARGUMENTS) {
            outputView.showCheckoutUsage();
            return;
        }
        String branch = args[BRANCH_NAME_INDEX];
        CheckoutService.CheckoutResult result = checkoutService.switchBranch(branch);

        if (result == CheckoutService.CheckoutResult.SUCCESS) {
            outputView.showCheckoutSuccess(branch);
            return;
        }
        if (result == CheckoutService.CheckoutResult.BRANCH_NOT_FOUND) {
            outputView.showCheckoutNotFound(branch);
            return;
        }
        if (result == CheckoutService.CheckoutResult.WORKING_TREE_NOT_CLEAN) {
            outputView.showCheckoutDirty();
        }
    }
}
