package com.vhvkhangg.personalprivatevault.finance.internal.domain;

import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
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
import java.util.Objects;

/**
 * Financial transaction entity mapped to {@code financial_transactions}.
 */
@Entity
@Table(name = "financial_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinancialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false)
    private FinancialTransactionType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private FinancialTransactionStatus status;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "generated_by_recurring_rule_id")
    private Long generatedByRecurringRuleId;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "exchange_rate", precision = 24, scale = 10)
    private BigDecimal exchangeRate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public FinancialTransaction(
            FinancialTransactionType type,
            FinancialTransactionStatus status,
            Long categoryId,
            Long generatedByRecurringRuleId,
            String description,
            String notes,
            Instant occurredAt,
            BigDecimal exchangeRate
    ) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.categoryId = categoryId;
        this.generatedByRecurringRuleId = generatedByRecurringRuleId;
        this.description = description;
        this.notes = notes;
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        this.exchangeRate = exchangeRate;
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
            FinancialTransactionType type,
            FinancialTransactionStatus status,
            Long categoryId,
            String description,
            String notes,
            Instant occurredAt,
            BigDecimal exchangeRate
    ) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.categoryId = categoryId;
        this.description = description;
        this.notes = notes;
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        this.exchangeRate = exchangeRate;
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
