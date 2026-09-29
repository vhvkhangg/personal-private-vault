package com.vhvkhangg.personalprivatevault.account.view;

import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;

/**
 * Immutable view of an External Account.
 */
public record ExternalAccountView(
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
) {
}
