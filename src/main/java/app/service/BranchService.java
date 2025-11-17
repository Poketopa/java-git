package main.java.app.service;

import main.java.app.repository.RefRepository;

import java.util.List;
import java.util.Objects;

// 브랜치 조회/생성
// - list(): refs/heads/* 목록
// - create(): 현재 브랜치 HEAD를 기준으로 신규 브랜치 생성
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

    // 브랜치 이름 간단 검증 (공백/슬래시 금지)
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("[ERROR] 브랜치 이름이 비어 있습니다.");
        }
        if (name.contains(" ") || name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("[ERROR] 브랜치 이름에 공백이나 슬래시를 사용할 수 없습니다.");
        }
    }
}


