package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagQaCitationTest {

    private final RagQaValidator validator = new RagQaValidator();

    private RagRetrievalMatch createMatch(String key, String displayName, String excerpt, long charStart, long charEnd) {
        return createMatch(key, displayName, excerpt, charStart, charEnd, "DEFAULT");
    }

    private RagRetrievalMatch createMatch(String key, String displayName, String excerpt, long charStart, long charEnd, String group) {
        return new RagRetrievalMatch(
                key,
                excerpt,
                1L,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                displayName,
                new RagLocator("OBSIDIAN_MD", charStart, charEnd, 1L, 10L, "Section 1", Map.of()),
                group,
                "RAG_SNAPSHOT",
                "SNAPSHOT_CURRENT",
                "UNVERIFIED"
        );
    }

    @Test
    void noncanonClaimReceivesTrustedMarkerWithoutRelabelingCoreClaims() {
        var core = createMatch("[1]", "core.md", "Core archive is blue.", 0, 21, "IDEAVERSE_CORE");
        var noncore = createMatch("[2]", "noncore.md", "Alternate archive is amber.", 0, 26, "IDEAVERSE_NONCORE");
        var response = validator.validate(UUID.randomUUID(), "ANSWERED", List.of(
                new RagQaClaim("a", "Core archive is blue.", List.of(new RagQaClaimSupport("[1]", "Core archive is blue"))),
                new RagQaClaim("b", "Alternate archive is amber.", List.of(new RagQaClaimSupport("[2]", "Alternate archive is amber")))
        ), List.of(), List.of(), List.of(core, noncore), "en");
        assertThat(response.answer()).contains("Core archive is blue [1].", "NONCANON: Alternate archive is amber [2].")
                .doesNotContain("NONCANON: Core archive");
        assertThat(response.citations()).containsExactly(core, noncore);
    }

    @Test
    void unusedNoncoreEvidenceCannotRelabelAnEntireAnswer() {
        var core = createMatch("[1]", "core.md", "Core archive is blue.", 0, 21, "IDEAVERSE_CORE");
        var noncore = createMatch("[2]", "noncore.md", "Alternate archive is amber.", 0, 26, "IDEAVERSE_NONCORE");
        var response = validator.validate(UUID.randomUUID(), "ANSWERED", List.of(
                new RagQaClaim("a", "Core archive is blue.", List.of(new RagQaClaimSupport("[1]", "Core archive is blue")))
        ), List.of(), List.of(), List.of(core, noncore), "en");
        assertThat(response.answer()).doesNotContain("NONCANON");
        assertThat(response.citations()).containsExactly(core);
    }

    @Test
    @DisplayName("Citations correctly map exact quotes to multiple distinct sources")
    void multipleCitationsMappedCorrectly() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "doc1.md", "Vault uses Spring Modulith for architecture.", 0, 44);
        RagRetrievalMatch m2 = createMatch("[2]", "doc2.md", "PostgreSQL pgvector provides vector indexing.", 0, 46);

        RagQaClaim c1 = new RagQaClaim(
                "claim-1",
                "The system uses Spring Modulith.",
                List.of(new RagQaClaimSupport("[1]", "Spring Modulith"))
        );
        RagQaClaim c2 = new RagQaClaim(
                "claim-2",
                "Vector indexing is provided by PostgreSQL pgvector.",
                List.of(new RagQaClaimSupport("[2]", "pgvector provides"))
        );

        var response = validator.validate(
                reqId,
                "ANSWERED",
                List.of(c1, c2),
                List.of(),
                List.of(),
                List.of(m1, m2),
                "en"
        );

        assertThat(response.outcome()).isEqualTo(RagQaOutcome.ANSWERED);
        assertThat(response.citations()).hasSize(2);
        assertThat(response.citations().get(0).citationKey()).isEqualTo("[1]");
        assertThat(response.citations().get(1).citationKey()).isEqualTo("[2]");
        assertThat(response.answer()).contains("[1]");
        assertThat(response.answer()).contains("[2]");
    }

    @Test
    @DisplayName("Citations with unused matches in evidence retain only referenced matches")
    void unusedEvidenceMatchesAreFilteredFromCitations() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "used.md", "Target information is present here.", 0, 36);
        RagRetrievalMatch m2 = createMatch("[2]", "unused.md", "Irrelevant background noise.", 0, 28);

        RagQaClaim c1 = new RagQaClaim(
                "claim-1",
                "Found target info.",
                List.of(new RagQaClaimSupport("[1]", "Target information"))
        );

        var response = validator.validate(
                reqId,
                "ANSWERED",
                List.of(c1),
                List.of(),
                List.of(),
                List.of(m1, m2),
                "en"
        );

        assertThat(response.outcome()).isEqualTo(RagQaOutcome.ANSWERED);
        assertThat(response.citations()).containsExactly(m1);
        assertThat(response.citations()).doesNotContain(m2);
    }

    @Test
    @DisplayName("Claim referencing unknown citation key throws RagQueryUnavailableException")
    void unknownCitationKeyThrowsException() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "doc1.md", "Target text exists.", 0, 19);

        RagQaClaim claimWithBadKey = new RagQaClaim(
                "c1",
                "Statement.",
                List.of(new RagQaClaimSupport("[99]", "Target text"))
        );

        assertThatThrownBy(() -> validator.validate(
                reqId,
                "ANSWERED",
                List.of(claimWithBadKey),
                List.of(),
                List.of(),
                List.of(m1),
                "en"
        )).isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("references unknown citation key");
    }

    @Test
    @DisplayName("Claim with altered quote throws RagQueryUnavailableException")
    void alteredQuoteThrowsException() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "doc1.md", "The quick brown fox jumps over the lazy dog.", 0, 44);

        RagQaClaim claimWithHallucinatedQuote = new RagQaClaim(
                "c1",
                "Fox jumps over dog.",
                List.of(new RagQaClaimSupport("[1]", "The slow brown fox"))
        );

        assertThatThrownBy(() -> validator.validate(
                reqId,
                "ANSWERED",
                List.of(claimWithHallucinatedQuote),
                List.of(),
                List.of(),
                List.of(m1),
                "en"
        )).isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("is not an exact substring");
    }

    @Test
    @DisplayName("Multiple claims citing same match combine into single citation in list")
    void multipleClaimsCitingSameMatchDeduplicatesCitationList() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalMatch m1 = createMatch("[1]", "doc1.md", "Single document contains both premise and conclusion.", 0, 54);

        RagQaClaim c1 = new RagQaClaim("c1", "First statement.", List.of(new RagQaClaimSupport("[1]", "Single document")));
        RagQaClaim c2 = new RagQaClaim("c2", "Second statement.", List.of(new RagQaClaimSupport("[1]", "conclusion")));

        var response = validator.validate(
                reqId,
                "ANSWERED",
                List.of(c1, c2),
                List.of(),
                List.of(),
                List.of(m1),
                "en"
        );

        assertThat(response.citations()).containsExactly(m1);
        assertThat(response.claims()).hasSize(2);
    }
}
