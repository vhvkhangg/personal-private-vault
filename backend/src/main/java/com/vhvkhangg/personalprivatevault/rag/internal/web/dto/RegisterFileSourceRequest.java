package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterFileSourceRequest(
        @NotBlank(message = "filePath must not be blank")
        String filePath
) {}
