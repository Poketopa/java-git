package main.java.app.view;

import main.java.app.service.StatusService;
import main.java.app.service.LogService;

public final class OutputView {
    // REPL 안내/프롬프트/종료/에러 출력
    public void showWelcome() {
        System.out.println(Messages.REPL_WELCOME);
    }

    public void showPrompt() {
        System.out.print(Messages.REPL_PROMPT);
    }

    public void showBye() {
        System.out.println(Messages.REPL_BYE);
    }

    public void showRequireGitPrefix() {
        System.err.println(Messages.REPL_REQUIRE_GIT_PREFIX);
        showUsage();
    }

    public void showInputReadError(String detail) {
        System.err.println(Messages.REPL_INPUT_READ_ERROR + detail);
    }

    // 사용법 출력
    public void showUsage() {
        System.out.println(Messages.USAGE_HEADER);
        System.out.println(Messages.USAGE_INIT);
        System.out.println(Messages.USAGE_ADD);
        System.out.println(Messages.USAGE_COMMIT);
    }

    public void showInitSuccess() {
        System.out.println(Messages.INIT_SUCCESS);
    }

    public void showAddMissingPath() {
        System.err.println(Messages.ADD_MISSING_PATH);
    }

    public void showAddedToIndex(int count) {
        System.out.printf(Messages.ADDED_TO_INDEX + "%n", count);
    }

    public void showCommitCreated() {
        System.out.println(Messages.COMMIT_CREATED);
    }

    public void showCommitUsageError() {
        System.err.println(Messages.COMMIT_USAGE_ERROR);
    }

    // status 출력 포맷팅
    public void showStatus(StatusService.StatusResult result) {
        boolean hasStaged = !(result.stagedAdded().isEmpty() && result.stagedModified().isEmpty() && result.stagedDeleted().isEmpty());
        boolean hasNotStaged = !(result.modifiedNotStaged().isEmpty() && result.deletedNotStaged().isEmpty());
        boolean hasUntracked = !result.untracked().isEmpty();
        if (!hasStaged && !hasNotStaged && !hasUntracked) {
            System.out.println(Messages.STATUS_CLEAN);
            return;
        }
        if (hasStaged) {
            System.out.println(Messages.STATUS_SECTION_STAGED);
            result.stagedAdded().keySet().forEach(path -> System.out.println(Messages.STATUS_INDENT + path + " " + Messages.STATUS_STAGED_ADDED));
            result.stagedModified().keySet().forEach(path -> System.out.println(Messages.STATUS_INDENT + path + " " + Messages.STATUS_STAGED_MODIFIED));
            result.stagedDeleted().forEach(path -> System.out.println(Messages.STATUS_INDENT + path + " " + Messages.STATUS_STAGED_DELETED));
            System.out.println();
        }
        if (hasNotStaged) {
            System.out.println(Messages.STATUS_SECTION_NOT_STAGED);
            result.modifiedNotStaged().forEach(path -> System.out.println(Messages.STATUS_INDENT + Messages.STATUS_LABEL_MODIFIED + path));
            result.deletedNotStaged().forEach(path -> System.out.println(Messages.STATUS_INDENT + Messages.STATUS_LABEL_DELETED + path));
            System.out.println();
        }
        if (hasUntracked) {
            System.out.println(Messages.STATUS_SECTION_UNTRACKED);
            result.untracked().forEach(path -> System.out.println(Messages.STATUS_INDENT + path));
        }
    }

    public void showBranches(java.util.List<String> names) {
        System.out.println(Messages.BRANCH_LIST_HEADER);
        if (names == null || names.isEmpty()) {
            return;
        }
        names.forEach(n -> System.out.println(Messages.STATUS_INDENT + n));
    }

    public void showBranchCreated(String name) {
        System.out.println(Messages.BRANCH_CREATED + name);
    }

    public void showBranchAlreadyExists(String name) {
        System.err.println(Messages.BRANCH_ALREADY_EXISTS + name);
    }

    public void showCheckoutUsage() {
        System.out.println(Messages.CHECKOUT_USAGE);
    }

    public void showCheckoutSuccess(String branch) {
        System.out.println(Messages.CHECKOUT_SUCCESS + branch);
    }

    public void showCheckoutDirty() {
        System.err.println(Messages.CHECKOUT_DIRTY);
    }

    public void showCheckoutNotFound(String branch) {
        System.err.println(Messages.CHECKOUT_NOT_FOUND + branch);
    }

    // merge 출력
    public void showMergeUsage() {
        System.out.println(Messages.MERGE_USAGE);
    }

