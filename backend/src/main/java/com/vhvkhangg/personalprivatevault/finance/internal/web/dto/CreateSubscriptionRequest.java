package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.BillingCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSubscriptionRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String provider,
        @NotNull @PositiveOrZero BigDecimal priceAmount,
        @NotBlank @Size(min = 3, max = 3) String currencyCode,
        @NotNull BillingCycle billingCycle,
        @Positive Integer billingInterval,
        @Positive Integer customCycleDays,
        LocalDate nextBillingDate,
        Boolean autoRenew,
        Long paymentWalletId,
        Long recurringRuleId,
        @Size(max = 2048) String url,
        String notes,
        Boolean active
) {}
