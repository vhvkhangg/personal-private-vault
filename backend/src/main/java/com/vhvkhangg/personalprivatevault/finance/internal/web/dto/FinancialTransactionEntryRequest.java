package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FinancialTransactionEntryRequest(
        @Schema(description = "Target wallet ID", example = "1")
        @NotNull Long walletId,

        @Schema(
                description = "Monetary delta in wallet currency. Negative for expenses/outflows, positive for income/inflows. In TRANSFER transactions, requires one negative and one positive entry across distinct wallets.",
                example = "-25.50"
        )
        @NotNull BigDecimal amountDelta
) {}
