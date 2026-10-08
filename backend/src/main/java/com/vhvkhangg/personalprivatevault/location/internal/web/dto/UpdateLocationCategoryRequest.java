package com.vhvkhangg.personalprivatevault.location.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLocationCategoryRequest(
        @NotBlank(message = "Category name must not be blank")
        @Size(max = 150, message = "Category name must not exceed 150 characters")
        String name,
        String description
) {
}
