package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

/** Real Java authority -> native bound Python -> restricted SQL -> Java publication. */
@Timeout(90)
class RagNativeDocumentBatchIntegrationTest extends AbstractEnabledRagIntegrationTest {
    private RagSourceView source(int ordinal, boolean empty) {
        Long genre = jdbcTemplate.queryForObject("INSERT INTO fiction_genres(name) VALUES (?) RETURNING id",
                Long.class, UUID.randomUUID().toString());
        Long person = jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('PERSON') RETURNING id", Long.class);
        jdbcTemplate.update("INSERT INTO persons(id,name) VALUES (?,'Synthetic Author')", person);
        Long id = jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FICTION') RETURNING id", Long.class);
        jdbcTemplate.update("""
                INSERT INTO fictions(id,title,format,genre_id,author_person_id,description,is_nsfw)
                VALUES (?,?,'NOVEL',?,?,?,false)
                """, id, "Tiếng Việt 中文 繁體 title " + ordinal, genre, person, empty ? "" : "Approved description");
        var candidate = enabledRag.registerDomainCandidate(RagSourceType.FICTION, id.toString(),
                RagClassification.NON_PRIVATE, null, null);
        Set<String> fields = Set.of(empty ? "description" : "title");
        return enabledRag.approveSource(candidate.id(), new RagConsentCommand(fields,
                candidate.sourceVersion(), candidate.consentResetVersion(), candidate.authorizationEpoch(),
                candidate.fieldPolicyVersion(), empty ? candidate.currentDescriptionRevisionHash() : null));
    }

    @Test void realNativeEightSuccessTwoFailurePublishesAtomicallyAndReconcilesLostAck() {
        var sources = new ArrayList<RagSourceView>();
        var roots = new ArrayList<RagDocumentRootItem>();
        var tasks = new ArrayList<RagDocumentBatchTask>();
        for (int i = 0; i < 10; i++) {
            var source = source(i, i >= 8); sources.add(source);
            String key = "root-" + i;
            roots.add(new RagDocumentRootItem(key, source.sourceType(), source.sourceId(), source.sourceVersion(),
                    source.authorizationEpoch(), source.fieldPolicyVersion(), source.approvedFields()));
            tasks.add(new RagDocumentBatchTask(key, source.sourceId(), "DOMAIN", null, null, null,
                    source.approvedFields().stream().sorted().toList(), Map.of()));
        }
        var admitted = enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(roots, Map.of()));
        enabledRag.executeDocumentBatch(admitted.jobId(), admitted.attemptId(), tasks);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_document_outputs WHERE job_id=?",
                Integer.class, admitted.jobId())).isEqualTo(10);
        String manifest = jdbcTemplate.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?",
                String.class, admitted.jobId());
        var command = new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest));
        var result = enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), command);
        assertThat(result.jobStatus()).isEqualTo("COMPLETED");
        assertThat(result.publishedCount()).isEqualTo(8);
        assertThat(result.failedCount()).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.document_chunks", Integer.class)).isEqualTo(8);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL",
                Integer.class)).isZero();
        assertThat(enabledRag.finalizeDocumentBatch(admitted.jobId(), admitted.attemptId(), command))
                .isEqualTo(result);
        var report = enabledRag.getDocumentBatchReport(admitted.jobId());
        assertThat(report.summaryOutcome()).isEqualTo("PARTIAL_SUCCESS");
        assertThat(report.outcomes().stream().filter(o -> "SUCCESS".equals(o.result())))
                .allMatch(o -> "CURRENT".equals(o.currentIndexState()));
    }

    @Test void callerBodyCannotReplaceAnApprovedProjection() {
        var source = source(1, false);
        var admitted = enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(
                new RagDocumentRootItem("root", source.sourceType(), source.sourceId(), source.sourceVersion(),
                        source.authorizationEpoch(), source.fieldPolicyVersion(), source.approvedFields())
        ), Map.of()));
        var forged = new RagDocumentBatchTask("root", source.sourceId(), "DOMAIN", "unapproved caller body",
                null, null, source.approvedFields().stream().toList(), Map.of());
        assertThatThrownBy(() -> enabledRag.executeDocumentBatch(admitted.jobId(), admitted.attemptId(), List.of(forged)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_document_outputs WHERE job_id=?",
                Integer.class, admitted.jobId())).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",
                String.class, admitted.jobId())).isEqualTo("ABORTED");
    }
}
