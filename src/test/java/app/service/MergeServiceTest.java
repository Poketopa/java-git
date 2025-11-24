package app.service;

import app.repository.ObjectReader;
import app.repository.RefRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static app.service.MergeService.MergeResult.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MergeServiceTest {

    @Test
    @DisplayName("대상 브랜치 이름이 null 또는 공백이면 BRANCH_NOT_FOUND를 반환한다")
    void merge_whenTargetBranchIsNullOrBlank_returnsBranchNotFound() {
        RefRepository refRepository = mock(RefRepository.class);
        ObjectReader objectReader = mock(ObjectReader.class);
        MergeService mergeService = new MergeService(refRepository, objectReader);

        assertThat(mergeService.merge(null)).isEqualTo(BRANCH_NOT_FOUND);
        assertThat(mergeService.merge("  ")).isEqualTo(BRANCH_NOT_FOUND);

        verifyNoInteractions(refRepository, objectReader);
    }

    @Test
    @DisplayName("대상 브랜치의 HEAD가 없으면 BRANCH_NOT_FOUND를 반환한다")
    void merge_whenTargetHeadIsEmpty_returnsBranchNotFound() {
        RefRepository refRepository = mock(RefRepository.class);
        ObjectReader objectReader = mock(ObjectReader.class);
        MergeService mergeService = new MergeService(refRepository, objectReader);

        when(refRepository.readCurrentBranch()).thenReturn("main");
        when(refRepository.readBranchHead("main")).thenReturn("c1");
        when(refRepository.readBranchHead("feature")).thenReturn(null);

        MergeService.MergeResult result = mergeService.merge("feature");

        assertThat(result).isEqualTo(BRANCH_NOT_FOUND);
        verify(refRepository, never()).updateBranchHead(anyString(), anyString());
        verifyNoInteractions(objectReader);
    }

    @Test
    @DisplayName("현재 브랜치의 HEAD가 없으면 대상 HEAD로 Fast-forward 한다")
    void merge_whenCurrentHeadIsEmpty_performsFastForward() {
        RefRepository refRepository = mock(RefRepository.class);
        ObjectReader objectReader = mock(ObjectReader.class);
        MergeService mergeService = new MergeService(refRepository, objectReader);

        when(refRepository.readCurrentBranch()).thenReturn("main");
        when(refRepository.readBranchHead("main")).thenReturn(null);
        when(refRepository.readBranchHead("feature")).thenReturn("t1");

        MergeService.MergeResult result = mergeService.merge("feature");

        assertThat(result).isEqualTo(FAST_FORWARD);
        verify(refRepository).updateBranchHead("main", "t1");
        verifyNoInteractions(objectReader);
    }

    @Test
    @DisplayName("현재 HEAD와 대상 HEAD가 같으면 Already up to date를 반환한다")
    void merge_whenHeadsAreEqual_returnsAlreadyUpToDate() {
        RefRepository refRepository = mock(RefRepository.class);
        ObjectReader objectReader = mock(ObjectReader.class);
        MergeService mergeService = new MergeService(refRepository, objectReader);

        when(refRepository.readCurrentBranch()).thenReturn("main");
        when(refRepository.readBranchHead("main")).thenReturn("c1");
        when(refRepository.readBranchHead("feature")).thenReturn("c1");

        MergeService.MergeResult result = mergeService.merge("feature");

        assertThat(result).isEqualTo(ALREADY_UP_TO_DATE);
        verify(refRepository, never()).updateBranchHead(anyString(), anyString());
        verifyNoInteractions(objectReader);
    }

    @Test
    @DisplayName("현재 커밋이 대상 커밋의 조상이면 Fast-forward를 수행한다")
    void merge_whenCurrentIsAncestorOfTarget_performsFastForward() {
        RefRepository refRepository = mock(RefRepository.class);
        ObjectReader objectReader = mock(ObjectReader.class);
        MergeService mergeService = new MergeService(refRepository, objectReader);

        // main: c1, feature: c3 (c3 -> c2 -> c1)
        when(refRepository.readCurrentBranch()).thenReturn("main");
        when(refRepository.readBranchHead("main")).thenReturn("c1");
        when(refRepository.readBranchHead("feature")).thenReturn("c3");

        // c3 parent = c2
        when(objectReader.readRaw("c3")).thenReturn(
                ("tree tree-sha\n" +
                        "parent c2\n" +
                        "\n" +
                        "msg").getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        // c2 parent = c1
        when(objectReader.readRaw("c2")).thenReturn(
                ("tree tree-sha\n" +
                        "parent c1\n" +
                        "\n" +
                        "msg").getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        // c1 has no parent line
        when(objectReader.readRaw("c1")).thenReturn(
                ("tree tree-sha\n" +
                        "\n" +
                        "msg").getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        MergeService.MergeResult result = mergeService.merge("feature");

        assertThat(result).isEqualTo(FAST_FORWARD);
        verify(refRepository).updateBranchHead("main", "c3");
    }

    @Test
    @DisplayName("현재 커밋이 대상 커밋의 조상이 아니면 NOT_FAST_FORWARD를 반환한다")
    void merge_whenCurrentIsNotAncestorOfTarget_returnsNotFastForward() {
        RefRepository refRepository = mock(RefRepository.class);
        ObjectReader objectReader = mock(ObjectReader.class);
        MergeService mergeService = new MergeService(refRepository, objectReader);

        // main: base, feature: other (other -> a -> b)
        when(refRepository.readCurrentBranch()).thenReturn("main");
        when(refRepository.readBranchHead("main")).thenReturn("base");
        when(refRepository.readBranchHead("feature")).thenReturn("other");

        when(objectReader.readRaw("other")).thenReturn(
                ("tree tree-sha\n" +
                        "parent a\n" +
                        "\n" +
                        "msg").getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        when(objectReader.readRaw("a")).thenReturn(
                ("tree tree-sha\n" +
                        "parent b\n" +
                        "\n" +
                        "msg").getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        when(objectReader.readRaw("b")).thenReturn(
                ("tree tree-sha\n" +
                        "\n" +
                        "msg").getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        MergeService.MergeResult result = mergeService.merge("feature");

        assertThat(result).isEqualTo(NOT_FAST_FORWARD);
        verify(refRepository, never()).updateBranchHead(anyString(), anyString());
    }
}


