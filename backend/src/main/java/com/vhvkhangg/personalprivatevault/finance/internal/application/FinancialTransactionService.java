package com.vhvkhangg.personalprivatevault.finance.internal.application;

import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionStatus;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.FinancialTransaction;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.FinancialTransactionEntry;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.TransactionCategory;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.Wallet;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.FinancialTransactionEntryRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.FinancialTransactionRepository;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.WalletRepository;
import com.vhvkhangg.personalprivatevault.finance.transaction.FinancialTransactionOperations;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.CreateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.FinancialTransactionEntryInput;
import com.vhvkhangg.personalprivatevault.finance.transaction.command.UpdateFinancialTransactionCommand;
import com.vhvkhangg.personalprivatevault.finance.transaction.exception.FinancialTransactionNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.transaction.exception.InvalidFinancialTransactionException;
import com.vhvkhangg.personalprivatevault.finance.view.FinancialTransactionEntryView;
import com.vhvkhangg.personalprivatevault.finance.view.FinancialTransactionView;
import com.vhvkhangg.personalprivatevault.finance.wallet.exception.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application service implementing {@link FinancialTransactionOperations}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialTransactionService implements FinancialTransactionOperations {

    private final FinancialTransactionRepository financialTransactionRepository;
    private final FinancialTransactionEntryRepository financialTransactionEntryRepository;
    private final WalletRepository walletRepository;
    private final FinanceLockManager lockManager;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public FinancialTransactionView createTransaction(CreateFinancialTransactionCommand command) {
        if (command == null) {
            throw new InvalidFinancialTransactionException("Command must not be null");
        }
        validateTransactionScalars(command.type(), command.occurredAt(), command.description(), command.exchangeRate());
        validateEntriesShapeAndSign(command.type(), command.entries());

        FinancialTransactionStatus status = command.status() != null ? command.status() : FinancialTransactionStatus.POSTED;
        BigDecimal exchangeRate = FinanceValidationUtils.normalizeExchangeRate(command.exchangeRate());

        // Lock order: category -> wallets ascending
        TransactionCategory category = lockManager.lockAndRefreshCategory(command.categoryId());
        validateCategoryCompatibility(command.type(), category);

        List<Long> walletIds = command.entries().stream().map(FinancialTransactionEntryInput::walletId).toList();
        List<Wallet> wallets = lockManager.lockAndRefreshWalletsAscending(walletIds);
        validateWalletsNotDeleted(wallets);

        FinancialTransaction transaction = new FinancialTransaction(
                command.type(),
                status,
                command.categoryId(),
                null,
                command.description(),
                command.notes(),
                command.occurredAt(),
                exchangeRate
        );
        FinancialTransaction saved = financialTransactionRepository.save(transaction);

        List<FinancialTransactionEntryView> entryViews = new ArrayList<>(command.entries().size());
        for (FinancialTransactionEntryInput entryInput : command.entries()) {
            BigDecimal delta = FinanceValidationUtils.normalizeMoney(entryInput.amountDelta(), "amountDelta", InvalidFinancialTransactionException::new);
            FinancialTransactionEntry entry = new FinancialTransactionEntry(saved.getId(), entryInput.walletId(), delta);
            FinancialTransactionEntry savedEntry = financialTransactionEntryRepository.save(entry);
            entryViews.add(toEntryView(savedEntry));
        }
        financialTransactionEntryRepository.flush();
        financialTransactionRepository.flush();

        return toTransactionView(saved, entryViews);
    }

    @Override
    @Transactional
    public FinancialTransactionView updateTransaction(UpdateFinancialTransactionCommand command) {
        if (command == null) {
            throw new InvalidFinancialTransactionException("Command must not be null");
        }
        if (command.id() == null) {
            throw new InvalidFinancialTransactionException("Transaction id must not be null");
        }
        validateTransactionScalars(command.type(), command.occurredAt(), command.description(), command.exchangeRate());
        validateEntriesShapeAndSign(command.type(), command.entries());

        // Lock order: existing transaction -> category -> wallets ascending
        FinancialTransaction transaction = lockManager.lockAndRefreshTransaction(command.id());
        if (transaction.getDeletedAt() != null) {
            throw new FinancialTransactionNotFoundException(command.id());
        }

        FinancialTransactionStatus status = command.status() != null ? command.status() : FinancialTransactionStatus.POSTED;
        BigDecimal exchangeRate = FinanceValidationUtils.normalizeExchangeRate(command.exchangeRate());

        TransactionCategory category = lockManager.lockAndRefreshCategory(command.categoryId());
        validateCategoryCompatibility(command.type(), category);

        List<Long> walletIds = command.entries().stream().map(FinancialTransactionEntryInput::walletId).toList();
        List<Wallet> wallets = lockManager.lockAndRefreshWalletsAscending(walletIds);
        validateWalletsNotDeleted(wallets);

        // Replace child entries
        List<FinancialTransactionEntry> existingEntries = financialTransactionEntryRepository.findByTransactionIdOrderByIdAsc(transaction.getId());
        existingEntries.forEach(entityManager::detach);
        financialTransactionEntryRepository.deleteByTransactionId(transaction.getId());
        financialTransactionEntryRepository.flush();

        List<FinancialTransactionEntryView> entryViews = new ArrayList<>(command.entries().size());
        for (FinancialTransactionEntryInput entryInput : command.entries()) {
            BigDecimal delta = FinanceValidationUtils.normalizeMoney(entryInput.amountDelta(), "amountDelta", InvalidFinancialTransactionException::new);
            FinancialTransactionEntry entry = new FinancialTransactionEntry(transaction.getId(), entryInput.walletId(), delta);
            FinancialTransactionEntry savedEntry = financialTransactionEntryRepository.save(entry);
            entryViews.add(toEntryView(savedEntry));
        }
        financialTransactionEntryRepository.flush();

        transaction.update(
                command.type(),
                status,
                command.categoryId(),
                command.description(),
                command.notes(),
                command.occurredAt(),
                exchangeRate
        );
        financialTransactionRepository.flush();

        return toTransactionView(transaction, entryViews);
    }

    @Override
    public FinancialTransactionView findTransactionById(Long id) {
        if (id == null) {
            throw new InvalidFinancialTransactionException("Transaction id must not be null");
        }
        FinancialTransaction transaction = financialTransactionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new FinancialTransactionNotFoundException(id));
        List<FinancialTransactionEntryView> entries = financialTransactionEntryRepository
                .findByTransactionIdOrderByIdAsc(transaction.getId())
                .stream()
                .map(this::toEntryView)
                .toList();
        return toTransactionView(transaction, entries);
    }

    @Override
    public List<FinancialTransactionView> findRecentTransactionsByWallet(Long walletId, int limit) {
        if (walletId == null) {
            throw new InvalidFinancialTransactionException("Wallet id must not be null");
        }
        if (limit <= 0) {
            throw new InvalidFinancialTransactionException("Limit must be positive");
        }
        walletRepository.findByIdAndDeletedAtIsNull(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        List<FinancialTransaction> transactions = financialTransactionRepository.findRecentByWalletId(walletId, PageRequest.of(0, limit));
        return batchLoadViews(transactions);
    }

    @Override
    public List<FinancialTransactionView> findRecentTransactions(int limit) {
        if (limit <= 0) {
            throw new InvalidFinancialTransactionException("Limit must be positive");
        }
        List<FinancialTransaction> transactions = financialTransactionRepository.findByDeletedAtIsNullOrderByOccurredAtDescIdDesc(PageRequest.of(0, limit));
        return batchLoadViews(transactions);
    }

    @Override
    @Transactional
    public FinancialTransactionView softDeleteTransaction(Long id) {
        if (id == null) {
            throw new InvalidFinancialTransactionException("Transaction id must not be null");
        }
        FinancialTransaction transaction = lockManager.lockAndRefreshTransaction(id);
        transaction.softDelete();
        financialTransactionRepository.flush();
        List<FinancialTransactionEntryView> entries = financialTransactionEntryRepository
                .findByTransactionIdOrderByIdAsc(transaction.getId())
                .stream()
                .map(this::toEntryView)
                .toList();
        return toTransactionView(transaction, entries);
    }

    @Override
    @Transactional
    public FinancialTransactionView restoreTransaction(Long id) {
        if (id == null) {
            throw new InvalidFinancialTransactionException("Transaction id must not be null");
        }
        FinancialTransaction transaction = lockManager.lockAndRefreshTransaction(id);
        transaction.restore();
        financialTransactionRepository.flush();
        financialTransactionEntryRepository.findByTransactionIdOrderByIdAsc(transaction.getId()).forEach(entityManager::detach);
        List<FinancialTransactionEntryView> entries = financialTransactionEntryRepository
                .findByTransactionIdOrderByIdAsc(transaction.getId())
                .stream()
                .map(this::toEntryView)
                .toList();
        return toTransactionView(transaction, entries);
    }

    private List<FinancialTransactionView> batchLoadViews(List<FinancialTransaction> transactions) {
        if (transactions.isEmpty()) {
            return List.of();
        }
        List<Long> txIds = transactions.stream().map(FinancialTransaction::getId).toList();
        List<FinancialTransactionEntry> allEntries = financialTransactionEntryRepository.findByTransactionIdInOrderByIdAsc(txIds);
        Map<Long, List<FinancialTransactionEntryView>> entriesByTxId = allEntries.stream()
                .map(this::toEntryView)
                .collect(Collectors.groupingBy(
                        FinancialTransactionEntryView::transactionId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        return transactions.stream()
                .map(tx -> toTransactionView(tx, entriesByTxId.getOrDefault(tx.getId(), List.of())))
                .toList();
    }

    private FinancialTransactionView loadWithEntries(FinancialTransaction tx) {
        List<FinancialTransactionEntryView> entries = financialTransactionEntryRepository
                .findByTransactionIdOrderByIdAsc(tx.getId())
                .stream()
                .map(this::toEntryView)
                .toList();
        return toTransactionView(tx, entries);
    }

    private void validateTransactionScalars(
            FinancialTransactionType type,
            Object occurredAt,
            String description,
            BigDecimal exchangeRate
    ) {
        if (type == null) {
            throw new InvalidFinancialTransactionException("Transaction type must not be null");
        }
        if (occurredAt == null) {
            throw new InvalidFinancialTransactionException("occurredAt must not be null");
        }
        if (description != null && description.length() > 1000) {
            throw new InvalidFinancialTransactionException("description must not exceed 1000 characters");
        }
        FinanceValidationUtils.validateExchangeRate(exchangeRate);
    }

    private void validateEntriesShapeAndSign(
            FinancialTransactionType type,
            List<FinancialTransactionEntryInput> entries
    ) {
        if (entries == null || entries.isEmpty()) {
            throw new InvalidFinancialTransactionException("Transaction must have at least one entry");
        }
        for (FinancialTransactionEntryInput entry : entries) {
            if (entry.walletId() == null) {
                throw new InvalidFinancialTransactionException("walletId must not be null");
            }
            BigDecimal normalized = FinanceValidationUtils.normalizeMoney(entry.amountDelta(), "amountDelta", InvalidFinancialTransactionException::new);
            if (normalized.compareTo(BigDecimal.ZERO) == 0) {
                throw new InvalidFinancialTransactionException("amountDelta must not be zero");
            }
        }

        List<Long> distinctWalletIds = entries.stream()
                .map(FinancialTransactionEntryInput::walletId)
                .distinct()
                .toList();
        if (distinctWalletIds.size() != entries.size()) {
            throw new InvalidFinancialTransactionException("A wallet may only appear once in a transaction's entries");
        }

        if (type == FinancialTransactionType.INCOME) {
            if (entries.size() != 1) {
                throw new InvalidFinancialTransactionException("INCOME transaction must have exactly one entry");
            }
            if (entries.getFirst().amountDelta().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidFinancialTransactionException("INCOME entry amountDelta must be positive");
            }
        } else if (type == FinancialTransactionType.EXPENSE) {
            if (entries.size() != 1) {
                throw new InvalidFinancialTransactionException("EXPENSE transaction must have exactly one entry");
            }
            if (entries.getFirst().amountDelta().compareTo(BigDecimal.ZERO) >= 0) {
                throw new InvalidFinancialTransactionException("EXPENSE entry amountDelta must be negative");
            }
        } else if (type == FinancialTransactionType.TRANSFER) {
            if (entries.size() != 2) {
                throw new InvalidFinancialTransactionException("TRANSFER transaction must have exactly two entries");
            }
            long positiveCount = entries.stream()
                    .filter(e -> e.amountDelta().compareTo(BigDecimal.ZERO) > 0)
                    .count();
            long negativeCount = entries.stream()
                    .filter(e -> e.amountDelta().compareTo(BigDecimal.ZERO) < 0)
                    .count();
            if (positiveCount != 1 || negativeCount != 1) {
                throw new InvalidFinancialTransactionException(
                        "TRANSFER transaction must have exactly one source entry (negative delta) and one destination entry (positive delta)"
                );
            }
        }
    }

    private void validateCategoryCompatibility(FinancialTransactionType type, TransactionCategory category) {
        if (type == FinancialTransactionType.TRANSFER) {
            if (category != null) {
                throw new InvalidFinancialTransactionException("TRANSFER transactions must not have a category");
            }
            return;
        }
        if (category == null) {
            return;
        }
        if (type == FinancialTransactionType.INCOME) {
            if (category.getKind() != TransactionCategoryKind.INCOME && category.getKind() != TransactionCategoryKind.BOTH) {
                throw new InvalidFinancialTransactionException(
                        "Category kind " + category.getKind() + " is incompatible with INCOME transaction"
                );
            }
        } else if (type == FinancialTransactionType.EXPENSE) {
            if (category.getKind() != TransactionCategoryKind.EXPENSE && category.getKind() != TransactionCategoryKind.BOTH) {
                throw new InvalidFinancialTransactionException(
                        "Category kind " + category.getKind() + " is incompatible with EXPENSE transaction"
                );
            }
        }
    }

    private void validateWalletsNotDeleted(List<Wallet> wallets) {
        for (Wallet wallet : wallets) {
            if (wallet.getDeletedAt() != null) {
                throw new InvalidFinancialTransactionException("Referenced wallet is deleted: " + wallet.getId());
            }
        }
    }

    private FinancialTransactionEntryView toEntryView(FinancialTransactionEntry entry) {
        return new FinancialTransactionEntryView(
                entry.getId(),
                entry.getTransactionId(),
                entry.getWalletId(),
                entry.getAmountDelta()
        );
    }

    private FinancialTransactionView toTransactionView(FinancialTransaction tx, List<FinancialTransactionEntryView> entries) {
        return new FinancialTransactionView(
                tx.getId(),
                tx.getType(),
                tx.getStatus(),
                tx.getCategoryId(),
                tx.getGeneratedByRecurringRuleId(),
                tx.getDescription(),
                tx.getNotes(),
                tx.getOccurredAt(),
                tx.getExchangeRate(),
                entries,
                tx.getCreatedAt(),
                tx.getUpdatedAt(),
                tx.getDeletedAt()
        );
    }
}
