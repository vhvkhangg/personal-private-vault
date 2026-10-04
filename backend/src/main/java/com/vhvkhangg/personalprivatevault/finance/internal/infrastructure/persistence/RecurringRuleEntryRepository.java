package com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringRuleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Spring Data repository for {@link RecurringRuleEntry}.
 */
@Repository
public interface RecurringRuleEntryRepository extends JpaRepository<RecurringRuleEntry, Long> {

    List<RecurringRuleEntry> findByRecurringRuleIdOrderByIdAsc(Long recurringRuleId);

    List<RecurringRuleEntry> findByRecurringRuleIdInOrderByIdAsc(Collection<Long> recurringRuleIds);

    boolean existsByWalletId(Long walletId);

    @Modifying
    @Query("DELETE FROM RecurringRuleEntry e WHERE e.recurringRuleId = :recurringRuleId")
    void deleteByRecurringRuleId(@Param("recurringRuleId") Long recurringRuleId);
}
