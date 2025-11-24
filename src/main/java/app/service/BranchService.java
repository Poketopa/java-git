package app.service;

import app.repository.RefRepository;
import java.util.List;
import java.util.Objects;


public final class BranchService {
    private final RefRepository refRepository;

    public BranchService(RefRepository refRepository) {
        this.refRepository = Objects.requireNonNull(refRepository, "refRepository");
    }

    public List<String> list() {
        return refRepository.listBranches();
    }

    public void create(String branchName) {
        validateName(branchName);
        String current = refRepository.readCurrentBranch();
        String base = refRepository.readBranchHead(current);
        refRepository.createBranch(branchName, base);
    }


    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 브랜치 이름이 비어 있습니다.");
        }
        if (name.contains(" ") || name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("[ERROR] 브랜치 이름에 공백이나 슬래시를 사용할 수 없습니다.");
        }
    }
}
