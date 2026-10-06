package com.vhvkhangg.personalprivatevault.account.internal.web.dto;

import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;

public record ExternalAccountResponse(
        Long id,
        Long platformId,
        ExternalAccountOwnership ownership,
        ExternalAccountType accountType,
        String username,
        String externalId,
        String displayName,
        String avatarUrl,
        String bannerUrl,
        String profileDescription,
        String ownerName,
        String url,
        String notes
) {}
