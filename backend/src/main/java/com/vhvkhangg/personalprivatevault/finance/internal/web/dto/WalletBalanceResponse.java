package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import java.math.BigDecimal;

public record WalletBalanceResponse(
        Long walletId,
        BigDecimal balance
) {}
