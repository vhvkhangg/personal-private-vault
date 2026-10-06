package com.vhvkhangg.personalprivatevault.finance;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FinanceWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("Finance Wallets, Categories, Transactions, and Recurring Rules endpoints work over HTTP")
    void financeLifecycleOverHttp() throws Exception {
        // 1. Create wallet -> 201 Created
        String walletName = "Cash Wallet " + System.currentTimeMillis();
        String walletPayload = """
                {
                  "name": "%s",
                  "type": "CASH",
                  "currencyCode": "USD",
                  "openingBalance": 1000.00
                }
                """.formatted(walletName);

        String walletResponse = mockMvc.perform(post("/api/v1/finance/wallets")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(walletPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(walletName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number walletId = objectMapper.readTree(walletResponse).path("data").path("id").numberValue();

        // 2. Create category -> 201 Created
        String catName = "Groceries " + System.currentTimeMillis();
        String catPayload = """
                {
                  "name": "%s",
                  "kind": "EXPENSE"
                }
                """.formatted(catName);

        String catResponse = mockMvc.perform(post("/api/v1/finance/categories")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(catPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value(catName))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number catId = objectMapper.readTree(catResponse).path("data").path("id").numberValue();

        // 3. Create transaction -> 201 Created
        String txPayload = """
                {
                  "type": "EXPENSE",
                  "categoryId": %d,
                  "description": "Weekly groceries",
                  "occurredAt": "2026-10-05T09:00:00Z",
                  "entries": [
                    {
                      "walletId": %d,
                      "amountDelta": -45.50
                    }
                  ]
                }
                """.formatted(catId, walletId);

        String txResponse = mockMvc.perform(post("/api/v1/finance/transactions")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(txPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.description").value("Weekly groceries"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number txId = objectMapper.readTree(txResponse).path("data").path("id").numberValue();

        // 4. Get transaction by ID -> 200 OK
        mockMvc.perform(get("/api/v1/finance/transactions/{id}", txId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Weekly groceries"));

        // 5. Create recurring rule -> 201 Created
        String rulePayload = """
                {
                  "name": "Streaming Rule %d",
                  "transactionType": "EXPENSE",
                  "categoryId": %d,
                  "postingMode": "AUTO_POST",
                  "frequency": "MONTHLY",
                  "intervalCount": 1,
                  "dayOfMonth": 1,
                  "startDate": "2026-10-01",
                  "entries": [
                    {
                      "walletId": %d,
                      "amountDelta": -15.00
                    }
                  ]
                }
                """.formatted(System.currentTimeMillis(), catId, walletId);

        mockMvc.perform(post("/api/v1/finance/recurring-rules")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rulePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.frequency").value("MONTHLY"));
    }
}
