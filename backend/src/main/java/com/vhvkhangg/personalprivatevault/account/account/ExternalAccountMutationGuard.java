package com.vhvkhangg.personalprivatevault.account.account;

import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;

/**
 * Guard interface allowing dependent modules to validate proposed mutations to an external account
 * (such as changing account type or platform) before the mutation is committed.
 */
public interface ExternalAccountMutationGuard {

    void validateMutation(Long accountId, ExternalAccountType newType, Long newPlatformId);
}
