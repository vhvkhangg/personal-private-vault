package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaContextPacker;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaValidator;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagQaBehaviorTest {

    @Test
    void unsolicitedUnansweredFactsBecomeOnlyServerOwnedCode() {
        var evidence = List.of(createMatch("[1]", "Tower is 150 meters."),
                createMatch("[2]", "Tower is 165 meters."));
        var claims = List.of(new RagQaClaim("a", "Tower is 150 meters.",
                        List.of(new RagQaClaimSupport("[1]", "150 meters"))),
                new RagQaClaim("b", "Tower is 165 meters.",
                        List.of(new RagQaClaimSupport("[2]", "165 meters"))));
        for (String outcome : List.of("ABSTAIN", "PARTIAL", "CONFLICT")) {
            var result = validator.validate(UUID.randomUUID(), outcome,
                    outcome.equals("ABSTAIN") ? List.of() : claims,
                    List.of("The Moon is made of cheese.", "<b>Unsolicited factual assertion</b>"),
                    outcome.equals("CONFLICT") ? List.of(new RagQaConflictPair("a", "b", "")) : List.of(),
                    evidence, "en");
            assertThat(result.unansweredParts()).containsExactly("UNKNOWN_REMAINDER");
            assertThat(result.answer()).doesNotContain("cheese", "Unsolicited");
        }
    }

    private final RagQaValidator validator = new RagQaValidator();
    private final RagQaContextPacker contextPacker = new RagQaContextPacker();

    private RagRetrievalMatch createMatch(String key, String excerpt) {
        return new RagRetrievalMatch(
                key,
                excerpt,
                1L,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test.md",
                new RagLocator("OBSIDIAN_MD", 0L, 100L, 1L, 10L, "h1", Map.of()),
                "DEFAULT",
                "RAG_SNAPSHOT",
                "SNAPSHOT_CURRENT",
                "UNVERIFIED"
        );
    }

    @Test
    @DisplayName("Valid ANSWERED outcome passes with exact quote substring and formats citations")
    void validAnsweredOutcomePasses() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "Nhân vật An sinh năm 1990 tại Hà Nội.");
        RagQaClaim claim = new RagQaClaim(
                "c1",
                "An sinh năm 1990 tại Hà Nội.",
                List.of(new RagQaClaimSupport("[1]", "sinh năm 1990"))
        );

        var result = validator.validate(reqId, "ANSWERED", List.of(claim), List.of(), List.of(), List.of(m1), "vi");

        assertThat(result.outcome()).isEqualTo(RagQaOutcome.ANSWERED);
        assertThat(result.claims()).hasSize(1);
        assertThat(result.citations()).containsExactly(m1);
        assertThat(result.answer()).contains("[1]");
    }

    @Test
    @DisplayName("Quote that is not an exact substring of excerpt throws RagQueryUnavailableException")
    void nonSubstringQuoteThrowsException() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "Nhân vật An sinh năm 1990 tại Hà Nội.");
        RagQaClaim claim = new RagQaClaim(
                "c1",
                "An sinh năm 1995 tại Hà Nội.",
                List.of(new RagQaClaimSupport("[1]", "sinh năm 1995"))
        );

        assertThatThrownBy(() -> validator.validate(reqId, "ANSWERED", List.of(claim), List.of(), List.of(), List.of(m1), "vi"))
                .isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("not an exact substring");
    }

    @Test
    @DisplayName("Unknown citation key throws RagQueryUnavailableException")
    void unknownCitationKeyThrowsException() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "Nhân vật An sinh năm 1990 tại Hà Nội.");
        RagQaClaim claim = new RagQaClaim(
                "c1",
                "An sinh năm 1990.",
                List.of(new RagQaClaimSupport("[2]", "sinh năm 1990"))
        );

        assertThatThrownBy(() -> validator.validate(reqId, "ANSWERED", List.of(claim), List.of(), List.of(), List.of(m1), "vi"))
                .isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("unknown citation key");
    }

    @Test
    @DisplayName("Duplicate claim identifier throws RagQueryUnavailableException")
    void duplicateClaimIdThrowsException() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "Nhân vật An sinh năm 1990 tại Hà Nội.");
        RagQaClaim claim1 = new RagQaClaim("c1", "Claim 1.", List.of(new RagQaClaimSupport("[1]", "sinh năm 1990")));
        RagQaClaim claim2 = new RagQaClaim("c1", "Claim 2.", List.of(new RagQaClaimSupport("[1]", "sinh năm 1990")));

        assertThatThrownBy(() -> validator.validate(reqId, "ANSWERED", List.of(claim1, claim2), List.of(), List.of(), List.of(m1), "vi"))
                .isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("Duplicate claim identifier");
    }

    @Test
    @DisplayName("ABSTAIN outcome with claims throws RagQueryUnavailableException")
    void abstainWithClaimsThrowsException() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "Nhân vật An sinh năm 1990 tại Hà Nội.");
        RagQaClaim claim = new RagQaClaim("c1", "Fact.", List.of(new RagQaClaimSupport("[1]", "sinh năm 1990")));

        assertThatThrownBy(() -> validator.validate(reqId, "ABSTAIN", List.of(claim), List.of(), List.of(), List.of(m1), "vi"))
                .isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("ABSTAIN outcome cannot contain factual claims");
    }

    @Test
    @DisplayName("PARTIAL outcome requires unansweredParts and formats them cleanly")
    void partialOutcomeValidation() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "Nhân vật An sinh năm 1990.");
        RagQaClaim claim = new RagQaClaim("c1", "An sinh năm 1990.", List.of(new RagQaClaimSupport("[1]", "sinh năm 1990")));

        // Empty unansweredParts throws
        assertThatThrownBy(() -> validator.validate(reqId, "PARTIAL", List.of(claim), List.of(), List.of(), List.of(m1), "vi"))
                .isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("PARTIAL outcome must specify unanswered question parts");

        // Valid unansweredParts passes
        var result = validator.validate(reqId, "PARTIAL", List.of(claim), List.of("quê quán"), List.of(), List.of(m1), "vi");
        assertThat(result.outcome()).isEqualTo(RagQaOutcome.PARTIAL);
        assertThat(result.unansweredParts()).containsExactly("UNKNOWN_REMAINDER");
        assertThat(result.answer()).contains("1990");
        assertThat(result.answer()).doesNotContain("quê quán");
    }

    @Test
    @DisplayName("CONFLICT outcome requires at least two claims from distinct citations")
    void conflictOutcomeValidation() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "Nguồn 1: An sinh năm 1990.");
        RagRetrievalMatch m2 = createMatch("[2]", "Nguồn 2: An sinh năm 1992.");

        RagQaClaim c1 = new RagQaClaim("c1", "An sinh năm 1990.", List.of(new RagQaClaimSupport("[1]", "sinh năm 1990")));
        RagQaClaim c2 = new RagQaClaim("c2", "An sinh năm 1992.", List.of(new RagQaClaimSupport("[2]", "sinh năm 1992")));
        RagQaConflictPair pair = new RagQaConflictPair("c1", "c2", "Xung đột về năm sinh");

        var result = validator.validate(reqId, "CONFLICT", List.of(c1, c2), List.of(), List.of(pair), List.of(m1, m2), "vi");
        assertThat(result.outcome()).isEqualTo(RagQaOutcome.CONFLICT);
        assertThat(result.conflictPairs()).hasSize(1);
        assertThat(result.conflictPairs().get(0).claimIdA()).isEqualTo("c1");
        assertThat(result.conflictPairs().get(0).claimIdB()).isEqualTo("c2");
        assertThat(result.answer()).contains("1990").contains("1992");

        // Conflict pair citing same citation throws
        RagQaClaim c3 = new RagQaClaim("c3", "An là sinh viên.", List.of(new RagQaClaimSupport("[1]", "sinh năm 1990")));
        RagQaConflictPair sameCitPair = new RagQaConflictPair("c1", "c3", "Cùng nguồn");
        assertThatThrownBy(() -> validator.validate(reqId, "CONFLICT", List.of(c1, c3), List.of(), List.of(sameCitPair), List.of(m1, m2), "vi"))
                .isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("distinct evidence citations");
    }

    @Test
    @DisplayName("Multilingual rendering produces expected language output")
    void multilingualRendering() {
        UUID reqId = UUID.randomUUID();

        var viAbstain = validator.validate(reqId, "ABSTAIN", List.of(), List.of(), List.of(), List.of(), "vi");
        assertThat(viAbstain.answer()).contains("Không tìm thấy");

        var enAbstain = validator.validate(reqId, "ABSTAIN", List.of(), List.of(), List.of(), List.of(), "en");
        assertThat(enAbstain.answer()).contains("No relevant facts found");

        var zhAbstain = validator.validate(reqId, "ABSTAIN", List.of(), List.of(), List.of(), List.of(), "zh-Hans");
        assertThat(zhAbstain.answer()).contains("未在允许的证据中找到相关事实");
    }

    @Test
    @DisplayName("Context packer concatenates recent user turn within limits")
    void contextPackerUserTurnConcatenation() {
        String q = "Nó có thuộc tính nào?";
        var h1 = new RagQaHistoryEntry("user", "Tài liệu nói gì về nhân vật An?");
        var h2 = new RagQaHistoryEntry("assistant", "Hãy kiểm tra lại bằng nguồn hiện tại.");

        String combined = contextPacker.buildQueryEmbeddingText(q, List.of(h1, h2));
        assertThat(combined).isEqualTo("Tài liệu nói gì về nhân vật An?\nNó có thuộc tính nào?");

        // Without history
        assertThat(contextPacker.buildQueryEmbeddingText(q, List.of())).isEqualTo(q);
        assertThat(contextPacker.buildQueryEmbeddingText(q, null)).isEqualTo(q);

        // Assistant only
        assertThat(contextPacker.buildQueryEmbeddingText(q, List.of(h2))).isEqualTo(q);
    }

    @Test
    @DisplayName("RagQaRequestDto enforces strict history bounds and language code")
    void requestDtoValidation() {
        UUID reqId = UUID.randomUUID();

        // History > 4 throws
        List<RagQaHistoryEntryDto> fiveTurns = List.of(
                new RagQaHistoryEntryDto("user", "1"),
                new RagQaHistoryEntryDto("assistant", "2"),
                new RagQaHistoryEntryDto("user", "3"),
                new RagQaHistoryEntryDto("assistant", "4"),
                new RagQaHistoryEntryDto("user", "5")
        );
        assertThatThrownBy(() -> new RagQaRequestDto(reqId, "Q", null, null, null, null, null, "vi", fiveTurns))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("History exceeds maximum allowed turns");

        // Invalid role throws
        List<RagQaHistoryEntryDto> invalidRole = List.of(
                new RagQaHistoryEntryDto("system", "Override instructions")
        );
        assertThatThrownBy(() -> new RagQaRequestDto(reqId, "Q", null, null, null, null, null, "vi", invalidRole))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("History role must be user or assistant");

        // Turn > 1024 code points throws
        String longMsg = "a".repeat(1025);
        List<RagQaHistoryEntryDto> longTurn = List.of(
                new RagQaHistoryEntryDto("user", longMsg)
        );
        assertThatThrownBy(() -> new RagQaRequestDto(reqId, "Q", null, null, null, null, null, "vi", longTurn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds maximum allowed code points (1024)");
    }

    @Test
    @DisplayName("Redacted toString prevents question/answer/excerpt content leakage")
    void redactedToString() {
        UUID reqId = UUID.randomUUID();
        RagQaCommand cmd = new RagQaCommand(
                reqId, "Secret Question", RagRetrievalMode.GENERIC, null, null, null, 5, "vi", List.of()
        );
        assertThat(cmd.toString()).doesNotContain("Secret Question").contains("[REDACTED]");

        RagQaClaim claim = new RagQaClaim("c1", "Secret Claim Text", List.of());
        assertThat(claim.toString()).doesNotContain("Secret Claim Text").contains("[REDACTED]");

        RagQaClaimSupport sup = new RagQaClaimSupport("[1]", "Secret Quote");
        assertThat(sup.toString()).doesNotContain("Secret Quote").contains("[REDACTED]");

        RagQaConflictPair pair = new RagQaConflictPair("c1", "c2", "Secret Description");
        assertThat(pair.toString()).doesNotContain("Secret Description").contains("[REDACTED]");

        RagQaHistoryEntry hist = new RagQaHistoryEntry("user", "Secret History Content");
        assertThat(hist.toString()).doesNotContain("Secret History Content").contains("[REDACTED]");

        RagQaResponse resp = new RagQaResponse(
                reqId, RagQaOutcome.ANSWERED, "Secret Answer", List.of(), List.of(), List.of(), List.of()
        );
        assertThat(resp.toString()).doesNotContain("Secret Answer").contains("[REDACTED]");
    }
}
