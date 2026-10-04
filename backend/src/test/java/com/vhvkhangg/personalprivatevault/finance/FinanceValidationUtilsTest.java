package com.vhvkhangg.personalprivatevault.finance;

import com.vhvkhangg.personalprivatevault.finance.internal.application.FinanceValidationUtils;
import com.vhvkhangg.personalprivatevault.finance.transaction.exception.InvalidFinancialTransactionException;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.InvalidWalletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FinanceValidationUtilsTest {

    @Test
    @DisplayName("Normalizes valid money at maximum representable boundary numeric(19,4)")
    void validMoneyAtMaximumBoundary() {
        BigDecimal maxMoney = new BigDecimal("999999999999999.9999");
        BigDecimal normalized = FinanceValidationUtils.normalizeMoney(maxMoney);
        assertThat(normalized).isEqualByComparingTo(maxMoney);
        assertThat(normalized.scale()).isEqualTo(4);
        assertThat(normalized.precision()).isEqualTo(19);

        BigDecimal minMoney = new BigDecimal("-999999999999999.9999");
        BigDecimal normalizedMin = FinanceValidationUtils.normalizeMoney(minMoney);
        assertThat(normalizedMin).isEqualByComparingTo(minMoney);
        assertThat(normalizedMin.scale()).isEqualTo(4);
        assertThat(normalizedMin.precision()).isEqualTo(19);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1000000000000000", "1E+15", "-1000000000000000", "-1E+15"})
    @DisplayName("Rejects money amounts exceeding precision 19 after scale 4 normalization without leaking values")
    void rejectsMoneyExceedingPrecision19(String amountStr) {
        BigDecimal amount = new BigDecimal(amountStr);
        assertThatThrownBy(() -> FinanceValidationUtils.normalizeMoney(amount, "amount", InvalidWalletException::new))
                .isInstanceOf(InvalidWalletException.class)
                .hasMessage("amount exceeds maximum precision 19")
                .hasMessageNotContaining(amountStr);
    }

    @Test
    @DisplayName("Accepts equivalent zero-padded and scientific representations within precision 19")
    void acceptsEquivalentZeroPaddedAndScientificRepresentations() {
        // 1 with 20 trailing zeros
        BigDecimal padded = new BigDecimal("1.00000000000000000000");
        BigDecimal normalizedPadded = FinanceValidationUtils.normalizeMoney(padded);
        assertThat(normalizedPadded).isEqualByComparingTo(new BigDecimal("1.0000"));
        assertThat(normalizedPadded.scale()).isEqualTo(4);

        // 1E+14 fits in 15 integer digits + 4 scale digits = 19 digits
        BigDecimal sci14 = new BigDecimal("1E+14");
        BigDecimal normalizedSci = FinanceValidationUtils.normalizeMoney(sci14);
        assertThat(normalizedSci).isEqualByComparingTo(new BigDecimal("100000000000000.0000"));
        assertThat(normalizedSci.scale()).isEqualTo(4);
        assertThat(normalizedSci.precision()).isEqualTo(19);

        // Negative scale (1e3 = 1000)
        BigDecimal sci3 = new BigDecimal("1e3");
        BigDecimal normalizedSci3 = FinanceValidationUtils.normalizeMoney(sci3);
        assertThat(normalizedSci3).isEqualByComparingTo(new BigDecimal("1000.0000"));
        assertThat(normalizedSci3.scale()).isEqualTo(4);

        // Zero with multiple trailing zeros
        BigDecimal paddedZero = new BigDecimal("0.0000000000");
        BigDecimal normalizedZero = FinanceValidationUtils.normalizeMoney(paddedZero);
        assertThat(normalizedZero).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(normalizedZero.scale()).isEqualTo(4);
    }

    @Test
    @DisplayName("Rejects money amounts with excess fractional precision without leaking values")
    void rejectsExcessFractionalPrecision() {
        BigDecimal unrounded = new BigDecimal("12.34567");
        assertThatThrownBy(() -> FinanceValidationUtils.normalizeMoney(unrounded, "amountDelta", InvalidFinancialTransactionException::new))
                .isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessage("amountDelta scale must not exceed 4 without rounding")
                .hasMessageNotContaining("12.34567");
    }

    @Test
    @DisplayName("Validates exchange rate at maximum boundary numeric(24,10)")
    void validExchangeRateAtMaximumBoundary() {
        BigDecimal maxRate = new BigDecimal("99999999999999.9999999999");
        BigDecimal normalized = FinanceValidationUtils.normalizeExchangeRate(maxRate);
        assertThat(normalized).isEqualByComparingTo(maxRate);
        assertThat(normalized.scale()).isEqualTo(10);
        assertThat(normalized.precision()).isEqualTo(24);
    }

    @ParameterizedTest
    @ValueSource(strings = {"100000000000000", "1E+14"})
    @DisplayName("Rejects exchange rates exceeding precision 24 without leaking values")
    void rejectsExchangeRateExceedingPrecision24(String rateStr) {
        BigDecimal rate = new BigDecimal(rateStr);
        assertThatThrownBy(() -> FinanceValidationUtils.normalizeExchangeRate(rate))
                .isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessage("exchangeRate exceeds maximum precision 24")
                .hasMessageNotContaining(rateStr);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0000", "-1.0000"})
    @DisplayName("Rejects non-positive exchange rates without leaking values")
    void rejectsNonPositiveExchangeRate(String rateStr) {
        BigDecimal rate = new BigDecimal(rateStr);
        assertThatThrownBy(() -> FinanceValidationUtils.normalizeExchangeRate(rate))
                .isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessage("exchangeRate must be positive")
                .hasMessageNotContaining(rateStr);
    }

    @Test
    @DisplayName("Accepts equivalent zero-padded exchange rates and scientific representation")
    void acceptsEquivalentZeroPaddedExchangeRate() {
        BigDecimal padded = new BigDecimal("1.250000000000000000");
        BigDecimal normalized = FinanceValidationUtils.normalizeExchangeRate(padded);
        assertThat(normalized).isEqualByComparingTo(new BigDecimal("1.2500000000"));
        assertThat(normalized.scale()).isEqualTo(10);

        BigDecimal sci13 = new BigDecimal("1E+13");
        BigDecimal normalizedSci = FinanceValidationUtils.normalizeExchangeRate(sci13);
        assertThat(normalizedSci).isEqualByComparingTo(new BigDecimal("10000000000000.0000000000"));
        assertThat(normalizedSci.scale()).isEqualTo(10);
        assertThat(normalizedSci.precision()).isEqualTo(24);
    }

    @Test
    @DisplayName("Rejects exchange rate that cannot be represented at scale 10 without rounding")
    void rejectsExcessExchangeRateScale() {
        BigDecimal excessScale = new BigDecimal("1.12345678901");
        assertThatThrownBy(() -> FinanceValidationUtils.normalizeExchangeRate(excessScale))
                .isInstanceOf(InvalidFinancialTransactionException.class)
                .hasMessage("exchangeRate scale must not exceed 10 without rounding")
                .hasMessageNotContaining("1.12345678901");
    }
}
