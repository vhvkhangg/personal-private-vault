package com.vhvkhangg.personalprivatevault.finance.internal.domain;

import com.vhvkhangg.personalprivatevault.finance.enums.WalletType;
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
 * Wallet entity mapped to {@code wallets}.
 */
@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false)
    private WalletType type;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    @Column(name = "opening_balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal openingBalance;

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

    public Wallet(
            String name,
            WalletType type,
            String currencyCode,
            BigDecimal openingBalance,
            String notes,
            boolean active
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        this.openingBalance = Objects.requireNonNull(openingBalance, "openingBalance must not be null");
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
            WalletType type,
            String currencyCode,
            BigDecimal openingBalance,
            String notes,
            boolean active
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        this.openingBalance = Objects.requireNonNull(openingBalance, "openingBalance must not be null");
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
