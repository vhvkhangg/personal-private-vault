package com.vhvkhangg.personalprivatevault.finance.internal.application;

import com.vhvkhangg.personalprivatevault.finance.enums.BillingCycle;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.RecurringTransactionRule;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.Subscription;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.Wallet;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.RecurringTransactionRuleRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.SubscriptionRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.WalletRepository;
import com.vhvkhangg.personalprivatevault.finance.recurring.exception.RecurringTransactionRuleNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.subscription.SubscriptionOperations;
import com.vhvkhangg.personalprivatevault.finance.subscription.command.CreateSubscriptionCommand;
import com.vhvkhangg.personalprivatevault.finance.subscription.command.UpdateSubscriptionCommand;
import com.vhvkhangg.personalprivatevault.finance.subscription.exception.InvalidSubscriptionException;
import com.vhvkhangg.personalprivatevault.finance.subscription.exception.SubscriptionConflictException;
import com.vhvkhangg.personalprivatevault.finance.subscription.exception.SubscriptionNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.view.SubscriptionView;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.WalletNotFoundException;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Application service implementing {@link SubscriptionOperations}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService implements SubscriptionOperations {

    private final SubscriptionRepository subscriptionRepository;
    private final WalletRepository walletRepository;
    private final RecurringTransactionRuleRepository recurringTransactionRuleRepository;
    private final ReferenceCatalog referenceCatalog;
    private final FinanceLockManager lockManager;

    @Override
    @Transactional
    public SubscriptionView createSubscription(CreateSubscriptionCommand command) {
        if (command == null) {
            throw new InvalidSubscriptionException("Command must not be null");
        }
        int interval = command.billingInterval() != null ? command.billingInterval() : 1;
        validateSubscriptionFields(
                command.name(),
                command.provider(),
                command.priceAmount(),
                command.currencyCode(),
                command.billingCycle(),
                interval,
                command.customCycleDays(),
                command.url()
        );

        // Guard referenced owners in canonical order: RecurringRule -> Wallet
        if (command.recurringRuleId() != null) {
            try {
                RecurringTransactionRule rule = lockManager.lockAndRefreshRecurringRule(command.recurringRuleId());
                if (rule.getDeletedAt() != null) {
                    throw new InvalidSubscriptionException("Recurring rule is deleted: " + command.recurringRuleId());
                }
            } catch (RecurringTransactionRuleNotFoundException ex) {
                throw new InvalidSubscriptionException("Recurring rule does not exist: " + command.recurringRuleId(), ex);
            }
        }
        if (command.paymentWalletId() != null) {
            try {
                Wallet wallet = lockManager.lockAndRefreshWallet(command.paymentWalletId());
                if (wallet.getDeletedAt() != null) {
                    throw new InvalidSubscriptionException("Payment wallet is deleted: " + command.paymentWalletId());
                }
            } catch (WalletNotFoundException ex) {
                throw new InvalidSubscriptionException("Payment wallet does not exist: " + command.paymentWalletId(), ex);
            }
        }

        if (command.recurringRuleId() != null && subscriptionRepository.existsByRecurringRuleId(command.recurringRuleId())) {
            throw new SubscriptionConflictException("Recurring rule is already linked to another subscription");
        }

        BigDecimal price = FinanceValidationUtils.normalizeMoney(command.priceAmount(), "priceAmount", InvalidSubscriptionException::new);
        boolean autoRenew = command.autoRenew() == null || command.autoRenew();
        boolean active = command.active() == null || command.active();

        Subscription subscription = new Subscription(
                command.name().trim(),
                command.provider(),
                price,
                command.currencyCode(),
                command.billingCycle(),
                interval,
                command.customCycleDays(),
                command.nextBillingDate(),
                autoRenew,
                command.paymentWalletId(),
                command.recurringRuleId(),
                command.url(),
                command.notes(),
                active
        );

        try {
            Subscription saved = subscriptionRepository.saveAndFlush(subscription);
            return toView(saved);
        } catch (DataIntegrityViolationException ex) {
            if (isRecurringRuleUniqueViolation(ex)) {
                throw new SubscriptionConflictException("Recurring rule is already linked to another subscription");
            }
            throw ex;
        }
    }

    @Override
    @Transactional
    public SubscriptionView updateSubscription(UpdateSubscriptionCommand command) {
        if (command == null) {
            throw new InvalidSubscriptionException("Command must not be null");
        }
        if (command.id() == null) {
            throw new InvalidSubscriptionException("Subscription id must not be null");
        }
        int interval = command.billingInterval() != null ? command.billingInterval() : 1;
        validateSubscriptionFields(
                command.name(),
                command.provider(),
                command.priceAmount(),
                command.currencyCode(),
                command.billingCycle(),
                interval,
                command.customCycleDays(),
                command.url()
        );

        // Lock target subscription first
        Subscription subscription = lockManager.lockAndRefreshSubscription(command.id());
        if (subscription.getDeletedAt() != null) {
            throw new SubscriptionNotFoundException(command.id());
        }

        boolean ruleChanged = !Objects.equals(subscription.getRecurringRuleId(), command.recurringRuleId());
        boolean walletChanged = !Objects.equals(subscription.getPaymentWalletId(), command.paymentWalletId());

        // Guard new/changed links in canonical order: RecurringRule -> Wallet
        if (ruleChanged && command.recurringRuleId() != null) {
            try {
                RecurringTransactionRule rule = lockManager.lockAndRefreshRecurringRule(command.recurringRuleId());
                if (rule.getDeletedAt() != null) {
                    throw new InvalidSubscriptionException("Recurring rule is deleted: " + command.recurringRuleId());
                }
            } catch (RecurringTransactionRuleNotFoundException ex) {
                throw new InvalidSubscriptionException("Recurring rule does not exist: " + command.recurringRuleId(), ex);
            }
        } else if (!ruleChanged && command.recurringRuleId() != null) {
            if (recurringTransactionRuleRepository.findById(command.recurringRuleId()).isEmpty()) {
                throw new InvalidSubscriptionException("Recurring rule does not exist: " + command.recurringRuleId());
            }
        }

        if (walletChanged && command.paymentWalletId() != null) {
            try {
                Wallet wallet = lockManager.lockAndRefreshWallet(command.paymentWalletId());
                if (wallet.getDeletedAt() != null) {
                    throw new InvalidSubscriptionException("Payment wallet is deleted: " + command.paymentWalletId());
                }
            } catch (WalletNotFoundException ex) {
                throw new InvalidSubscriptionException("Payment wallet does not exist: " + command.paymentWalletId(), ex);
            }
        } else if (!walletChanged && command.paymentWalletId() != null) {
            if (walletRepository.findById(command.paymentWalletId()).isEmpty()) {
                throw new InvalidSubscriptionException("Payment wallet does not exist: " + command.paymentWalletId());
            }
        }

        if (command.recurringRuleId() != null
                && subscriptionRepository.existsByRecurringRuleIdAndIdNot(command.recurringRuleId(), subscription.getId())) {
            throw new SubscriptionConflictException("Recurring rule is already linked to another subscription");
        }

        BigDecimal price = FinanceValidationUtils.normalizeMoney(command.priceAmount(), "priceAmount", InvalidSubscriptionException::new);
        boolean autoRenew = command.autoRenew() == null || command.autoRenew();
        boolean active = command.active() == null || command.active();

        subscription.update(
                command.name().trim(),
                command.provider(),
                price,
                command.currencyCode(),
                command.billingCycle(),
                interval,
                command.customCycleDays(),
                command.nextBillingDate(),
                autoRenew,
                command.paymentWalletId(),
                command.recurringRuleId(),
                command.url(),
                command.notes(),
                active
        );

        try {
            subscriptionRepository.flush();
            return toView(subscription);
        } catch (DataIntegrityViolationException ex) {
            if (isRecurringRuleUniqueViolation(ex)) {
                throw new SubscriptionConflictException("Recurring rule is already linked to another subscription");
            }
            throw ex;
        }
    }

    @Override
    public SubscriptionView findSubscriptionById(Long id) {
        if (id == null) {
            throw new InvalidSubscriptionException("Subscription id must not be null");
        }
        Subscription subscription = subscriptionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));
        return toView(subscription);
    }

    @Override
    public List<SubscriptionView> findActiveSubscriptions(int limit) {
        if (limit <= 0) {
            throw new InvalidSubscriptionException("Limit must be positive");
        }
        return subscriptionRepository.findActiveSubscriptions(PageRequest.of(0, limit))
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionView softDeleteSubscription(Long id) {
        if (id == null) {
            throw new InvalidSubscriptionException("Subscription id must not be null");
        }
        Subscription subscription = lockManager.lockAndRefreshSubscription(id);
        subscription.softDelete();
        subscriptionRepository.flush();
        return toView(subscription);
    }

    @Override
    @Transactional
    public SubscriptionView restoreSubscription(Long id) {
        if (id == null) {
            throw new InvalidSubscriptionException("Subscription id must not be null");
        }
        Subscription subscription = lockManager.lockAndRefreshSubscription(id);
        subscription.restore();
        subscriptionRepository.flush();
        return toView(subscription);
    }

    private void validateSubscriptionFields(
            String name,
            String provider,
            BigDecimal priceAmount,
            String currencyCode,
            BillingCycle billingCycle,
            int billingInterval,
            Integer customCycleDays,
            String url
    ) {
        if (name == null || name.isBlank()) {
            throw new InvalidSubscriptionException("Subscription name must not be blank");
        }
        if (name.length() > 500) {
            throw new InvalidSubscriptionException("Subscription name must not exceed 500 characters");
        }
        if (provider != null && provider.length() > 500) {
            throw new InvalidSubscriptionException("Provider must not exceed 500 characters");
        }
        BigDecimal normalizedPrice = FinanceValidationUtils.normalizeMoney(priceAmount, "priceAmount", InvalidSubscriptionException::new);
        if (normalizedPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidSubscriptionException("priceAmount must be nonnegative");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new InvalidSubscriptionException("currencyCode must not be blank");
        }
        if (referenceCatalog.currency(currencyCode).isEmpty()) {
            throw new InvalidSubscriptionException("Unknown currency code: " + currencyCode);
        }
        if (billingCycle == null) {
            throw new InvalidSubscriptionException("billingCycle must not be null");
        }
        if (billingInterval <= 0) {
            throw new InvalidSubscriptionException("billingInterval must be positive");
        }
        if (billingCycle == BillingCycle.CUSTOM) {
            if (customCycleDays == null || customCycleDays <= 0) {
                throw new InvalidSubscriptionException("customCycleDays must be positive for CUSTOM billing cycle");
            }
        } else {
            if (customCycleDays != null) {
                throw new InvalidSubscriptionException("customCycleDays must be null for non-CUSTOM billing cycle");
            }
        }
        if (url != null && url.length() > 2048) {
            throw new InvalidSubscriptionException("url must not exceed 2048 characters");
        }
    }

    private boolean isRecurringRuleUniqueViolation(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException cve && cve.getConstraintName() != null) {
                String cName = cve.getConstraintName().toLowerCase(Locale.ROOT);
                if (cName.contains("subscriptions_recurring_rule_id_key")
                        || (cName.contains("recurring_rule_id") && !cName.startsWith("fk_"))) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        Throwable root = ex.getRootCause();
        String rootMsg = root != null && root.getMessage() != null ? root.getMessage().toLowerCase(Locale.ROOT) : "";
        if (rootMsg.contains("subscriptions_recurring_rule_id_key")
                || (rootMsg.contains("recurring_rule_id") && (rootMsg.contains("unique") || rootMsg.contains("duplicate key")))) {
            return true;
        }
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase(Locale.ROOT) : "";
        return msg.contains("subscriptions_recurring_rule_id_key")
                || (msg.contains("recurring_rule_id") && (msg.contains("unique") || msg.contains("duplicate key")));
    }

    private SubscriptionView toView(Subscription s) {
        return new SubscriptionView(
                s.getId(),
                s.getName(),
                s.getProvider(),
                s.getPriceAmount(),
                s.getCurrencyCode(),
                s.getBillingCycle(),
                s.getBillingInterval(),
                s.getCustomCycleDays(),
                s.getNextBillingDate(),
                s.isAutoRenew(),
                s.getPaymentWalletId(),
                s.getRecurringRuleId(),
                s.getUrl(),
                s.getNotes(),
                s.isActive(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                s.getDeletedAt()
        );
    }
}
