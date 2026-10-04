package com.vhvkhangg.personalprivatevault.finance.internal.application;

import com.vhvkhangg.personalprivatevault.finance.category.TransactionCategoryOperations;
import com.vhvkhangg.personalprivatevault.finance.category.command.CreateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.category.command.UpdateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.category.exception.InvalidTransactionCategoryException;
import com.vhvkhangg.personalprivatevault.finance.category.exception.TransactionCategoryConflictException;
import com.vhvkhangg.personalprivatevault.finance.category.exception.TransactionCategoryNotFoundException;
import com.vhvkhangg.personalprivatevault.finance.enums.FinancialTransactionType;
import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;
import com.vhvkhangg.personalprivatevault.finance.internal.domain.TransactionCategory;
import com.vhvkhangg.personalprivatevault.finance.internal.infrastructure.persistence.TransactionCategoryRepository;
import com.vhvkhangg.personalprivatevault.finance.view.TransactionCategoryView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service implementing {@link TransactionCategoryOperations}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionCategoryService implements TransactionCategoryOperations {

    private final TransactionCategoryRepository transactionCategoryRepository;
    private final FinanceLockManager lockManager;

    @Override
    @Transactional
    public TransactionCategoryView createCategory(CreateTransactionCategoryCommand command) {
        if (command == null) {
            throw new InvalidTransactionCategoryException("Command must not be null");
        }
        validateCategoryFields(command.name(), command.kind());

        if (command.parentCategoryId() != null) {
            if (!transactionCategoryRepository.existsById(command.parentCategoryId())) {
                throw new InvalidTransactionCategoryException("Parent category does not exist: " + command.parentCategoryId());
            }
        }

        boolean active = command.active() == null || command.active();
        TransactionCategory category = new TransactionCategory(
                command.name().trim(),
                command.kind(),
                command.parentCategoryId(),
                active
        );
        TransactionCategory saved = transactionCategoryRepository.saveAndFlush(category);
        return toView(saved);
    }

    @Override
    @Transactional
    public TransactionCategoryView updateCategory(UpdateTransactionCategoryCommand command) {
        if (command == null) {
            throw new InvalidTransactionCategoryException("Command must not be null");
        }
        if (command.id() == null) {
            throw new InvalidTransactionCategoryException("Category id must not be null");
        }
        validateCategoryFields(command.name(), command.kind());

        if (command.id().equals(command.parentCategoryId())) {
            throw new InvalidTransactionCategoryException("A category cannot directly parent itself");
        }

        if (command.parentCategoryId() != null) {
            if (!transactionCategoryRepository.existsById(command.parentCategoryId())) {
                throw new InvalidTransactionCategoryException("Parent category does not exist: " + command.parentCategoryId());
            }
        }

        TransactionCategory category = lockManager.lockAndRefreshCategory(command.id());

        if (command.kind() != category.getKind()) {
            if (command.kind() == TransactionCategoryKind.INCOME) {
                boolean hasExpenseTx = transactionCategoryRepository.existsFinancialTransactionByCategoryIdAndType(
                        category.getId(), FinancialTransactionType.EXPENSE
                );
                boolean hasExpenseRule = transactionCategoryRepository.existsRecurringRuleByCategoryIdAndTransactionType(
                        category.getId(), FinancialTransactionType.EXPENSE
                );
                if (hasExpenseTx || hasExpenseRule) {
                    throw new TransactionCategoryConflictException(
                            "Cannot change category kind to INCOME because EXPENSE transactions or recurring rules reference it"
                    );
                }
            } else if (command.kind() == TransactionCategoryKind.EXPENSE) {
                boolean hasIncomeTx = transactionCategoryRepository.existsFinancialTransactionByCategoryIdAndType(
                        category.getId(), FinancialTransactionType.INCOME
                );
                boolean hasIncomeRule = transactionCategoryRepository.existsRecurringRuleByCategoryIdAndTransactionType(
                        category.getId(), FinancialTransactionType.INCOME
                );
                if (hasIncomeTx || hasIncomeRule) {
                    throw new TransactionCategoryConflictException(
                            "Cannot change category kind to EXPENSE because INCOME transactions or recurring rules reference it"
                    );
                }
            }
        }

        boolean active = command.active() == null || command.active();
        category.update(
                command.name().trim(),
                command.kind(),
                command.parentCategoryId(),
                active
        );
        transactionCategoryRepository.flush();
        return toView(category);
    }

    @Override
    public TransactionCategoryView findCategoryById(Long id) {
        if (id == null) {
            throw new InvalidTransactionCategoryException("Category id must not be null");
        }
        TransactionCategory category = transactionCategoryRepository.findById(id)
                .orElseThrow(() -> new TransactionCategoryNotFoundException(id));
        return toView(category);
    }

    @Override
    public List<TransactionCategoryView> findCategories(Boolean activeFilter, int limit) {
        if (limit <= 0) {
            throw new InvalidTransactionCategoryException("Limit must be positive");
        }
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<TransactionCategory> categories;
        if (activeFilter != null) {
            categories = transactionCategoryRepository.findByActiveOrderByNameAscIdAsc(activeFilter, pageRequest);
        } else {
            categories = transactionCategoryRepository.findAllByOrderByNameAscIdAsc(pageRequest);
        }
        return categories.stream().map(this::toView).toList();
    }

    private void validateCategoryFields(String name, TransactionCategoryKind kind) {
        if (name == null || name.isBlank()) {
            throw new InvalidTransactionCategoryException("Category name must not be blank");
        }
        if (name.length() > 255) {
            throw new InvalidTransactionCategoryException("Category name must not exceed 255 characters");
        }
        if (kind == null) {
            throw new InvalidTransactionCategoryException("Category kind must not be null");
        }
    }

    private TransactionCategoryView toView(TransactionCategory c) {
        return new TransactionCategoryView(
                c.getId(),
                c.getName(),
                c.getKind(),
                c.getParentCategoryId(),
                c.isActive(),
                c.getCreatedAt()
        );
    }
}