    public void showMergeAlreadyUpToDate() {
        System.out.println(Messages.MERGE_ALREADY_UP_TO_DATE);
    }

    public void showMergeFastForward(String sourceBranch) {
        System.out.println(Messages.MERGE_FAST_FORWARD + sourceBranch);
    }

    public void showMergeBranchNotFound(String branch) {
        System.err.println(Messages.MERGE_BRANCH_NOT_FOUND + branch);
    }

    public void showMergeNotFastForward() {
        System.err.println(Messages.MERGE_NOT_FAST_FORWARD);
    }

    // push
    public void showPushUsage() {
        System.out.println(Messages.PUSH_USAGE);
    }
    public void showPushSuccess(String branch) {
        System.out.println(Messages.PUSH_SUCCESS + branch);
    }
    public void showPushUpToDate() {
        System.out.println(Messages.PUSH_UP_TO_DATE);
    }
    public void showPushRejectedNonFastForward() {
        System.err.println(Messages.PUSH_REJECTED_NON_FF);
    }
    public void showPushLocalNoCommits() {
        System.err.println(Messages.PUSH_LOCAL_NO_COMMITS);
    }

    // pull
    public void showPullUsage() {
        System.out.println(Messages.PULL_USAGE);
    }
    public void showPullSuccess(String branch) {
        System.out.println(Messages.PULL_SUCCESS + branch);
    }
    public void showPullUpToDate() {
        System.out.println(Messages.PULL_UP_TO_DATE);
    }
    public void showPullRemoteNoCommits() {
        System.out.println(Messages.PULL_REMOTE_NO_COMMITS);
    }
    public void showPullNotFastForward() {
        System.err.println(Messages.PULL_NOT_FAST_FORWARD);
    }

    // clone
    public void showCloneUsage() {
        System.out.println(Messages.CLONE_USAGE);
    }
    public void showCloneSuccess(String targetDir) {
        System.out.println(Messages.CLONE_SUCCESS + targetDir);
    }
    public void showCloneRemoteNotFound(String remoteDir) {
        System.err.println(Messages.CLONE_REMOTE_NOT_FOUND + remoteDir);
    }
    public void showCloneTargetExists(String targetDir) {
        System.err.println(Messages.CLONE_TARGET_EXISTS + targetDir);
    }
    public void showCloneRemoteNoCommits() {
        System.err.println(Messages.CLONE_REMOTE_NO_COMMITS);
    }

    // http remote
    public void showServeHttpUsage() {
        System.out.println(Messages.SERVE_HTTP_USAGE);
    }
    public void showServeHttpStarted(int port) {
        System.out.println(Messages.SERVE_HTTP_STARTED + port);
    }

    public void showPushHttpUsage() {
        System.out.println(Messages.PUSH_HTTP_USAGE);
    }
    public void showPushHttpSuccess(String branch) {
        System.out.println(Messages.PUSH_HTTP_SUCCESS + branch);
    }
    public void showPushHttpUpToDate() {
        System.out.println(Messages.PUSH_HTTP_UP_TO_DATE);
    }
    public void showPushHttpRejectedNonFastForward() {
        System.err.println(Messages.PUSH_HTTP_REJECTED_NON_FF);
    }
    public void showPushHttpLocalNoCommits() {
        System.err.println(Messages.PUSH_HTTP_LOCAL_NO_COMMITS);
    }

    public void showPullHttpUsage() {
        System.out.println(Messages.PULL_HTTP_USAGE);
    }
    public void showPullHttpSuccess(String branch) {
        System.out.println(Messages.PULL_HTTP_SUCCESS + branch);
    }
    public void showPullHttpUpToDate() {
        System.out.println(Messages.PULL_HTTP_UP_TO_DATE);
    }
    public void showPullHttpRemoteNoCommits() {
        System.out.println(Messages.PULL_HTTP_REMOTE_NO_COMMITS);
    }
    public void showPullHttpNotFastForward() {
        System.err.println(Messages.PULL_HTTP_NOT_FAST_FORWARD);
    }
    // log 출력 포맷팅 (요약)
    public void showLog(java.util.List<LogService.LogEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            System.out.println(Messages.LOG_NO_COMMITS);
            return;
        }
        for (LogService.LogEntry e : entries) {
            String shortOid = e.hash().length() >= 7 ? e.hash().substring(0, 7) : e.hash();
            System.out.println(shortOid + " " + e.message());
            System.out.println(Messages.STATUS_INDENT + Messages.LOG_LABEL_AUTHOR + e.author());
            System.out.println(Messages.STATUS_INDENT + Messages.LOG_LABEL_DATE + e.dateTimeIso());
            System.out.println();
        }
    }
}
