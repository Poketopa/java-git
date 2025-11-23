package app.domain;

import app.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommitTest {

    @Test
    @DisplayName("유효한 값으로 Commit을 생성하면 필드들이 정상적으로 설정된다")
    void createCommit_withValidArguments_setsFields() {
        // given
        String message = "initial commit";
        String treeOid = "tree-sha";
        String parentOid = "parent-sha";
        String author = "author-name";

        // when
        Commit commit = new Commit(message, treeOid, parentOid, author);

        // then
        assertThat(commit.message()).isEqualTo(message);
        assertThat(commit.treeOid()).isEqualTo(treeOid);
        assertThat(commit.parentOid()).isEqualTo(parentOid);
        assertThat(commit.author()).isEqualTo(author);
        assertThat(commit.createdAtMillis()).isPositive();
        assertThat(commit.createdAtMillis()).isLessThanOrEqualTo(System.currentTimeMillis());
    }

    @Test
    @DisplayName("커밋 메시지가 null이면 예외가 발생한다")
    void createCommit_whenMessageIsNull_throwsException() {
        assertThatThrownBy(() -> new Commit(null, "tree", "parent", "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorCode.COMMIT_MESSAGE_NULL.message());
    }

    @Test
    @DisplayName("커밋 메시지가 비어 있으면 예외가 발생한다")
    void createCommit_whenMessageIsBlank_throwsException() {
        assertThatThrownBy(() -> new Commit("   ", "tree", "parent", "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorCode.COMMIT_MESSAGE_EMPTY.message());
    }

    @Test
    @DisplayName("Tree OID가 null이면 예외가 발생한다")
    void createCommit_whenTreeOidIsNull_throwsException() {
        assertThatThrownBy(() -> new Commit("msg", null, "parent", "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorCode.COMMIT_TREE_OID_NULL.message());
    }

    @Test
    @DisplayName("작성자가 null이면 예외가 발생한다")
    void createCommit_whenAuthorIsNull_throwsException() {
        assertThatThrownBy(() -> new Commit("msg", "tree", "parent", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorCode.COMMIT_AUTHOR_NULL.message());
    }

    @Test
    @DisplayName("작성자가 비어 있으면 예외가 발생한다")
    void createCommit_whenAuthorIsBlank_throwsException() {
        assertThatThrownBy(() -> new Commit("msg", "tree", "parent", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorCode.COMMIT_AUTHOR_EMPTY.message());
    }
}


