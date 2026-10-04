package com.vhvkhangg.personalprivatevault.finance.subscription;

import com.vhvkhangg.personalprivatevault.finance.subscription.command.CreateSubscriptionCommand;
import com.vhvkhangg.personalprivatevault.finance.subscription.command.UpdateSubscriptionCommand;
import com.vhvkhangg.personalprivatevault.finance.view.SubscriptionView;

import java.util.List;

/**
 * Public capability operations for subscriptions.
 */
public interface SubscriptionOperations {

    /**
     * Creates a new subscription.
     *
     * @param command creation command
     * @return created subscription view
     */
    SubscriptionView createSubscription(CreateSubscriptionCommand command);

    /**
     * Updates an existing subscription.
     *
     * @param command update command
     * @return updated subscription view
     */
    SubscriptionView updateSubscription(UpdateSubscriptionCommand command);

    /**
     * Finds a non-deleted subscription by ID.
     *
     * @param id subscription identifier
     * @return subscription view
     */
    SubscriptionView findSubscriptionById(Long id);

    /**
     * Finds active non-deleted subscriptions ordered by next_billing_date ASC NULLS LAST, id ASC.
     *
     * @param limit positive maximum count
     * @return list of subscription views
     */
    List<SubscriptionView> findActiveSubscriptions(int limit);

    /**
     * Soft-deletes a subscription. Idempotent.
     *
     * @param id subscription identifier
     * @return subscription view
     */
    SubscriptionView softDeleteSubscription(Long id);

    /**
     * Restores a soft-deleted subscription. Idempotent.
     *
     * @param id subscription identifier
     * @return subscription view
     */
    SubscriptionView restoreSubscription(Long id);
}
