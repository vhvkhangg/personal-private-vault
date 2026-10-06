package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.BillingCycle;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SubscriptionResponse(
        Long id,
        String name,
        String provider,
        BigDecimal priceAmount,
        String currencyCode,
        BillingCycle billingCycle,
        int billingInterval,
        Integer customCycleDays,
        LocalDate nextBillingDate,
        boolean autoRenew,
        Long paymentWalletId,
        Long recurringRuleId,
        String url,
        String notes,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {}
