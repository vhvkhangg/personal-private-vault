package com.vhvkhangg.personalprivatevault.collection.software.internal.application;

import com.vhvkhangg.personalprivatevault.collection.software.enums.SoftwareType;
import com.vhvkhangg.personalprivatevault.collection.software.internal.domain.SoftwareItem;
import com.vhvkhangg.personalprivatevault.collection.software.internal.infrastructure.persistence.SoftwareItemPlatformRepository;
import com.vhvkhangg.personalprivatevault.collection.software.internal.infrastructure.persistence.SoftwareItemRepository;
import com.vhvkhangg.personalprivatevault.collection.software.software.CreateSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.software.software.InvalidSoftwareItemException;
import com.vhvkhangg.personalprivatevault.collection.software.software.SoftwareItemNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.software.software.SoftwareOperations;
import com.vhvkhangg.personalprivatevault.collection.software.software.UpdateSoftwareItemCommand;
import com.vhvkhangg.personalprivatevault.collection.software.view.SoftwareItemView;
import com.vhvkhangg.personalprivatevault.collection.software.view.SoftwarePlatformView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Application service implementing {@link SoftwareOperations}.
 */
@Service
@RequiredArgsConstructor
public class SoftwareService implements SoftwareOperations {

    private static final BigDecimal MAX_PRICE = new BigDecimal("999999999999999.9999");

    private final SoftwareItemRepository softwareItemRepository;
    private final SoftwareItemPlatformRepository softwareItemPlatformRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final ReferenceCatalog referenceCatalog;

    @Override
    @Transactional
    public SoftwareItemView create(CreateSoftwareItemCommand command) {
        if (command == null) {
            throw new InvalidSoftwareItemException("CreateSoftwareItemCommand must not be null");
        }

        ValidatedSoftwareItem validated = validateSoftwareItem(
                command.name(),
                command.type(),
                command.logoUrl(),
                command.description(),
                command.priceAmount(),
                command.currencyCode(),
                command.url(),
                command.review()
        );

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.SOFTWARE);

        SoftwareItem item = new SoftwareItem(
                vaultEntry.id(),
                validated.name(),
                validated.type(),
                validated.logoUrl(),
                validated.description(),
                validated.priceAmount(),
                validated.currencyCode(),
                validated.url(),
                validated.review()
        );

