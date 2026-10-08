package com.vhvkhangg.personalprivatevault.audit;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToNoteRequest;
import com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToStudyRequest;
import com.vhvkhangg.personalprivatevault.feed.resource.SavedResourceOperations;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateManualSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.view.SavedResourceView;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.CreateNoteCommand;
import com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteOperations;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BA15-6 regression tests:
 * Verifies that Feed and Import advices translate public Knowledge validation/conflict/not-found
 * exceptions safely without leaking internal details, and guarantees complete transactional
 * all-or-nothing rollback (no target entity created, no provenance recorded).
 */
class FeedImportKnowledgeExceptionIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private SavedResourceOperations savedResourceOperations;

    @Autowired
    private NoteOperations noteOperations;

    private Long createTestSavedResource(String title) {
        SavedResourceView view = savedResourceOperations.saveManual(new CreateManualSavedResourceCommand(
                SavedResourceKind.ARTICLE,
                title,
                "https://example.com/item-" + System.nanoTime(),
                "Author",
                "Summary",
                null,
                "Source",
                "https://example.com",
                null,
                null
        ));
        return view.id();
    }

    @Test
    @DisplayName("BA15-6: Duplicate Note hash triggers 409 KNOWLEDGE_CONFLICT and performs all-or-nothing rollback")
    void duplicateNoteHashTranslatesTo409WithFullRollback() throws Exception {
        // Pre-create a note with an imported file hash
        String duplicateHash = "sha256-abc123duplicate";
        noteOperations.create(new CreateNoteCommand(
                "Existing Note", "Some content", null, null, null, "file.md", duplicateHash, null
        ));

        Long resId = createTestSavedResource("Resource with duplicate note");
        Integer initialVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);

        // Attempt to convert to note with the same hash
        ConvertToNoteRequest request = new ConvertToNoteRequest(
                "New Note from Feed", "Content", "Summary", "Source", "https://example.com", "file.md", duplicateHash, null
        );

        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/note", resId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("KNOWLEDGE_CONFLICT"));

        // Rollback verification: No second note created, no dangling vault entry, no conversion recorded
        Integer noteCount = jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class);
        assertThat(noteCount).isEqualTo(1);

        Integer finalVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        assertThat(finalVaultCount).isEqualTo(initialVaultCount);

        Integer conversionCount = jdbcTemplate.queryForObject("SELECT count(*) FROM saved_resource_conversions", Integer.class);
        assertThat(conversionCount).isZero();
    }

    @Test
    @DisplayName("BA15-6: Invalid author combination triggers 422 INVALID_KNOWLEDGE_ITEM and rolls back")
    void invalidAuthorCombinationTranslatesTo422WithFullRollback() throws Exception {
        Long resId = createTestSavedResource("Resource with invalid author");
        Integer initialVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);

        // Setting both authorPersonId and authorGroupId is invalid according to Knowledge domain rules
        ConvertToStudyRequest request = new ConvertToStudyRequest(
                "Study With Invalid Authors", null, KnowledgeStudyType.BOOK, null, null, 9999L, 9999L, null, null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/saved-resources/{id}/convert/study", resId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_KNOWLEDGE_ITEM"));

        // Rollback verification: No study item created, no dangling vault entry, no conversion recorded
        Integer studyCount = jdbcTemplate.queryForObject("SELECT count(*) FROM study_items", Integer.class);
        assertThat(studyCount).isZero();

        Integer finalVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        assertThat(finalVaultCount).isEqualTo(initialVaultCount);

        Integer conversionCount = jdbcTemplate.queryForObject("SELECT count(*) FROM saved_resource_conversions", Integer.class);
        assertThat(conversionCount).isZero();
    }

    @Autowired
    private com.vhvkhangg.personalprivatevault.importdata.job.ImportJobOperations importJobOperations;

    @Test
    @DisplayName("BA15-6: Import job execution Knowledge Conflict (duplicate hash in job) triggers HTTP 409 and rolls back completely")
    void importJobExecutionKnowledgeConflictTriggers409AndRollsBack() throws Exception {
        String sharedHash = "sha256-intra-job-dup-" + System.nanoTime();

        // Create import job with 2 items sharing the same new hash (both valid at parse/validate time)
        String csv = """
                title,contentMarkdown,importedFileName,importedFileHash
                "Note 0","Content 0","file0.md","%s"
                "Note 1","Content 1","file1.md","%s"
                """.formatted(sharedHash, sharedHash);

        var job = importJobOperations.createJob(new com.vhvkhangg.personalprivatevault.importdata.job.command.CreateImportJobCommand(
                com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType.NOTE,
                com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat.CSV,
                "notes.csv", null
        ));

        importJobOperations.parse(job.id(), csv);
        importJobOperations.validate(job.id());

        Integer initialVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);

        // Pre-execution provenance check: both items VALID and unlinked
        var preItems = importJobOperations.findJobItems(job.id(), 10);
        assertThat(preItems).hasSize(2);
        assertThat(preItems.get(0).status()).isEqualTo(com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus.VALID);
        assertThat(preItems.get(0).importedVaultEntryId()).isNull();
        assertThat(preItems.get(1).status()).isEqualTo(com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus.VALID);
        assertThat(preItems.get(1).importedVaultEntryId()).isNull();

        // Execute via HTTP POST /api/v1/imports/jobs/{id}/execute with IMPORT decision for both
        String executePayload = """
                {
                  "itemDecisions": [
                    {"itemIndex": 0, "decision": "IMPORT"},
                    {"itemIndex": 1, "decision": "IMPORT"}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/imports/jobs/{id}/execute", job.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executePayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("KNOWLEDGE_CONFLICT"));

        // All-or-nothing rollback assertion: exactly 0 notes exist, item 0 was rolled back cleanly
        Integer noteCount = jdbcTemplate.queryForObject("SELECT count(*) FROM notes", Integer.class);
        assertThat(noteCount).isZero();

        // Vault entries count is unchanged: item 0 Vault entry was rolled back!
        Integer finalVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        assertThat(finalVaultCount).isEqualTo(initialVaultCount);

        // Job status remains VALIDATED, not IMPORTED
        String jobStatus = jdbcTemplate.queryForObject("SELECT status FROM import_jobs WHERE id = ?", String.class, job.id());
        assertThat(jobStatus).isEqualTo("VALIDATED");

        // Provenance rollback: item 0 and item 1 both retain VALID status and null importedVaultEntryId
        var postItems = importJobOperations.findJobItems(job.id(), 10);
        assertThat(postItems.get(0).status()).isEqualTo(com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus.VALID);
        assertThat(postItems.get(0).importedVaultEntryId()).isNull();
        assertThat(postItems.get(1).status()).isEqualTo(com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus.VALID);
        assertThat(postItems.get(1).importedVaultEntryId()).isNull();
    }

    @Test
    @DisplayName("BA15-6: Import job execution Invalid Knowledge Item (conflicting authors) triggers HTTP 422 and rolls back")
    void importJobExecutionInvalidKnowledgeItemTriggers422AndRollsBack() throws Exception {
        String csv = """
                title,type,learningStatus,url,authorPersonId,authorGroupId
                "Study With Both Authors",BOOK,PLANNED,"https://example.com/study",9999,9999
                """;

        var job = importJobOperations.createJob(new com.vhvkhangg.personalprivatevault.importdata.job.command.CreateImportJobCommand(
                com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType.STUDY,
                com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat.CSV,
                "study_invalid.csv", null
        ));

        importJobOperations.parse(job.id(), csv);
        importJobOperations.validate(job.id());

        Integer initialVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);

        String executePayload = """
                {
                  "itemDecisions": [
                    {"itemIndex": 0, "decision": "IMPORT"}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/imports/jobs/{id}/execute", job.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(executePayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("INVALID_KNOWLEDGE_ITEM"));

        Integer studyCount = jdbcTemplate.queryForObject("SELECT count(*) FROM study_items", Integer.class);
        assertThat(studyCount).isZero();

        Integer finalVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        assertThat(finalVaultCount).isEqualTo(initialVaultCount);

        String jobStatus = jdbcTemplate.queryForObject("SELECT status FROM import_jobs WHERE id = ?", String.class, job.id());
        assertThat(jobStatus).isEqualTo("VALIDATED");

        var items = importJobOperations.findJobItems(job.id(), 10);
        assertThat(items.get(0).status()).isEqualTo(com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus.VALID);
        assertThat(items.get(0).importedVaultEntryId()).isNull();
    }
}
