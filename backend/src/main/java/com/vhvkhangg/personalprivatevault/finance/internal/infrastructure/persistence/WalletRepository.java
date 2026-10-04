package com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.finance.internal.domain.Wallet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link Wallet}.
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByIdAndDeletedAtIsNull(Long id);

    List<Wallet> findByDeletedAtIsNullOrderByNameAscIdAsc(Pageable pageable);

    List<Wallet> findByActiveAndDeletedAtIsNullOrderByNameAscIdAsc(boolean active, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(e.amountDelta), 0)
        FROM FinancialTransactionEntry e, FinancialTransaction t
        WHERE e.transactionId = t.id
          AND e.walletId = :walletId
          AND t.status = :status
          AND t.deletedAt IS NULL
    """)
    BigDecimal calculatePostedDeltaSum(
            @Param("walletId") Long walletId,
            @Param("status") com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus status
    );
}
