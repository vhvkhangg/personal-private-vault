package com.vhvkhangg.personalprivatevault.audit;

import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;
import com.vhvkhangg.personalprivatevault.finance.view.WalletView;
import com.vhvkhangg.personalprivatevault.finance.wallet.WalletOperations;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.CreateWalletCommand;
import com.vhvkhangg.personalprivatevault.location.address.AddressOperations;
import com.vhvkhangg.personalprivatevault.location.address.CreateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.location.CreateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.location.LocationOperations;
import com.vhvkhangg.personalprivatevault.location.view.LocationView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BA15-4 regression tests:
 * Verifies canonical HTTP 400 / no-write behavior when request DTO lists contain null members
 * across Import, Account, Finance (create & update), and Location, while retaining legitimate
 * empty/null list semantics.
 */
class NullCollectionMemberValidationTest extends AbstractWebIntegrationTest {

    @Autowired
    private WalletOperations walletOperations;

    @Autowired
    private LocationOperations locationOperations;

    @Autowired
    private AddressOperations addressOperations;

    @Autowired
    private com.vhvkhangg.personalprivatevault.account.account.ExternalAccountOperations externalAccountOperations;

    @Autowired
    private com.vhvkhangg.personalprivatevault.importdata.job.ImportJobOperations importJobOperations;

