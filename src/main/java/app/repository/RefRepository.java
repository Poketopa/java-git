package main.java.app.repository;

public interface RefRepository {
    String readCurrentBranch();
    String readBranchHead(String branchName);
    void updateBranchHead(String branchName, String commitSha);
    java.util.List<String> listBranches();
    void createBranch(String branchName, String baseCommitSha);
    void updateCurrentBranch(String branchName);
}



