package com.vhvkhangg.personalprivatevault.finance.view;

import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;

import java.time.Instant;

/**
 * Read model representing an immutable transaction category.
 */
public record TransactionCategoryView(
        Long id,
        String name,
        TransactionCategoryKind kind,
        Long parentCategoryId,
        boolean active,
        Instant createdAt
) {
}
