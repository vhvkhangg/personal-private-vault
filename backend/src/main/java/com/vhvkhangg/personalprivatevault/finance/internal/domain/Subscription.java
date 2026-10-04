package com.vhvkhangg.personalprivatevault.finance.internal.domain;

import com.vhvkhangg.personalprivatevault.finance.enums.BillingCycle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Subscription entity mapped to {@code subscriptions}.
 */
@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "provider", length = 500)
    private String provider;

    @Column(name = "price_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal priceAmount;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle;

    @Column(name = "billing_interval", nullable = false)
    private int billingInterval;

    @Column(name = "custom_cycle_days")
    private Integer customCycleDays;

    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew;

    @Column(name = "payment_wallet_id")
    private Long paymentWalletId;

    @Column(name = "recurring_rule_id", unique = true)
    private Long recurringRuleId;

    @Column(name = "url", length = 2048)
    private String url;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Subscription(
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
            boolean active
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.provider = provider;
        this.priceAmount = Objects.requireNonNull(priceAmount, "priceAmount must not be null");
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        this.billingCycle = Objects.requireNonNull(billingCycle, "billingCycle must not be null");
        this.billingInterval = billingInterval;
        this.customCycleDays = customCycleDays;
        this.nextBillingDate = nextBillingDate;
        this.autoRenew = autoRenew;
        this.paymentWalletId = paymentWalletId;
        this.recurringRuleId = recurringRuleId;
        this.url = url;
        this.notes = notes;
        this.active = active;
    }

    @PrePersist
    protected void onPrePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        updatedAt = Instant.now();
    }

    public void update(
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
            boolean active
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.provider = provider;
        this.priceAmount = Objects.requireNonNull(priceAmount, "priceAmount must not be null");
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        this.billingCycle = Objects.requireNonNull(billingCycle, "billingCycle must not be null");
        this.billingInterval = billingInterval;
        this.customCycleDays = customCycleDays;
        this.nextBillingDate = nextBillingDate;
        this.autoRenew = autoRenew;
        this.paymentWalletId = paymentWalletId;
        this.recurringRuleId = recurringRuleId;
        this.url = url;
        this.notes = notes;
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        if (this.deletedAt == null) {
            this.deletedAt = Instant.now();
            this.updatedAt = Instant.now();
        }
    }

    public void restore() {
        if (this.deletedAt != null) {
            this.deletedAt = null;
            this.updatedAt = Instant.now();
        }
    }
}
