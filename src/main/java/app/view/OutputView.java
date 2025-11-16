package main.java.app.view;

import main.java.app.service.StatusService;
import main.java.app.service.LogService;

public final class OutputView {
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
