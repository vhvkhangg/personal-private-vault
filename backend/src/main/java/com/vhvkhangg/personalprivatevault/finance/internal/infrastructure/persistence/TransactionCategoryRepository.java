package com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.TransactionCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link TransactionCategory}.
 */
@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {

    List<TransactionCategory> findAllByOrderByNameAscIdAsc(Pageable pageable);

    List<TransactionCategory> findByActiveOrderByNameAscIdAsc(boolean active, Pageable pageable);

    @Query("""
        SELECT COUNT(t) > 0
        FROM FinancialTransaction t
        WHERE t.categoryId = :categoryId
          AND t.type = :type
    """)
    boolean existsFinancialTransactionByCategoryIdAndType(
            @Param("categoryId") Long categoryId,
            @Param("type") FinancialTransactionType type
    );

    @Query("""
        SELECT COUNT(r) > 0
        FROM RecurringTransactionRule r
        WHERE r.categoryId = :categoryId
          AND r.transactionType = :type
    """)
    boolean existsRecurringRuleByCategoryIdAndTransactionType(
            @Param("categoryId") Long categoryId,
            @Param("type") FinancialTransactionType type
    );
}
