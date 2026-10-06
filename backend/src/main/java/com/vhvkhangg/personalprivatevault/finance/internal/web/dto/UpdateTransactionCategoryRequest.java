package com.vhvkhangg.personalprivatevault.finance.internal.web.dto;

import com.vhvkhangg.personalprivatevault.finance.enums.TransactionCategoryKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTransactionCategoryRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull TransactionCategoryKind kind,
        Long parentCategoryId,
        Boolean active
) {}
