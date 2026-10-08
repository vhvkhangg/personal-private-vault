package com.vhvkhangg.personalprivatevault.account.internal.application;

import com.vhvkhangg.personalprivatevault.account.account.CreateExternalAccountCommand;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountConflictException;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountNotFoundException;
import com.vhvkhangg.personalprivatevault.account.account.ExternalAccountOperations;
import com.vhvkhangg.personalprivatevault.account.account.InvalidExternalAccountException;
import com.vhvkhangg.personalprivatevault.account.account.UpdateExternalAccountCommand;
import com.vhvkhangg.personalprivatevault.account.internal.domain.ExternalAccount;
import com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence.ExternalAccountRepository;
import com.vhvkhangg.personalprivatevault.account.view.ExternalAccountView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExternalAccountService implements ExternalAccountOperations {

    private final ReferenceCatalog referenceCatalog;
    private final VaultEntryOperations vaultEntryOperations;
    private final ExternalAccountRepository externalAccountRepository;
    private final List<com.vhvkhangg.personalprivatevault.account.account.ExternalAccountMutationGuard> mutationGuards;
    private final jakarta.persistence.EntityManager entityManager;

    @Override
    @Transactional
    public ExternalAccountView create(CreateExternalAccountCommand command) {
        if (command == null) {
            throw new InvalidExternalAccountException("CreateExternalAccountCommand must not be null");
        }
        validateIdentifiersAndMetadata(
                command.platformId(),
                command.ownership(),
                command.accountType(),
                command.username(),
                command.externalId(),
                command.displayName(),
                command.avatarUrl(),
                command.bannerUrl(),
                command.ownerName(),
                command.url()
        );

        String trimmedExternalId = trimOrNull(command.externalId());
        if (trimmedExternalId != null) {
            if (externalAccountRepository.findByPlatformIdAndExternalId(command.platformId(), trimmedExternalId).isPresent()) {
                throw new ExternalAccountConflictException(
                        "External account already exists for platform ID " + command.platformId() + " and the specified external ID"
                );
            }
        }

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.EXTERNAL_ACCOUNT);

        ExternalAccount account = new ExternalAccount(
                vaultEntry.id(),
                command.platformId(),
                command.ownership(),
                command.accountType(),
                trimOrNull(command.username()),
                trimmedExternalId,
                trimOrNull(command.displayName()),
                trimOrNull(command.avatarUrl()),
                trimOrNull(command.bannerUrl()),
                trimOrNull(command.profileDescription()),
                trimOrNull(command.ownerName()),
                trimOrNull(command.url()),
                trimOrNull(command.notes()),
                true
        );

        try {
            externalAccountRepository.saveAndFlush(account);
        } catch (DataIntegrityViolationException ex) {
            String constraint = extractConstraintName(ex);
            if (constraint.contains("platform_id_external_id") || constraint.contains("external_accounts_platform_id_external_id")) {
                throw new ExternalAccountConflictException(
                        "External account already exists for platform ID " + command.platformId() + " and the specified external ID"
                );
            }
            throw new ExternalAccountConflictException("External account conflict occurred during creation");
        }

        return toView(account);
    }

    @Override
    @Transactional
    public ExternalAccountView update(Long id, UpdateExternalAccountCommand command) {
        if (id == null) {
            throw new InvalidExternalAccountException("External account ID must not be null");
        }
        if (command == null) {
            throw new InvalidExternalAccountException("UpdateExternalAccountCommand must not be null");
        }

        ExternalAccount account = externalAccountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ExternalAccountNotFoundException("External account with id " + id + " does not exist"));
        entityManager.refresh(account);

        if (account.getAccountType() != command.accountType() || !Objects.equals(account.getPlatformId(), command.platformId())) {
            for (com.vhvkhangg.personalprivatevault.account.account.ExternalAccountMutationGuard guard : mutationGuards) {
                guard.validateMutation(id, command.accountType(), command.platformId());
            }
        }

        validateIdentifiersAndMetadata(
                command.platformId(),
                command.ownership(),
                command.accountType(),
                command.username(),
                command.externalId(),
                command.displayName(),
                command.avatarUrl(),
                command.bannerUrl(),
                command.ownerName(),
                command.url()
        );

        String trimmedExternalId = trimOrNull(command.externalId());
        if (trimmedExternalId != null) {
            Optional<ExternalAccount> duplicate = externalAccountRepository.findByPlatformIdAndExternalId(command.platformId(), trimmedExternalId);
            if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                throw new ExternalAccountConflictException(
                        "External account already exists for platform ID " + command.platformId() + " and the specified external ID"
                );
            }
        }

        account.update(
                command.platformId(),
                command.ownership(),
                command.accountType(),
                trimOrNull(command.username()),
                trimmedExternalId,
                trimOrNull(command.displayName()),
                trimOrNull(command.avatarUrl()),
                trimOrNull(command.bannerUrl()),
                trimOrNull(command.profileDescription()),
                trimOrNull(command.ownerName()),
                trimOrNull(command.url()),
                trimOrNull(command.notes())
        );

        try {
            externalAccountRepository.saveAndFlush(account);
        } catch (DataIntegrityViolationException ex) {
            String constraint = extractConstraintName(ex);
            if (constraint.contains("platform_id_external_id") || constraint.contains("external_accounts_platform_id_external_id")) {
                throw new ExternalAccountConflictException(
                        "External account already exists for platform ID " + command.platformId() + " and the specified external ID"
                );
            }
            throw new ExternalAccountConflictException("External account conflict occurred during update");
        }

        return toView(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExternalAccountView> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return externalAccountRepository.findById(id).map(this::toView);
    }

    @Override
    @Transactional
    public Optional<ExternalAccountView> findAndLock(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return externalAccountRepository.findByIdForUpdate(id)
                .map(account -> {
                    entityManager.refresh(account);
                    return toView(account);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalAccountView> findRecentByPlatformId(Long platformId, int limit) {
        if (platformId == null) {
            return List.of();
        }
        int clampedLimit = Math.clamp(limit, 1, 100);
        return externalAccountRepository.findRecentByPlatformId(platformId, PageRequest.of(0, clampedLimit))
                .stream()
                .map(this::toView)
                .toList();
    }

    private void validateIdentifiersAndMetadata(
            Long platformId,
            Object ownership,
            Object accountType,
            String username,
            String externalId,
            String displayName,
            String avatarUrl,
            String bannerUrl,
            String ownerName,
            String url
    ) {
        if (platformId == null) {
            throw new InvalidExternalAccountException("Platform ID must not be null");
        }
        if (referenceCatalog.platform(platformId).isEmpty()) {
            throw new InvalidExternalAccountException("Platform with id " + platformId + " does not exist");
        }
        if (ownership == null) {
            throw new InvalidExternalAccountException("Ownership must not be null");
        }
        if (accountType == null) {
            throw new InvalidExternalAccountException("AccountType must not be null");
        }

        String trimmedUser = trimOrNull(username);
        String trimmedExtId = trimOrNull(externalId);
        String trimmedUrl = trimOrNull(url);

        if (trimmedUser == null && trimmedExtId == null && trimmedUrl == null) {
            throw new InvalidExternalAccountException(
                    "At least one identifier (username, external_id, or url) must be present"
            );
        }

        if (trimmedUser != null && trimmedUser.length() > 255) {
            throw new InvalidExternalAccountException("Username must not exceed 255 characters");
        }
        if (trimmedExtId != null && trimmedExtId.length() > 255) {
            throw new InvalidExternalAccountException("External ID must not exceed 255 characters");
        }
        String trimmedDisplayName = trimOrNull(displayName);
        if (trimmedDisplayName != null && trimmedDisplayName.length() > 500) {
            throw new InvalidExternalAccountException("Display name must not exceed 500 characters");
        }
        String trimmedAvatarUrl = trimOrNull(avatarUrl);
        if (trimmedAvatarUrl != null && trimmedAvatarUrl.length() > 2048) {
            throw new InvalidExternalAccountException("Avatar URL must not exceed 2048 characters");
        }
        String trimmedBannerUrl = trimOrNull(bannerUrl);
        if (trimmedBannerUrl != null && trimmedBannerUrl.length() > 2048) {
            throw new InvalidExternalAccountException("Banner URL must not exceed 2048 characters");
        }
        String trimmedOwnerName = trimOrNull(ownerName);
        if (trimmedOwnerName != null && trimmedOwnerName.length() > 500) {
            throw new InvalidExternalAccountException("Owner name must not exceed 500 characters");
        }
        if (trimmedUrl != null && trimmedUrl.length() > 2048) {
            throw new InvalidExternalAccountException("URL must not exceed 2048 characters");
        }
    }

    private String extractConstraintName(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException cve) {
                return cve.getConstraintName() != null ? cve.getConstraintName().toLowerCase(Locale.ROOT) : "";
            }
            cause = cause.getCause();
        }
        return "";
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ExternalAccountView toView(ExternalAccount account) {
        return new ExternalAccountView(
                account.getId(),
                account.getPlatformId(),
                account.getOwnership(),
                account.getAccountType(),
                account.getUsername(),
                account.getExternalId(),
                account.getDisplayName(),
                account.getAvatarUrl(),
                account.getBannerUrl(),
                account.getProfileDescription(),
                account.getOwnerName(),
                account.getUrl(),
                account.getNotes()
        );
    }
}
