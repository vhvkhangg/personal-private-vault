package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublicCredentialRequest(
        @JsonProperty(required = true)
        @NotBlank(message = "Private password must not be blank")
        @Size(max = 128, message = "Private password must not exceed 128 characters")
        String privatePassword,

        @JsonProperty(required = true)
        @NotBlank(message = "Public password must not be blank")
        @Size(min = 12, max = 128, message = "Public password must be between 12 and 128 characters")
        String publicPassword
) {
    @Override
    public String toString() {
        return "PublicCredentialRequest[privatePassword=[REDACTED], publicPassword=[REDACTED]]";
    }
}
