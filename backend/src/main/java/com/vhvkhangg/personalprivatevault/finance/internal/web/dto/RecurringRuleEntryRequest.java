package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecurringRuleEntryRequest(
        @Schema(description = "Target wallet ID. Must be distinct across all entries in the recurring rule.")
        @NotNull Long walletId,
        @Schema(description = "Amount delta for the wallet. Positive for income or transfer destination; negative for expense or transfer source.")
        @NotNull BigDecimal amountDelta
) {}
