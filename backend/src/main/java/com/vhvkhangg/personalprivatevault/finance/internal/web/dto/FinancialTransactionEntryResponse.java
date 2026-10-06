package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import java.math.BigDecimal;

public record FinancialTransactionEntryResponse(
        Long id,
        Long transactionId,
        Long walletId,
        BigDecimal amountDelta
) {}
