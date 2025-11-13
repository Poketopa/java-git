package main.java.app.view;

public final class ConsoleOutputView {
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
}


