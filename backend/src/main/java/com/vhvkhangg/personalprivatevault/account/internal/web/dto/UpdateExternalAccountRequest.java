package com.vhvkhangg.personalprivatevault.account.internal.web.dto;

import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateExternalAccountRequest(
        @NotNull Long platformId,
        @NotNull ExternalAccountOwnership ownership,
        @NotNull ExternalAccountType accountType,
        @NotBlank @Size(max = 255) String username,
        @Size(max = 255) String externalId,
        @Size(max = 255) String displayName,
        @Size(max = 2048) String avatarUrl,
        @Size(max = 2048) String bannerUrl,
        String profileDescription,
        @Size(max = 255) String ownerName,
        @Size(max = 2048) String url,
        String notes
) {}
