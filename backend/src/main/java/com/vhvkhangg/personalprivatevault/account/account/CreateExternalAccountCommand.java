package com.vhvkhangg.personalprivatevault.account.account;

import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;

/**
 * Command to create a new External Account backed by a Vault Entry.
 */
public record CreateExternalAccountCommand(
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
