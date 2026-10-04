package com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.finance.internal.domain.Subscription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link Subscription}.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByRecurringRuleId(Long recurringRuleId);

    boolean existsByRecurringRuleIdAndIdNot(Long recurringRuleId, Long id);

    @Query("""
        SELECT s
        FROM Subscription s
        WHERE s.active = true
          AND s.deletedAt IS NULL
        ORDER BY s.nextBillingDate ASC NULLS LAST, s.id ASC
    """)
    List<Subscription> findActiveSubscriptions(Pageable pageable);
}
