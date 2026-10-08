package com.vhvkhangg.personalprivatevault.audit;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemDecision;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportJobStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import com.vhvkhangg.personalprivatevault.importdata.job.ImportJobOperations;
import com.vhvkhangg.personalprivatevault.importdata.job.command.CreateImportJobCommand;
import com.vhvkhangg.personalprivatevault.importdata.job.command.ExecuteImportJobCommand;
import com.vhvkhangg.personalprivatevault.importdata.job.command.ImportItemDecisionInput;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportJobException;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobItemView;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobView;
import com.vhvkhangg.personalprivatevault.knowledge.api.InvalidKnowledgeItemException;
import com.vhvkhangg.personalprivatevault.knowledge.study.study.StudyItemOperations;
import com.vhvkhangg.personalprivatevault.knowledge.study.view.StudyItemView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BA15-14 regression tests:
 * Verifies exact 100-item and 101-item job boundaries, default HTTP pagination compatibility,
 * page/limit input rejections, execution and entity persistence beyond the first page,
 * and atomic all-or-nothing failure rollback.
 */
class ImportJobPaginationIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private ImportJobOperations importJobOperations;

    @Autowired
    private StudyItemOperations studyItemOperations;

    @Test
    @DisplayName("BA15-14: Exact 100-item job boundary, default pagination compatibility, and boundary item execution")
    void exact100ItemJobBoundaryAndDefaultPaginationCompatibility() throws Exception {
        // 1. Build CSV with exactly 100 items (indices 0..99)
        StringBuilder csv = new StringBuilder("title,type,learningStatus,url,description\n");
        for (int i = 0; i < 100; i++) {
            csv.append("\"Book ").append(i).append("\",BOOK,PLANNED,\"https://example.com/").append(i).append("\",\"Desc ").append(i).append("\"\n");
        }

        ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                ImportTargetType.STUDY, ImportFormat.CSV, "100_books.csv", null
        ));
        Long jobId = job.id();

        ImportJobView parsed = importJobOperations.parse(jobId, csv.toString());
        assertThat(parsed.totalItems()).isEqualTo(100);

        // 2. Default HTTP pagination compatibility (no query params -> defaults to page=0, limit=50; meta is null)
        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(50))
                .andExpect(jsonPath("$.data[0].itemIndex").value(0))
                .andExpect(jsonPath("$.data[49].itemIndex").value(49));

        // 3. Page 0 with limit 100 -> returns exactly 100 items (indices 0..99; meta is null)
        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "0")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(100))
                .andExpect(jsonPath("$.data[0].itemIndex").value(0))
                .andExpect(jsonPath("$.data[99].itemIndex").value(99));

        // 4. Page 1 with limit 100 -> returns 0 items (boundary check: no 101st item)
        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "1")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(0));

        // 5. Paginated inspection across pages (page 0 limit 50 and page 1 limit 50):
        // Verify complete contiguous sequence with no omissions and no duplicates
        List<Integer> combined100Indices = new ArrayList<>();

        var page0Result = mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "0")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(50))
                .andExpect(jsonPath("$.data[0].itemIndex").value(0))
                .andExpect(jsonPath("$.data[49].itemIndex").value(49))
                .andReturn().getResponse().getContentAsString();

        var page0Json = objectMapper.readTree(page0Result).path("data");
        for (var item : page0Json) {
            combined100Indices.add(item.path("itemIndex").asInt());
        }

        // 6. Page 1 with limit 50 -> exactly 50 items (50..99)
        var page1Result = mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "1")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(50))
                .andExpect(jsonPath("$.data[0].itemIndex").value(50))
                .andExpect(jsonPath("$.data[49].itemIndex").value(99))
                .andReturn().getResponse().getContentAsString();

        var page1Json = objectMapper.readTree(page1Result).path("data");
        for (var item : page1Json) {
            combined100Indices.add(item.path("itemIndex").asInt());
        }

        // Assert combined sequence across pages has no omissions and no duplicates
        assertThat(combined100Indices).hasSize(100);
        assertThat(combined100Indices).containsExactlyElementsOf(
                java.util.stream.IntStream.range(0, 100).boxed().toList()
        );

        // 7. Page 2 with limit 50 -> 0 items
        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "2")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(0));

        // 8. Validate job
        importJobOperations.validate(jobId);

        // 9. Execute decisions: import boundary item 99, skip 0..98
        List<ImportItemDecisionInput> decisions = new ArrayList<>();
        for (int i = 0; i < 99; i++) {
            decisions.add(new ImportItemDecisionInput(i, ImportItemDecision.SKIP));
        }
        decisions.add(new ImportItemDecisionInput(99, ImportItemDecision.IMPORT));

        ImportJobView executed = importJobOperations.execute(jobId, new ExecuteImportJobCommand(decisions));
        assertThat(executed.status()).isEqualTo(ImportJobStatus.IMPORTED);
        assertThat(executed.importedItems()).isEqualTo(1);

        // 10. Verify boundary item 99 is persisted
        List<ImportJobItemView> page1Items = importJobOperations.findJobItems(jobId, 1, 50);
        ImportJobItemView item99 = page1Items.get(49);
        assertThat(item99.itemIndex()).isEqualTo(99);
        assertThat(item99.status()).isEqualTo(ImportItemStatus.IMPORTED);
        assertThat(item99.importedVaultEntryId()).isNotNull();

        Optional<StudyItemView> entity99 = studyItemOperations.findById(item99.importedVaultEntryId());
        assertThat(entity99).isPresent();
        assertThat(entity99.get().title()).isEqualTo("Book 99");
    }

    @Test
    @DisplayName("BA15-14: Exact 101-item job boundary and execution beyond first page (page 1 item 100)")
    void exact101ItemJobBoundaryWithExecutionBeyondFirstPage() throws Exception {
        // 1. Build CSV with exactly 101 items (indices 0..100)
        StringBuilder csv = new StringBuilder("title,type,learningStatus,url,description\n");
        for (int i = 0; i < 101; i++) {
            csv.append("\"Book ").append(i).append("\",BOOK,PLANNED,\"https://example.com/").append(i).append("\",\"Desc ").append(i).append("\"\n");
        }

        ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                ImportTargetType.STUDY, ImportFormat.CSV, "101_books.csv", null
        ));
        Long jobId = job.id();

        ImportJobView parsed = importJobOperations.parse(jobId, csv.toString());
        assertThat(parsed.totalItems()).isEqualTo(101);

        // 2. HTTP: Page 0 with limit 100 -> exactly 100 items (0..99; meta is null)
        List<Integer> combined101Indices = new ArrayList<>();

        var page0Result = mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "0")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(100))
                .andExpect(jsonPath("$.data[0].itemIndex").value(0))
                .andExpect(jsonPath("$.data[99].itemIndex").value(99))
                .andReturn().getResponse().getContentAsString();

        for (var item : objectMapper.readTree(page0Result).path("data")) {
            combined101Indices.add(item.path("itemIndex").asInt());
        }

        // 3. HTTP: Page 1 with limit 100 -> exactly 1 item (index 100, on page 1 beyond the first page; meta is null)
        var page1Result = mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "1")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].itemIndex").value(100))
                .andReturn().getResponse().getContentAsString();

        for (var item : objectMapper.readTree(page1Result).path("data")) {
            combined101Indices.add(item.path("itemIndex").asInt());
        }

        // Assert combined sequence across pages has all 101 items in strict order with no omissions and no duplicates
        assertThat(combined101Indices).hasSize(101);
        assertThat(combined101Indices).containsExactlyElementsOf(
                java.util.stream.IntStream.range(0, 101).boxed().toList()
        );

        // 4. Validate job
        importJobOperations.validate(jobId);

        // 5. Execute decisions: import index 0 (page 0) AND index 100 (page 1, beyond first page), skip 1..99
        List<ImportItemDecisionInput> decisions = new ArrayList<>();
        decisions.add(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT));
        for (int i = 1; i < 100; i++) {
            decisions.add(new ImportItemDecisionInput(i, ImportItemDecision.SKIP));
        }
        decisions.add(new ImportItemDecisionInput(100, ImportItemDecision.IMPORT));

        ImportJobView executed = importJobOperations.execute(jobId, new ExecuteImportJobCommand(decisions));
        assertThat(executed.status()).isEqualTo(ImportJobStatus.IMPORTED);
        assertThat(executed.importedItems()).isEqualTo(2);

        // 6. Verify page 0 item (index 0) was imported and entity is present
        ImportJobItemView item0 = importJobOperations.findJobItems(jobId, 0, 1).getFirst();
        assertThat(item0.itemIndex()).isEqualTo(0);
        assertThat(item0.status()).isEqualTo(ImportItemStatus.IMPORTED);
        assertThat(item0.importedVaultEntryId()).isNotNull();

        Optional<StudyItemView> entity0 = studyItemOperations.findById(item0.importedVaultEntryId());
        assertThat(entity0).isPresent();
        assertThat(entity0.get().title()).isEqualTo("Book 0");

        // 7. Verify page 1 item (index 100) was imported beyond first page and entity is present
        ImportJobItemView item100 = importJobOperations.findJobItems(jobId, 1, 100).getFirst();
        assertThat(item100.itemIndex()).isEqualTo(100);
        assertThat(item100.status()).isEqualTo(ImportItemStatus.IMPORTED);
        assertThat(item100.importedVaultEntryId()).isNotNull();

        Optional<StudyItemView> entity100 = studyItemOperations.findById(item100.importedVaultEntryId());
        assertThat(entity100).isPresent();
        assertThat(entity100.get().title()).isEqualTo("Book 100");
    }

    @Test
    @DisplayName("BA15-14: Pagination query validation, decision completeness, and OpenAPI parameter schema bounds")
    void paginationInputRejectionsAndIncompleteDecisions() throws Exception {
        StringBuilder csv = new StringBuilder("title,type,learningStatus,url,description\n");
        for (int i = 0; i < 10; i++) {
            csv.append("\"Book ").append(i).append("\",BOOK,PLANNED,\"https://example.com/").append(i).append("\",\"Desc ").append(i).append("\"\n");
        }

        ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                ImportTargetType.STUDY, ImportFormat.CSV, "10_books.csv", null
        ));
        Long jobId = job.id();
        importJobOperations.parse(jobId, csv.toString());

        // HTTP input rejections
        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "-1")
                        .param("limit", "50"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "0")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "0")
                        .param("limit", "101"))
                .andExpect(status().isBadRequest());

        // OpenAPI parameter schema bounds verification
        String apiDocs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var root = objectMapper.readTree(apiDocs);
        var params = root.path("paths").path("/api/v1/imports/jobs/{id}/items").path("get").path("parameters");
        boolean checkedPage = false;
        boolean checkedLimit = false;
        for (var param : params) {
            String name = param.path("name").asText();
            if ("page".equals(name)) {
                var schema = param.path("schema");
                assertThat(schema.has("default")).isTrue();
                assertThat(schema.path("default").asInt()).isEqualTo(0);
                assertThat(schema.has("minimum")).isTrue();
                assertThat(schema.path("minimum").asInt()).isEqualTo(0);
                assertThat(param.path("description").asText()).contains("beyond integer capacity return an empty list");
                checkedPage = true;
            } else if ("limit".equals(name)) {
                var schema = param.path("schema");
                assertThat(schema.has("default")).isTrue();
                assertThat(schema.path("default").asInt()).isEqualTo(50);
                assertThat(schema.has("maximum")).isTrue();
                assertThat(schema.path("maximum").asInt()).isEqualTo(100);

                // Lower bound must strictly exclude zero (reject missing bounds, non-exclusive minimum=0, etc.)
                boolean excludesZero = false;
                if (schema.has("exclusiveMinimum")) {
                    if (schema.path("exclusiveMinimum").isBoolean()) {
                        assertThat(schema.path("exclusiveMinimum").asBoolean()).isTrue();
                        assertThat(schema.has("minimum")).isTrue();
                        assertThat(schema.path("minimum").asInt()).isEqualTo(0);
                        excludesZero = true;
                    } else {
                        assertThat(schema.path("exclusiveMinimum").asInt()).isEqualTo(0);
                        excludesZero = true;
                    }
                } else if (schema.has("minimum")) {
                    assertThat(schema.path("minimum").asInt()).isEqualTo(1);
                    excludesZero = true;
                }
                assertThat(excludesZero)
                        .as("limit schema must declare a lower bound strictly excluding zero")
                        .isTrue();
                checkedLimit = true;
            }
        }
        assertThat(checkedPage).isTrue();
        assertThat(checkedLimit).isTrue();

        // Programmatic service query rejections
        assertThatThrownBy(() -> importJobOperations.findJobItems(jobId, -1, 50))
                .isInstanceOf(InvalidImportJobException.class)
                .hasMessageContaining("Page must be non-negative");

        assertThatThrownBy(() -> importJobOperations.findJobItems(jobId, 0, 0))
                .isInstanceOf(InvalidImportJobException.class)
                .hasMessageContaining("Limit must be positive");

        importJobOperations.validate(jobId);

        // Incomplete decisions rejection (9 decisions instead of 10)
        List<ImportItemDecisionInput> incomplete = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            incomplete.add(new ImportItemDecisionInput(i, ImportItemDecision.SKIP));
        }
        assertThatThrownBy(() -> importJobOperations.execute(jobId, new ExecuteImportJobCommand(incomplete)))
                .isInstanceOf(InvalidImportJobException.class)
                .hasMessageContaining("does not match job total items");
    }

    @Test
    @DisplayName("BA15-14 / BA15-6: Atomic all-or-nothing rollback when item beyond first page fails execution")
    void atomicFailureRollbackBeyondFirstPageLeavesTargetAndJobUnchanged() {
        // Build 101 items: index 0 is valid, index 1..99 valid, index 100 has conflicting authors (both authorPersonId and authorGroupId)
        StringBuilder csv = new StringBuilder("title,type,learningStatus,url,description,authorPersonId,authorGroupId\n");
        for (int i = 0; i < 100; i++) {
            csv.append("\"Atomic Book ").append(i).append("\",BOOK,PLANNED,\"https://example.com/").append(i).append("\",\"Desc\",,\n");
        }
        // Item 100 has both authorPersonId=1 and authorGroupId=2 -> Knowledge throws InvalidKnowledgeItemException
        csv.append("\"Atomic Book 100\",BOOK,PLANNED,\"https://example.com/100\",\"Desc\",1,2\n");

        ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                ImportTargetType.STUDY, ImportFormat.CSV, "101_atomic_books.csv", null
        ));
        Long jobId = job.id();
        importJobOperations.parse(jobId, csv.toString());
        importJobOperations.validate(jobId);

        Integer initialStudyCount = jdbcTemplate.queryForObject("SELECT count(*) FROM study_items", Integer.class);
        Integer initialVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);

        // Pre-execute check on item 0
        var prePage0 = importJobOperations.findJobItems(jobId, 0, 1);
        assertThat(prePage0.getFirst().status()).isEqualTo(ImportItemStatus.VALID);
        assertThat(prePage0.getFirst().importedVaultEntryId()).isNull();

        // Decisions: import index 0 (page 0), skip 1..99, import index 100 (page 1)
        List<ImportItemDecisionInput> decisions = new ArrayList<>();
        decisions.add(new ImportItemDecisionInput(0, ImportItemDecision.IMPORT));
        for (int i = 1; i < 100; i++) {
            decisions.add(new ImportItemDecisionInput(i, ImportItemDecision.SKIP));
        }
        decisions.add(new ImportItemDecisionInput(100, ImportItemDecision.IMPORT));

        assertThatThrownBy(() -> importJobOperations.execute(jobId, new ExecuteImportJobCommand(decisions)))
                .isInstanceOf(InvalidKnowledgeItemException.class)
                .hasMessageContaining("Study item cannot have both");

        // Verify all-or-nothing rollback: study count is unchanged (item 0 rolled back!)
        Integer finalStudyCount = jdbcTemplate.queryForObject("SELECT count(*) FROM study_items", Integer.class);
        assertThat(finalStudyCount).isEqualTo(initialStudyCount);

        // Vault entries count is unchanged (item 0 Vault entry was rolled back!)
        Integer finalVaultCount = jdbcTemplate.queryForObject("SELECT count(*) FROM vault_entries", Integer.class);
        assertThat(finalVaultCount).isEqualTo(initialVaultCount);

        // Verify job status remains VALIDATED (not IMPORTED)
        ImportJobView reloadedJob = importJobOperations.findJobById(jobId).orElseThrow();
        assertThat(reloadedJob.status()).isEqualTo(ImportJobStatus.VALIDATED);
        assertThat(reloadedJob.importedItems()).isZero();

        // Verify provenance of all items across both pages remains VALID and unlinked
        var postPage0 = importJobOperations.findJobItems(jobId, 0, 100);
        assertThat(postPage0).allMatch(i -> i.status() == ImportItemStatus.VALID && i.importedVaultEntryId() == null);
        var postPage1 = importJobOperations.findJobItems(jobId, 1, 100);
        assertThat(postPage1).allMatch(i -> i.status() == ImportItemStatus.VALID && i.importedVaultEntryId() == null);
    }

    @Test
    @DisplayName("BA15-14: Extreme page index and supported-offset threshold boundaries return bounded empty response without 500 or writes")
    void extremePageAndOffsetThresholdBoundariesReturnBoundedEmptyWithout500OrWrites() throws Exception {
        ImportJobView job = importJobOperations.createJob(new CreateImportJobCommand(
                ImportTargetType.STUDY, ImportFormat.CSV, "boundary_books.csv", null
        ));
        Long jobId = job.id();
        importJobOperations.parse(jobId, "title,type\n\"Book 1\",BOOK\n");

        int initialItemCount = jdbcTemplate.queryForObject("SELECT count(*) FROM import_job_items", Integer.class);

        // 1. Extreme page value: page = Integer.MAX_VALUE with limit = 100 -> returns 200 with empty data array
        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", String.valueOf(Integer.MAX_VALUE))
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(0));

        // 2. Limit 100: threshold = 21474836, threshold + 1 = 21474837
        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "21474836")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "21474837")
                        .param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(0));

        // 3. Limit 50: threshold = 42949672, threshold + 1 = 42949673
        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "42949672")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/api/v1/imports/jobs/{id}/items", jobId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .param("page", "42949673")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta").doesNotExist())
                .andExpect(jsonPath("$.data.length()").value(0));

        // 4. Programmatic service query check
        assertThat(importJobOperations.findJobItems(jobId, Integer.MAX_VALUE, 100)).isEmpty();
        assertThat(importJobOperations.findJobItems(jobId, 21474837, 100)).isEmpty();

        // 5. Zero database writes occurred
        int finalItemCount = jdbcTemplate.queryForObject("SELECT count(*) FROM import_job_items", Integer.class);
        assertThat(finalItemCount).isEqualTo(initialItemCount);
    }
}
