package com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringTransactionRule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link RecurringTransactionRule}.
 */
@Repository
public interface RecurringTransactionRuleRepository extends JpaRepository<RecurringTransactionRule, Long> {

    Optional<RecurringTransactionRule> findByIdAndDeletedAtIsNull(Long id);

    List<RecurringTransactionRule> findByDeletedAtIsNullOrderByNameAscIdAsc(Pageable pageable);

    List<RecurringTransactionRule> findByActiveAndDeletedAtIsNullOrderByNameAscIdAsc(boolean active, Pageable pageable);

    @Query("""
        SELECT r
        FROM RecurringTransactionRule r
        WHERE r.active = true
          AND r.deletedAt IS NULL
          AND r.nextRunAt IS NOT NULL
          AND r.nextRunAt <= :cutoff
        ORDER BY r.nextRunAt ASC, r.id ASC
    """)
    List<RecurringTransactionRule> findDueRules(@Param("cutoff") Instant cutoff, Pageable pageable);
}
