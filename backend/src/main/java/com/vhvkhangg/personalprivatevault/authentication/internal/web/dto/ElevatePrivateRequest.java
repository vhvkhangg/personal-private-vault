package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ElevatePrivateRequest(
        @JsonProperty(required = true)
        @NotBlank(message = "Private password must not be blank")
        @Size(max = 128, message = "Private password must not exceed 128 characters")
        String privatePassword
) {
    @Override
    public String toString() {
        return "ElevatePrivateRequest[privatePassword=[REDACTED]]";
    }
}
