package com.vhvkhangg.personalprivatevault.finance.internal.application;

import com.vhvkhangg.personalprivatevault.finance.transaction.exception.InvalidFinancialTransactionException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Common validation and normalization utilities for finance domain.
 */
public final class FinanceValidationUtils {

    private FinanceValidationUtils() {
    }

    /**
     * Validates that an amount is non-null, exactly representable at scale 4 without rounding,
     * and fits within precision 19.
     *
     * @param amount the amount to validate
     * @param fieldName name of the field for error reporting
     */
    public static void validateMoneyAmount(BigDecimal amount, String fieldName) {
        validateMoneyAmount(amount, fieldName, InvalidFinancialTransactionException::new);
    }

    /**
     * Validates that an amount is non-null, exactly representable at scale 4 without rounding,
     * and fits within precision 19, throwing an exception produced by the supplied factory on error.
     *
     * @param amount the amount to validate
     * @param fieldName name of the field for error reporting
     * @param exceptionSupplier factory for constructing the domain-specific exception
     */
    public static void validateMoneyAmount(
            BigDecimal amount,
            String fieldName,
            java.util.function.Function<String, ? extends RuntimeException> exceptionSupplier
    ) {
        normalizeMoney(amount, fieldName, exceptionSupplier);
    }

    /**
     * Normalizes a validated money amount to exactly scale 4.
     *
     * @param amount the money amount
     * @return normalized amount at scale 4
     */
    public static BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return normalizeMoney(amount, "amount", InvalidFinancialTransactionException::new);
    }

    /**
     * Normalizes a validated money amount to exactly scale 4, validating range and representation.
     *
     * @param amount the money amount
     * @param fieldName name of the field for error reporting
     * @param exceptionSupplier factory for constructing the domain-specific exception
     * @return normalized amount at scale 4
     */
    public static BigDecimal normalizeMoney(
            BigDecimal amount,
            String fieldName,
            java.util.function.Function<String, ? extends RuntimeException> exceptionSupplier
    ) {
        if (amount == null) {
            throw exceptionSupplier.apply(fieldName + " must not be null");
        }
        BigDecimal normalized;
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            normalized = BigDecimal.ZERO.setScale(4, RoundingMode.UNNECESSARY);
        } else {
            try {
                if (amount.scale() > 4) {
                    BigDecimal stripped = amount.stripTrailingZeros();
                    if (stripped.scale() > 4) {
                        throw exceptionSupplier.apply(
                                fieldName + " scale must not exceed 4 without rounding"
                        );
                    }
                    normalized = stripped.setScale(4, RoundingMode.UNNECESSARY);
                } else {
                    normalized = amount.setScale(4, RoundingMode.UNNECESSARY);
                }
            } catch (ArithmeticException ex) {
                throw exceptionSupplier.apply(
                        fieldName + " scale must not exceed 4 without rounding"
                );
            }
        }
        if (normalized.precision() > 19) {
            throw exceptionSupplier.apply(fieldName + " exceeds maximum precision 19");
        }
        return normalized;
    }

    /**
     * Validates an optional exchange rate: must be positive, scale <= 10 without rounding,
     * and precision <= 24.
     *
     * @param rate the exchange rate
     */
    public static void validateExchangeRate(BigDecimal rate) {
        normalizeExchangeRate(rate);
    }

    /**
     * Normalizes a validated exchange rate to scale 10.
     *
     * @param rate the exchange rate
     * @return normalized rate at scale 10
     */
    public static BigDecimal normalizeExchangeRate(BigDecimal rate) {
        if (rate == null) {
            return null;
        }
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFinancialTransactionException("exchangeRate must be positive");
        }
        BigDecimal normalized;
        try {
            if (rate.scale() > 10) {
                BigDecimal stripped = rate.stripTrailingZeros();
                if (stripped.scale() > 10) {
                    throw new InvalidFinancialTransactionException(
                            "exchangeRate scale must not exceed 10 without rounding"
                    );
                }
                normalized = stripped.setScale(10, RoundingMode.UNNECESSARY);
            } else {
                normalized = rate.setScale(10, RoundingMode.UNNECESSARY);
            }
        } catch (ArithmeticException ex) {
            throw new InvalidFinancialTransactionException(
                    "exchangeRate scale must not exceed 10 without rounding"
            );
        }
        if (normalized.precision() > 24) {
            throw new InvalidFinancialTransactionException("exchangeRate exceeds maximum precision 24");
        }
        return normalized;
    }
}
