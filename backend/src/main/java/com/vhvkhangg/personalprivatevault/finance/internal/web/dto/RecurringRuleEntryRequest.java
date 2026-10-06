package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecurringRuleEntryRequest(
        @NotNull Long walletId,
        @NotNull BigDecimal amountDelta
) {}
