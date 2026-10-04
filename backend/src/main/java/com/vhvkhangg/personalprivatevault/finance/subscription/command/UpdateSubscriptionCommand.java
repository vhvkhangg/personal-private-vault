package com.vhvkhangg.personalprivatevault.finance.subscription.command;

import com.vhvkhangg.personalprivatevault.finance.enums.BillingCycle;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command for updating an existing subscription.
 */
public record UpdateSubscriptionCommand(
        Long id,
        String name,
        String provider,
        BigDecimal priceAmount,
        String currencyCode,
        BillingCycle billingCycle,
        Integer billingInterval,
        Integer customCycleDays,
        LocalDate nextBillingDate,
        Boolean autoRenew,
        Long paymentWalletId,
        Long recurringRuleId,
        String url,
        String notes,
        Boolean active
) {
}
