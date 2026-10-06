package com.vhvkhangg.personalprivatevault.vault.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank(message = "Tag name must not be blank")
        @Size(max = 50, message = "Tag name must not exceed 50 characters")
        String name
) {
}
