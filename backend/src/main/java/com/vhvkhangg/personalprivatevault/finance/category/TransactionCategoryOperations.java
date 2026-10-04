package com.vhvkhangg.personalprivatevault.finance.category;

import com.vhvkhangg.personalprivatevault.finance.category.command.CreateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.category.command.UpdateTransactionCategoryCommand;
import com.vhvkhangg.personalprivatevault.finance.view.TransactionCategoryView;

import java.util.List;

/**
 * Public capability operations for transaction categories.
 */
public interface TransactionCategoryOperations {

    /**
     * Creates a new transaction category.
     *
     * @param command creation command
     * @return created category view
     */
    TransactionCategoryView createCategory(CreateTransactionCategoryCommand command);

    /**
     * Updates an existing transaction category.
     *
     * @param command update command
     * @return updated category view
     */
    TransactionCategoryView updateCategory(UpdateTransactionCategoryCommand command);

    /**
     * Finds a transaction category by ID.
     *
     * @param id category identifier
     * @return category view
     */
    TransactionCategoryView findCategoryById(Long id);

    /**
     * Finds transaction categories ordered by name ASC, id ASC.
     *
     * @param activeFilter optional active filter
     * @param limit positive maximum count
     * @return list of category views
     */
    List<TransactionCategoryView> findCategories(Boolean activeFilter, int limit);
}
