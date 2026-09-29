package com.vhvkhangg.personalprivatevault.account.account;

import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;

/**
 * Command to update metadata of an existing External Account.
 */
public record UpdateExternalAccountCommand(
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
