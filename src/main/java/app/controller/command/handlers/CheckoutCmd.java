package main.java.app.controller.command.handlers;

import main.java.app.service.CheckoutService;
import main.java.app.view.OutputView;

import java.util.Objects;

public final class CheckoutCmd {
    private final CheckoutService checkoutService;
    private final OutputView outputView;

    public CheckoutCmd(CheckoutService checkoutService, OutputView outputView) {
        this.checkoutService = Objects.requireNonNull(checkoutService);
        this.outputView = Objects.requireNonNull(outputView);
    }

    public void execute(String[] args) {
        if (args.length != 2) {
            outputView.showCheckoutUsage();
            return;
        }
        String branch = args[1];
        CheckoutService.CheckoutResult result = checkoutService.switchBranch(branch);
        switch (result) {
            case SUCCESS -> outputView.showCheckoutSuccess(branch);
            case BRANCH_NOT_FOUND -> outputView.showCheckoutNotFound(branch);
            case WORKING_TREE_NOT_CLEAN -> outputView.showCheckoutDirty();
        }
    }
}


