package com.vhvkhangg.personalprivatevault.finance.internal.application;

import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.Wallet;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.FinancialTransactionEntryRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.RecurringRuleEntryRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.WalletRepository;
import com.vhvkhangg.personalprivatevault.finance.view.WalletView;
import com.vhvkhangg.personalprivatevault.finance.wallet.WalletOperations;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.CreateWalletCommand;
import com.vhvkhangg.personalprivatevault.finance.wallet.command.UpdateWalletCommand;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.InvalidWalletException;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.WalletConflictException;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.WalletNotFoundException;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application service implementing {@link WalletOperations}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService implements WalletOperations {

    private final WalletRepository walletRepository;
    private final FinancialTransactionEntryRepository financialTransactionEntryRepository;
    private final RecurringRuleEntryRepository recurringRuleEntryRepository;
    private final ReferenceCatalog referenceCatalog;
    private final FinanceLockManager lockManager;

    @Override
    @Transactional
    public WalletView createWallet(CreateWalletCommand command) {
        if (command == null) {
            throw new InvalidWalletException("Command must not be null");
        }
        validateWalletFields(command.name(), command.type(), command.currencyCode());

        BigDecimal openingBalance = resolveOpeningBalance(command.openingBalance());
        boolean active = command.active() == null || command.active();

        Wallet wallet = new Wallet(
                command.name().trim(),
                command.type(),
                command.currencyCode(),
                openingBalance,
                command.notes(),
                active
        );
        Wallet saved = walletRepository.saveAndFlush(wallet);
        return toView(saved);
    }

    @Override
    @Transactional
    public WalletView updateWallet(UpdateWalletCommand command) {
        if (command == null) {
            throw new InvalidWalletException("Command must not be null");
        }
        if (command.id() == null) {
            throw new InvalidWalletException("Wallet id must not be null");
        }
        validateWalletFields(command.name(), command.type(), command.currencyCode());

        Wallet wallet = lockManager.lockAndRefreshWallet(command.id());
        if (wallet.getDeletedAt() != null) {
            throw new WalletNotFoundException(command.id());
        }

        if (!wallet.getCurrencyCode().equals(command.currencyCode())) {
            boolean hasLedgerReferences = financialTransactionEntryRepository.existsByWalletId(wallet.getId());
            boolean hasRecurringReferences = recurringRuleEntryRepository.existsByWalletId(wallet.getId());
            if (hasLedgerReferences || hasRecurringReferences) {
                throw new WalletConflictException(
                        "Cannot change currency of wallet while transaction or recurring rule entries reference it"
                );
            }
        }

        BigDecimal openingBalance = resolveOpeningBalance(command.openingBalance());
        boolean active = command.active() == null || command.active();

        wallet.update(
                command.name().trim(),
                command.type(),
                command.currencyCode(),
                openingBalance,
                command.notes(),
                active
        );
        walletRepository.flush();
        return toView(wallet);
    }

    @Override
    public WalletView findWalletById(Long id) {
        if (id == null) {
            throw new InvalidWalletException("Wallet id must not be null");
        }
        Wallet wallet = walletRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
        return toView(wallet);
    }

    @Override
    public List<WalletView> findWallets(Boolean activeFilter, int limit) {
        if (limit <= 0) {
            throw new InvalidWalletException("Limit must be positive");
        }
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Wallet> wallets;
        if (activeFilter != null) {
            wallets = walletRepository.findByActiveAndDeletedAtIsNullOrderByNameAscIdAsc(activeFilter, pageRequest);
        } else {
            wallets = walletRepository.findByDeletedAtIsNullOrderByNameAscIdAsc(pageRequest);
        }
        return wallets.stream().map(this::toView).toList();
    }

    @Override
    public BigDecimal currentBalance(Long walletId) {
        if (walletId == null) {
            throw new InvalidWalletException("Wallet id must not be null");
        }
        Wallet wallet = walletRepository.findByIdAndDeletedAtIsNull(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        BigDecimal postedDeltaSum = walletRepository.calculatePostedDeltaSum(walletId, FinancialTransactionStatus.POSTED);
        return wallet.getOpeningBalance().add(postedDeltaSum).setScale(4);
    }

    @Override
    @Transactional
    public WalletView softDeleteWallet(Long id) {
        if (id == null) {
            throw new InvalidWalletException("Wallet id must not be null");
        }
        Wallet wallet = lockManager.lockAndRefreshWallet(id);
        wallet.softDelete();
        walletRepository.flush();
        return toView(wallet);
    }

    @Override
    @Transactional
    public WalletView restoreWallet(Long id) {
        if (id == null) {
            throw new InvalidWalletException("Wallet id must not be null");
        }
        Wallet wallet = lockManager.lockAndRefreshWallet(id);
        wallet.restore();
        walletRepository.flush();
        return toView(wallet);
    }

    private void validateWalletFields(String name, Object type, String currencyCode) {
        if (name == null || name.isBlank()) {
            throw new InvalidWalletException("Wallet name must not be blank");
        }
        if (name.length() > 255) {
            throw new InvalidWalletException("Wallet name must not exceed 255 characters");
        }
        if (type == null) {
            throw new InvalidWalletException("Wallet type must not be null");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new InvalidWalletException("Currency code must not be blank");
        }
        if (referenceCatalog.currency(currencyCode).isEmpty()) {
            throw new InvalidWalletException("Unknown currency code: " + currencyCode);
        }
    }

    private BigDecimal resolveOpeningBalance(BigDecimal balance) {
        if (balance == null) {
            return BigDecimal.ZERO.setScale(4);
        }
        return FinanceValidationUtils.normalizeMoney(balance, "openingBalance", InvalidWalletException::new);
    }

    private WalletView toView(Wallet w) {
        return new WalletView(
                w.getId(),
                w.getName(),
                w.getType(),
                w.getCurrencyCode(),
                w.getOpeningBalance(),
                w.getNotes(),
                w.isActive(),
                w.getCreatedAt(),
                w.getUpdatedAt(),
                w.getDeletedAt()
        );
    }
}
