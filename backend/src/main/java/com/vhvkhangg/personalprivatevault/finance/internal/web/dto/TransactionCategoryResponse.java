package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;

import java.time.Instant;

public record TransactionCategoryResponse(
        Long id,
        String name,
        TransactionCategoryKind kind,
        Long parentCategoryId,
        boolean active,
        Instant createdAt
) {}
