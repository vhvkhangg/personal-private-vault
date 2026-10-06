package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import java.math.BigDecimal;

public record RecurringRuleEntryResponse(
        Long id,
        Long recurringRuleId,
        Long walletId,
        BigDecimal amountDelta
) {}