        softwareItemRepository.saveAndFlush(item);
        return toView(item);
    }

    @Override
    @Transactional
    public SoftwareItemView update(Long id, UpdateSoftwareItemCommand command) {
        if (id == null) {
            throw new InvalidSoftwareItemException("Software item ID must not be null");
        }
        if (command == null) {
            throw new InvalidSoftwareItemException("UpdateSoftwareItemCommand must not be null");
        }

        SoftwareItem item = softwareItemRepository.findById(id)
                .orElseThrow(() -> new SoftwareItemNotFoundException(id));

        ValidatedSoftwareItem validated = validateSoftwareItem(
                command.name(),
                command.type(),
                command.logoUrl(),
                command.description(),
                command.priceAmount(),
                command.currencyCode(),
                command.url(),
                command.review()
        );

        item.update(
                validated.name(),
                validated.type(),
                validated.logoUrl(),
                validated.description(),
                validated.priceAmount(),
                validated.currencyCode(),
                validated.url(),
                validated.review()
        );

        softwareItemRepository.saveAndFlush(item);
        return toView(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SoftwareItemView> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return softwareItemRepository.findById(id).map(this::toView);
    }

    @Override
    @Transactional
    public void addPlatform(Long softwareId, Long platformId) {
        if (softwareId == null) {
            throw new InvalidSoftwareItemException("Software ID must not be null");
        }
        if (!softwareItemRepository.existsById(softwareId)) {
            throw new SoftwareItemNotFoundException(softwareId);
        }
        if (platformId == null) {
            throw new InvalidSoftwareItemException("Platform ID must not be null");
        }
        if (referenceCatalog.platform(platformId).isEmpty()) {
            throw new InvalidSoftwareItemException("Platform with id " + platformId + " does not exist in reference catalog");
        }

        softwareItemPlatformRepository.insertIfAbsent(softwareId, platformId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SoftwarePlatformView> findPlatforms(Long softwareId, int limit) {
        if (softwareId == null) {
            throw new InvalidSoftwareItemException("Software ID must not be null");
        }
        if (limit <= 0) {
            throw new InvalidSoftwareItemException("Limit must be positive: " + limit);
        }
        if (!softwareItemRepository.existsById(softwareId)) {
            throw new SoftwareItemNotFoundException(softwareId);
        }

        PageRequest pageRequest = PageRequest.of(0, limit);
        return softwareItemPlatformRepository.findPlatformsBySoftwareId(softwareId, pageRequest).stream()
                .map(p -> new SoftwarePlatformView(p.getId().getSoftwareId(), p.getId().getPlatformId()))
                .toList();
    }

    private ValidatedSoftwareItem validateSoftwareItem(
            String rawName,
            SoftwareType type,
            String rawLogoUrl,
            String rawDescription,
            BigDecimal priceAmount,
            String rawCurrencyCode,
            String rawUrl,
            String rawReview
    ) {
        if (rawName == null || rawName.isBlank()) {
            throw new InvalidSoftwareItemException("Software item name must not be blank");
        }
        String name = rawName.trim();
        if (name.length() > 500) {
            throw new InvalidSoftwareItemException("Software item name must not exceed 500 characters");
        }

        if (type == null) {
            throw new InvalidSoftwareItemException("Software type is required");
        }

        String logoUrl = trimOrNull(rawLogoUrl);
        if (logoUrl != null && logoUrl.length() > 2048) {
            throw new InvalidSoftwareItemException("Logo URL must not exceed 2048 characters");
        }

        String description = trimOrNull(rawDescription);

        if (priceAmount != null) {
            if (priceAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidSoftwareItemException("Price amount must be nonnegative");
            }
            if (priceAmount.compareTo(MAX_PRICE) > 0) {
                throw new InvalidSoftwareItemException("Price amount exceeds maximum precision 19");
            }
            if (priceAmount.scale() > 4 && priceAmount.stripTrailingZeros().scale() > 4) {
                throw new InvalidSoftwareItemException("Price amount scale must not exceed 4 without rounding");
            }
        }

        String currencyCode = trimOrNull(rawCurrencyCode);
        if (priceAmount != null && currencyCode == null) {
            throw new InvalidSoftwareItemException("Currency code is required when price amount is present");
        }

        if (currencyCode != null) {
            currencyCode = currencyCode.toUpperCase(Locale.ROOT);
            if (currencyCode.length() != 3) {
                throw new InvalidSoftwareItemException("Currency code must be exactly 3 characters");
            }
            if (referenceCatalog.currency(currencyCode).isEmpty()) {
                throw new InvalidSoftwareItemException("Currency code '" + currencyCode + "' does not exist in reference catalog");
            }
        }

        String url = trimOrNull(rawUrl);
        if (url != null && url.length() > 2048) {
            throw new InvalidSoftwareItemException("URL must not exceed 2048 characters");
        }

        String review = trimOrNull(rawReview);

        return new ValidatedSoftwareItem(
                name,
                type,
                logoUrl,
                description,
                priceAmount,
                currencyCode,
                url,
                review
        );
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SoftwareItemView toView(SoftwareItem item) {
        return new SoftwareItemView(
                item.getId(),
                item.getName(),
                item.getType(),
                item.getLogoUrl(),
                item.getDescription(),
                item.getPriceAmount(),
                item.getCurrencyCode(),
                item.getUrl(),
                item.getReview()
        );
    }

    private record ValidatedSoftwareItem(
            String name,
            SoftwareType type,
            String logoUrl,
            String description,
            BigDecimal priceAmount,
            String currencyCode,
            String url,
            String review
    ) {}
}
