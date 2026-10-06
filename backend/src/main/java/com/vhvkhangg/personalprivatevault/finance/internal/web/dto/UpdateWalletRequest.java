package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateWalletRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull WalletType type,
        @NotBlank @Size(min = 3, max = 3) String currencyCode,
        BigDecimal openingBalance,
        String notes,
        Boolean active
) {}
