package com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.finance.internal.domain.FinancialTransactionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Spring Data repository for {@link FinancialTransactionEntry}.
 */
@Repository
public interface FinancialTransactionEntryRepository extends JpaRepository<FinancialTransactionEntry, Long> {

    List<FinancialTransactionEntry> findByTransactionIdOrderByIdAsc(Long transactionId);

    List<FinancialTransactionEntry> findByTransactionIdInOrderByIdAsc(Collection<Long> transactionIds);

    boolean existsByWalletId(Long walletId);

    @Modifying
    @Query("DELETE FROM FinancialTransactionEntry e WHERE e.transactionId = :transactionId")
    void deleteByTransactionId(@Param("transactionId") Long transactionId);
}
