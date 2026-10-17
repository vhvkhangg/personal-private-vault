package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.application.RagDependencyGraph.Snapshot;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceService;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class RagAuthorityContractTest {
    @Test
    void rejectsQuotedUnknownAndEncodedMetadataWhileAcceptingProse() {
        for (String text : List.of("{\"visibility\":\"PRIVATE\",\"body\":\"Synthetic text\"}",
                "\"private\": true", "unknown_schema: safe", "<private>false</private>",
                "visibility=PUBLIC", "---\nplain text", "Text\u0000hidden", "![child]", "key\\u003aPRIVATE")) {
            assertThat(RagSourceService.hasRestrictiveMetadata(text)).as(text).isTrue();
        }
        assertThat(RagSourceService.hasRestrictiveMetadata("Synthetic plain prose.\nVietnamese tiếng Việt!")).isFalse();
    }

    @Test
    void longestPathMemoizationDoesNotTruncateSharedDagBranches() {
        Snapshot graph = new Snapshot(Map.of(1L, Set.of(2L, 3L), 2L, Set.of(4L), 3L, Set.of(5L), 5L, Set.of(2L)), Map.of());
        assertThat(graph.descendantDepth(1L)).isEqualTo(5);
        assertThat(graph.descendants(1L)).containsExactly(2L, 3L, 4L, 5L);
    }

    @Test
    void detectsCyclesBelowRootAndBoundsDepth() {
        Snapshot cycle = new Snapshot(Map.of(1L, Set.of(2L), 2L, Set.of(3L), 3L, Set.of(2L)), Map.of());
        assertThatThrownBy(() -> cycle.descendants(1L)).isInstanceOf(IllegalStateException.class);
        Map<Long, Set<Long>> chain = new HashMap<>();
        for (long i = 1; i < 10; i++) chain.put(i, Set.of(i + 1));
        assertThatThrownBy(() -> new Snapshot(chain, Map.of()).descendantDepth(1L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("depth");
    }
}

