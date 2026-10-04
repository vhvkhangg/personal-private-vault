package com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.finance.internal.domain.FinancialTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link FinancialTransaction}.
 */
@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    Optional<FinancialTransaction> findByIdAndDeletedAtIsNull(Long id);

    List<FinancialTransaction> findByDeletedAtIsNullOrderByOccurredAtDescIdDesc(Pageable pageable);

    @Query("""
        SELECT DISTINCT t
        FROM FinancialTransaction t, FinancialTransactionEntry e
        WHERE t.id = e.transactionId
          AND e.walletId = :walletId
          AND t.deletedAt IS NULL
        ORDER BY t.occurredAt DESC, t.id DESC
    """)
    List<FinancialTransaction> findRecentByWalletId(@Param("walletId") Long walletId, Pageable pageable);
}
