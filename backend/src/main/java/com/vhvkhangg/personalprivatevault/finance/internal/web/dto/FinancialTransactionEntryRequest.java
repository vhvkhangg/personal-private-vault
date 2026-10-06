package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FinancialTransactionEntryRequest(
        @NotNull Long walletId,
        @NotNull BigDecimal amountDelta
) {}
