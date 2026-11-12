package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PublicProfileLoginRequest(
        @JsonProperty(required = true)
        @NotNull(message = "Profile is required")
        @Pattern(regexp = "^PUBLIC$", message = "profile must be exactly 'PUBLIC'")
        String profile,

        @JsonProperty(required = true)
        @NotBlank(message = "Password must not be blank")
        @Size(max = 128, message = "Password must not exceed 128 characters")
        String password
) {
    @Override
    public String toString() {
        return "PublicProfileLoginRequest[profile=" + profile + ", password=[REDACTED]]";
    }
}
