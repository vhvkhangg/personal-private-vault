package com.vhvkhangg.personalprivatevault.finance.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Financial transaction entry entity mapped to {@code financial_transaction_entries}.
 */
@Entity
@Table(name = "financial_transaction_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinancialTransactionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "amount_delta", precision = 19, scale = 4, nullable = false)
    private BigDecimal amountDelta;

    public FinancialTransactionEntry(Long transactionId, Long walletId, BigDecimal amountDelta) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId must not be null");
        this.walletId = Objects.requireNonNull(walletId, "walletId must not be null");
        this.amountDelta = Objects.requireNonNull(amountDelta, "amountDelta must not be null");
    }
}
