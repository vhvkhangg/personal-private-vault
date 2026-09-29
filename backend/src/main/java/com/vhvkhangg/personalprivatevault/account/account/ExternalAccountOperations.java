package com.vhvkhangg.personalprivatevault.account.account;

import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountView;

import java.util.List;
import java.util.Optional;

/**
 * Public capability-oriented contract for External Account operations.
 */
public interface ExternalAccountOperations {

    ExternalAccountView create(CreateExternalAccountCommand command);

    ExternalAccountView update(Long id, UpdateExternalAccountCommand command);

    Optional<ExternalAccountView> findById(Long id);

    List<ExternalAccountView> findRecentByPlatformId(Long platformId, int limit);
}
