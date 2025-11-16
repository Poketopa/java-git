package main.java.app.service;

import main.java.app.repository.RefRepository;

import java.util.Objects;

public final class CheckoutService {
    public enum CheckoutResult {
        SUCCESS,
        BRANCH_NOT_FOUND,
        WORKING_TREE_NOT_CLEAN
    }

    private final RefRepository refRepository;
    private final StatusService statusService;

    public CheckoutService(RefRepository refRepository, StatusService statusService) {
        this.refRepository = Objects.requireNonNull(refRepository, "refRepository");
        this.statusService = Objects.requireNonNull(statusService, "statusService");
    }

    public CheckoutResult switchBranch(String branchName) {
        if (!refRepository.listBranches().contains(branchName)) {
            return CheckoutResult.BRANCH_NOT_FOUND;
        }
        StatusService.StatusResult status = statusService.status();
        boolean hasStaged = !(status.stagedAdded().isEmpty() && status.stagedModified().isEmpty() && status.stagedDeleted().isEmpty());
        boolean hasNotStaged = !(status.modifiedNotStaged().isEmpty() && status.deletedNotStaged().isEmpty());
        boolean hasUntracked = !status.untracked().isEmpty();
        if (hasStaged || hasNotStaged || hasUntracked) {
            return CheckoutResult.WORKING_TREE_NOT_CLEAN;
        }
        refRepository.updateCurrentBranch(branchName);
        return CheckoutResult.SUCCESS;
    }
}


