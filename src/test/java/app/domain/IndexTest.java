package app.domain;

import app.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IndexTest {

    @Test
    @DisplayName("유효한 stagedFiles로 Index를 생성하면 불변 맵이 생성된다")
    void createIndex_withValidMap_createsUnmodifiableMap() {
        // given
        Map<String, String> source = new HashMap<>();
        source.put("file1.txt", "oid1");
        source.put("file2.txt", "oid2");

        // when
        Index index = new Index(source);

        // then
        Map<String, String> stagedFiles = index.stagedFiles();
        assertThat(stagedFiles)
                .hasSize(2)
                .containsEntry("file1.txt", "oid1")
                .containsEntry("file2.txt", "oid2");

        // 불변성 확인: put 시도 시 예외
        assertThatThrownBy(() -> stagedFiles.put("file3.txt", "oid3"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("빈 맵으로 Index를 생성하면 비어 있는 stagedFiles를 가진다")
    void createIndex_withEmptyMap_createsEmptyIndex() {
        // given
        Map<String, String> empty = Collections.emptyMap();

        // when
        Index index = new Index(empty);

        // then
        assertThat(index.stagedFiles()).isEmpty();
    }

    @Test
    @DisplayName("stagedFiles가 null이면 예외가 발생한다")
    void createIndex_whenStagedFilesIsNull_throwsException() {
        assertThatThrownBy(() -> new Index(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorCode.INDEX_STAGED_FILES_NULL.message());
    }

    @Test
    @DisplayName("파일 경로가 null인 항목이 있으면 예외가 발생한다")
    void createIndex_whenPathIsNull_throwsException() {
        Map<String, String> map = new HashMap<>();
        map.put(null, "oid1");

        assertThatThrownBy(() -> new Index(map))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorCode.INDEX_STAGED_FILE_PATH_NULL.message());
    }

    @Test
    @DisplayName("파일 OID가 null인 항목이 있으면 예외가 발생한다")
    void createIndex_whenOidIsNull_throwsException() {
        Map<String, String> map = new HashMap<>();
        map.put("file1.txt", null);

        assertThatThrownBy(() -> new Index(map))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorCode.INDEX_STAGED_FILE_OID_NULL.message());
    }
}
