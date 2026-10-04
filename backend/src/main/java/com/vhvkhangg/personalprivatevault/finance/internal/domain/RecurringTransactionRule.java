package com.vhvkhangg.personalprivatevault.finance.internal.domain;

import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurrenceFrequency;
import com.vhvkhangg.personalprivatevault.finance.enums.RecurringPostingMode;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Recurring transaction rule entity mapped to {@code recurring_transaction_rules}.
 */
@Entity
@Table(name = "recurring_transaction_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecurringTransactionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "transaction_type", nullable = false)
    private FinancialTransactionType transactionType;

    @Column(name = "category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "posting_mode", nullable = false)
    private RecurringPostingMode postingMode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "frequency", nullable = false)
    private RecurrenceFrequency frequency;

    @Column(name = "interval_count", nullable = false)
    private int intervalCount;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "month_of_year")
    private Integer monthOfYear;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "posting_time")
    private LocalTime postingTime;

    @Column(name = "next_run_at")
    private Instant nextRunAt;

    @Column(name = "description", length = 1000)
    private String description;

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

    public RecurringTransactionRule(
            String name,
            FinancialTransactionType transactionType,
            Long categoryId,
            RecurringPostingMode postingMode,
            RecurrenceFrequency frequency,
            int intervalCount,
            Integer dayOfMonth,
            Integer monthOfYear,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime postingTime,
            Instant nextRunAt,
            String description,
            String notes,
            boolean active
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.transactionType = Objects.requireNonNull(transactionType, "transactionType must not be null");
        this.categoryId = categoryId;
        this.postingMode = Objects.requireNonNull(postingMode, "postingMode must not be null");
        this.frequency = Objects.requireNonNull(frequency, "frequency must not be null");
        this.intervalCount = intervalCount;
        this.dayOfMonth = dayOfMonth;
        this.monthOfYear = monthOfYear;
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        this.endDate = endDate;
        this.postingTime = postingTime;
        this.nextRunAt = nextRunAt;
        this.description = description;
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
            FinancialTransactionType transactionType,
            Long categoryId,
            RecurringPostingMode postingMode,
            RecurrenceFrequency frequency,
            int intervalCount,
            Integer dayOfMonth,
            Integer monthOfYear,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime postingTime,
            Instant nextRunAt,
            String description,
            String notes,
            boolean active
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.transactionType = Objects.requireNonNull(transactionType, "transactionType must not be null");
        this.categoryId = categoryId;
        this.postingMode = Objects.requireNonNull(postingMode, "postingMode must not be null");
        this.frequency = Objects.requireNonNull(frequency, "frequency must not be null");
        this.intervalCount = intervalCount;
        this.dayOfMonth = dayOfMonth;
        this.monthOfYear = monthOfYear;
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        this.endDate = endDate;
        this.postingTime = postingTime;
        this.nextRunAt = nextRunAt;
        this.description = description;
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
