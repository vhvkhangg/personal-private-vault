package com.vhvkhangg.personalprivatevault.finance.category.command;

import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;

/**
 * Command for updating an existing transaction category.
 */
public record UpdateTransactionCategoryCommand(
        Long id,
        String name,
        TransactionCategoryKind kind,
        Long parentCategoryId,
        Boolean active
) {
}
