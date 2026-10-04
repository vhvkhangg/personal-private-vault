package com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringRuleWeekday;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringRuleWeekdayId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Spring Data repository for {@link RecurringRuleWeekday}.
 */
@Repository
public interface RecurringRuleWeekdayRepository extends JpaRepository<RecurringRuleWeekday, RecurringRuleWeekdayId> {

    List<RecurringRuleWeekday> findByIdRecurringRuleId(Long recurringRuleId);

    List<RecurringRuleWeekday> findByIdRecurringRuleIdIn(Collection<Long> recurringRuleIds);

    @Modifying
    @Query("DELETE FROM RecurringRuleWeekday w WHERE w.id.recurringRuleId = :recurringRuleId")
    void deleteByRecurringRuleId(@Param("recurringRuleId") Long recurringRuleId);
}
