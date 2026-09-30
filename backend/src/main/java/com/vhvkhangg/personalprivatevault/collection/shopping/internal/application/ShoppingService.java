package com.vhvkhangg.personalprivatevault.collection.shopping.internal.application;

import com.vhvkhangg.personalprivatevault.collection.shopping.enums.ShoppingStatus;
import com.vhvkhangg.personalprivatevault.collection.shopping.internal.domain.ShoppingItem;
import com.vhvkhangg.personalprivatevault.collection.shopping.internal.infrastructure.persistence.ShoppingItemRepository;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.CreateShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.InvalidShoppingItemException;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.ShoppingItemNotFoundException;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.ShoppingOperations;
import com.vhvkhangg.personalprivatevault.collection.shopping.shopping.UpdateShoppingItemCommand;
import com.vhvkhangg.personalprivatevault.collection.shopping.view.ShoppingItemView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

/**
 * Application service implementing {@link ShoppingOperations}.
 */
@Service
@RequiredArgsConstructor
public class ShoppingService implements ShoppingOperations {

    private final ShoppingItemRepository shoppingItemRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final ReferenceCatalog referenceCatalog;

    @Override
    @Transactional
    public ShoppingItemView create(CreateShoppingItemCommand command) {
        if (command == null) {
            throw new InvalidShoppingItemException("CreateShoppingItemCommand must not be null");
        }

        ValidatedShoppingItem validated = validateShoppingItem(
                command.name(),
                command.avatarUrl(),
                command.description(),
                command.priceAmount(),
                command.currencyCode(),
                command.platformId(),
                command.status(),
                command.url(),
                command.purchasedAt()
        );

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.SHOPPING);

        ShoppingItem item = new ShoppingItem(
                vaultEntry.id(),
                validated.name(),
                validated.avatarUrl(),
                validated.description(),
                validated.priceAmount(),
                validated.currencyCode(),
                validated.platformId(),
                validated.status(),
                validated.url(),
                validated.purchasedAt()
        );

        shoppingItemRepository.saveAndFlush(item);
        return toView(item);
    }

    @Override
    @Transactional
    public ShoppingItemView update(Long id, UpdateShoppingItemCommand command) {
        if (id == null) {
            throw new InvalidShoppingItemException("Shopping item ID must not be null");
        }
        if (command == null) {
            throw new InvalidShoppingItemException("UpdateShoppingItemCommand must not be null");
        }

        ShoppingItem item = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new ShoppingItemNotFoundException(id));

        ValidatedShoppingItem validated = validateShoppingItem(
                command.name(),
                command.avatarUrl(),
                command.description(),
                command.priceAmount(),
                command.currencyCode(),
                command.platformId(),
                command.status(),
                command.url(),
                command.purchasedAt()
        );

        item.update(
                validated.name(),
                validated.avatarUrl(),
                validated.description(),
                validated.priceAmount(),
                validated.currencyCode(),
                validated.platformId(),
                validated.status(),
                validated.url(),
                validated.purchasedAt()
        );

        shoppingItemRepository.saveAndFlush(item);
        return toView(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingItemView> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return shoppingItemRepository.findById(id).map(this::toView);
    }

    private ValidatedShoppingItem validateShoppingItem(
            String rawName,
            String rawAvatarUrl,
            String rawDescription,
            BigDecimal priceAmount,
            String rawCurrencyCode,
            Long platformId,
            ShoppingStatus rawStatus,
            String rawUrl,
            Instant purchasedAt
    ) {
        if (rawName == null || rawName.isBlank()) {
            throw new InvalidShoppingItemException("Shopping item name must not be blank");
        }
        String name = rawName.trim();
        if (name.length() > 500) {
            throw new InvalidShoppingItemException("Shopping item name must not exceed 500 characters");
        }

        String avatarUrl = trimOrNull(rawAvatarUrl);
        if (avatarUrl != null && avatarUrl.length() > 2048) {
            throw new InvalidShoppingItemException("Avatar URL must not exceed 2048 characters");
        }

        String description = trimOrNull(rawDescription);

        if (priceAmount != null && priceAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidShoppingItemException("Price amount must be nonnegative");
        }

        String currencyCode = trimOrNull(rawCurrencyCode);
        if (priceAmount != null && currencyCode == null) {
            throw new InvalidShoppingItemException("Currency code is required when price amount is present");
        }

        if (currencyCode != null) {
            currencyCode = currencyCode.toUpperCase(Locale.ROOT);
            if (currencyCode.length() != 3) {
                throw new InvalidShoppingItemException("Currency code must be exactly 3 characters");
            }
            if (referenceCatalog.currency(currencyCode).isEmpty()) {
                throw new InvalidShoppingItemException("Currency code '" + currencyCode + "' does not exist in reference catalog");
            }
        }

        if (platformId != null && referenceCatalog.platform(platformId).isEmpty()) {
            throw new InvalidShoppingItemException("Platform with id " + platformId + " does not exist in reference catalog");
        }

        ShoppingStatus status = rawStatus != null ? rawStatus : ShoppingStatus.WISHLIST;

        if (status == ShoppingStatus.WISHLIST && purchasedAt != null) {
            throw new InvalidShoppingItemException("Wishlist item must not have a purchased_at timestamp");
        }

        String url = trimOrNull(rawUrl);
        if (url != null && url.length() > 2048) {
            throw new InvalidShoppingItemException("URL must not exceed 2048 characters");
        }

        return new ValidatedShoppingItem(
                name,
                avatarUrl,
                description,
                priceAmount,
                currencyCode,
                platformId,
                status,
                url,
                purchasedAt
        );
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ShoppingItemView toView(ShoppingItem item) {
        return new ShoppingItemView(
                item.getId(),
                item.getName(),
                item.getAvatarUrl(),
                item.getDescription(),
                item.getPriceAmount(),
                item.getCurrencyCode(),
                item.getPlatformId(),
                item.getStatus(),
                item.getUrl(),
                item.getPurchasedAt()
        );
    }

    private record ValidatedShoppingItem(
            String name,
            String avatarUrl,
            String description,
            BigDecimal priceAmount,
            String currencyCode,
            Long platformId,
            ShoppingStatus status,
            String url,
            Instant purchasedAt
    ) {}
}
