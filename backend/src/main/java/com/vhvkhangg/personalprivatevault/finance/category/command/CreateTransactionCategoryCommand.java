package com.vhvkhangg.personalprivatevault.finance.category.command;

import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;

/**
 * Command for creating a transaction category.
 */
public record CreateTransactionCategoryCommand(
        String name,
        TransactionCategoryKind kind,
        Long parentCategoryId,
        Boolean active
) {
}