    @Test
    @DisplayName("BA15-4: Import execute request with null decision member returns HTTP 400 VALIDATION_ERROR on itemDecisions[0] without job or item writes")
    void importExecuteRejectsNullMember() throws Exception {
        var job = importJobOperations.createJob(new com.vhvkhangg.personalprivatevault.importdata.job.command.CreateImportJobCommand(
                com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType.NOTE,
                com.vhvkhangg.personalprivatevault.importdata.enums.ImportFormat.CSV,
                "null_decision_fixture.csv", null
        ));
        importJobOperations.parse(job.id(), "title,contentMarkdown\n\"Note Fixture\",\"Content Fixture\"\n");
        importJobOperations.validate(job.id());

        String payload = """
                {
                  "itemDecisions": [null]
                }
                """;
        mockMvc.perform(post("/api/v1/imports/jobs/{id}/execute", job.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("itemDecisions[0]"));

        // Persisted job status remains VALIDATED, importedItems is 0, and items remain unchanged
        var reloadedJob = importJobOperations.findJobById(job.id()).orElseThrow();
        assertThat(reloadedJob.status()).isEqualTo(com.vhvkhangg.personalprivatevault.importdata.enums.ImportJobStatus.VALIDATED);
        assertThat(reloadedJob.importedItems()).isZero();

        var items = importJobOperations.findJobItems(job.id(), 10);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).status()).isEqualTo(com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus.VALID);
        assertThat(items.get(0).importedVaultEntryId()).isNull();
    }

    @Test
    @DisplayName("BA15-4: Account follower snapshot request with null entry returns HTTP 400 VALIDATION_ERROR on entries[0], and valid empty entries succeeds")
    void followerSnapshotRejectsNullMember() throws Exception {
        Long platformId = jdbcTemplate.query(
                "SELECT id FROM platforms LIMIT 1",
                (rs, rowNum) -> rs.getLong("id")
        ).stream().findFirst().orElseGet(() -> jdbcTemplate.queryForObject(
                "INSERT INTO platforms (name, kind, url) VALUES ('SnapPlatform', 'SOCIAL'::platform_kind, 'https://example.com') RETURNING id",
                Long.class
        ));

        var account = externalAccountOperations.create(new com.vhvkhangg.personalprivatevault.account.account.CreateExternalAccountCommand(
                platformId,
                com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership.TRACKED,
                com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType.SOCIAL,
                "user_snap_" + System.nanoTime(),
                "EXT_SNAP_" + System.nanoTime(),
                "User Snap",
                null, null, null, null, "https://example.com/user", null
        ));

        // 1. Post with entries: [null] -> 400 VALIDATION_ERROR with entries[0] field error
        String payload = """
                {
                  "capturedAt": "2026-10-07T12:00:00Z",
                  "source": "MANUAL",
                  "reportedTotalCount": 10,
                  "entries": [null]
                }
                """;
        mockMvc.perform(post("/api/v1/accounts/{id}/snapshots", account.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("entries[0]"));

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshots WHERE owner_account_id = ?", Integer.class, account.id());
        assertThat(count).isZero();

        // 2. Valid control: entries: [] succeeds
        String validPayload = """
                {
                  "capturedAt": "2026-10-07T12:00:00Z",
                  "source": "MANUAL",
                  "reportedTotalCount": 0,
                  "entries": []
                }
                """;
        mockMvc.perform(post("/api/v1/accounts/{id}/snapshots", account.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isCreated());

        Integer countAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM follower_snapshots WHERE owner_account_id = ?", Integer.class, account.id());
        assertThat(countAfter).isEqualTo(1);
    }

    @Test
    @DisplayName("BA15-4: Finance transaction create and update reject null entry in entries list with entries[0] field error")
    void financeTransactionRejectsNullMember() throws Exception {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand("Checking", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true));

        // Create transaction with [null] entry -> 400
        String createPayloadWithNull = """
                {
                  "type": "EXPENSE",
                  "occurredAt": "%s",
                  "exchangeRate": 1.0,
                  "entries": [null]
                }
                """.formatted(Instant.now());
        mockMvc.perform(post("/api/v1/finance/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayloadWithNull))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("entries[0]"));

        Integer txCount = jdbcTemplate.queryForObject("SELECT count(*) FROM financial_transactions", Integer.class);
        assertThat(txCount).isZero();

        // Create valid transaction first
        String validCreate = """
                {
                  "type": "EXPENSE",
                  "occurredAt": "%s",
                  "exchangeRate": 1.0,
                  "entries": [
                    {
                      "walletId": %d,
                      "amountDelta": -10.00
                    }
                  ]
                }
                """.formatted(Instant.now(), wallet.id());
        String res = mockMvc.perform(post("/api/v1/finance/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreate))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long txId = objectMapper.readTree(res).path("data").path("id").asLong();

        // Update transaction with deliberately changed scalar controls + [null] entry -> 400 and unchanged state
        String updatePayloadWithNull = """
                {
                  "type": "EXPENSE",
                  "description": "Mutated Description",
                  "occurredAt": "%s",
                  "exchangeRate": 2.5,
                  "entries": [null]
                }
                """.formatted(Instant.now());
        mockMvc.perform(put("/api/v1/finance/transactions/{id}", txId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayloadWithNull))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("entries[0]"));

        // Persisted state comparison: transaction scalar fields and entries are completely unchanged
        var txRow = jdbcTemplate.queryForMap("SELECT description, exchange_rate FROM financial_transactions WHERE id = ?", txId);
        assertThat(txRow.get("description")).isNull();
        assertThat(new BigDecimal(txRow.get("exchange_rate").toString())).isEqualByComparingTo(BigDecimal.valueOf(1.0));

        var entries = jdbcTemplate.queryForList("SELECT wallet_id, amount_delta FROM financial_transaction_entries WHERE transaction_id = ?", txId);
        assertThat(entries).hasSize(1);
        assertThat(((Number) entries.get(0).get("wallet_id")).longValue()).isEqualTo(wallet.id());
        assertThat(new BigDecimal(entries.get(0).get("amount_delta").toString())).isEqualByComparingTo(new BigDecimal("-10.0000"));
    }

    @Test
    @DisplayName("BA15-4: Finance recurring rule create and update reject null entry in entries list with entries[0] field error and no writes")
    void financeRecurringRuleRejectsNullMember() throws Exception {
        WalletView wallet = walletOperations.createWallet(new CreateWalletCommand("Savings", WalletType.BANK_ACCOUNT, "USD", BigDecimal.ZERO, null, true));

        // 1. Create with entries: [null] -> 400 VALIDATION_ERROR on entries[0], no write
        String createPayloadWithNull = """
                {
                  "name": "Gym Rule",
                  "transactionType": "EXPENSE",
                  "postingMode": "AUTO_POST",
                  "frequency": "MONTHLY",
                  "dayOfMonth": 1,
                  "startDate": "2026-10-01",
                  "entries": [null]
                }
                """;
        mockMvc.perform(post("/api/v1/finance/recurring-rules")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayloadWithNull))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("entries[0]"));

        Integer ruleCount = jdbcTemplate.queryForObject("SELECT count(*) FROM recurring_transaction_rules", Integer.class);
        assertThat(ruleCount).isZero();

        // 2. Create valid recurring rule
        String validCreate = """
                {
                  "name": "Gym Valid Rule",
                  "description": "Original Rule Description",
                  "transactionType": "EXPENSE",
                  "postingMode": "AUTO_POST",
                  "frequency": "MONTHLY",
                  "dayOfMonth": 1,
                  "startDate": "2026-10-01",
                  "entries": [
                    {
                      "walletId": %d,
                      "amountDelta": -50.00
                    }
                  ]
                }
                """.formatted(wallet.id());
        String res = mockMvc.perform(post("/api/v1/finance/recurring-rules")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreate))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long ruleId = objectMapper.readTree(res).path("data").path("id").asLong();

        // 3. Update with deliberately changed name + description and entries: [null] -> 400 VALIDATION_ERROR on entries[0]
        String updatePayloadWithNull = """
                {
                  "name": "Gym Mutated Rule",
                  "description": "Mutated Rule Description",
                  "transactionType": "EXPENSE",
                  "postingMode": "AUTO_POST",
                  "frequency": "MONTHLY",
                  "dayOfMonth": 1,
                  "startDate": "2026-10-01",
                  "entries": [null]
                }
                """;
        mockMvc.perform(put("/api/v1/finance/recurring-rules/{id}", ruleId)
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayloadWithNull))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("entries[0]"));

        // Persisted state comparison: name and description remain original, entries remain unchanged
        var ruleRow = jdbcTemplate.queryForMap("SELECT name, description FROM recurring_transaction_rules WHERE id = ?", ruleId);
        assertThat(ruleRow.get("name")).isEqualTo("Gym Valid Rule");
        assertThat(ruleRow.get("description")).isEqualTo("Original Rule Description");

        var ruleEntries = jdbcTemplate.queryForList("SELECT wallet_id, amount_delta FROM recurring_rule_entries WHERE recurring_rule_id = ?", ruleId);
        assertThat(ruleEntries).hasSize(1);
        assertThat(((Number) ruleEntries.get(0).get("wallet_id")).longValue()).isEqualTo(wallet.id());
        assertThat(new BigDecimal(ruleEntries.get(0).get("amount_delta").toString())).isEqualByComparingTo(new BigDecimal("-50.0000"));
    }

    @Test
    @DisplayName("BA15-4: Location schedule rejects null intervals element on intervals[0] without writes, but accepts empty and null schedules")
    void locationScheduleNullMemberAndEmptyHandling() throws Exception {
        var addr = addressOperations.create(new CreateAddressCommand("Addr", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null, addr.id(), "Branch", null, null, null, null, null, null, null, null
        ));

        // 1. Null member in intervals -> 400 Bad Request (VALIDATION_ERROR) on intervals[0]
        String payloadWithNull = """
                {
                  "businessHoursKnown": true,
                  "intervals": [null]
                }
                """;
        mockMvc.perform(put("/api/v1/locations/{id}/business-hours", location.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWithNull))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("intervals[0]"));

        // Persisted check: no business hours intervals written
        Integer bhCount = jdbcTemplate.queryForObject("SELECT count(*) FROM location_business_hours WHERE location_id = ?", Integer.class, location.id());
        assertThat(bhCount).isZero();

        // 2. Empty intervals list [] -> 200 OK (supported empty schedule)
        String emptySchedule = """
                {
                  "businessHoursKnown": true,
                  "intervals": []
                }
                """;
        mockMvc.perform(put("/api/v1/locations/{id}/business-hours", location.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptySchedule))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessHoursKnown").value(true))
                .andExpect(jsonPath("$.data.intervals").isArray());

        // 3. Unknown schedule with empty intervals [] -> 200 OK
        String unknownEmptySchedule = """
                {
                  "businessHoursKnown": false,
                  "intervals": []
                }
                """;
        mockMvc.perform(put("/api/v1/locations/{id}/business-hours", location.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unknownEmptySchedule))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessHoursKnown").value(false));

        // 4. Null intervals -> 200 OK (supported null/cleared unknown schedule)
        String nullSchedule = """
                {
                  "businessHoursKnown": false,
                  "intervals": null
                }
                """;
        mockMvc.perform(put("/api/v1/locations/{id}/business-hours", location.id())
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nullSchedule))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessHoursKnown").value(false));
    }
}
