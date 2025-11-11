package main.java.app.repository;

public interface RefRepository {
    String readCurrentBranch();
    String readBranchHead(String branchName);
    void updateBranchHead(String branchName, String commitSha);
}


