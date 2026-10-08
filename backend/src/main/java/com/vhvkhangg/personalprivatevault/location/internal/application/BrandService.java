package com.vhvkhangg.personalprivatevault.location.internal.application;

import com.vhvkhangg.personalprivatevault.location.brand.BrandNotFoundException;
import com.vhvkhangg.personalprivatevault.location.brand.BrandOperations;
import com.vhvkhangg.personalprivatevault.location.brand.CreateBrandCommand;
import com.vhvkhangg.personalprivatevault.location.brand.InvalidBrandException;
import com.vhvkhangg.personalprivatevault.location.brand.UpdateBrandCommand;
import com.vhvkhangg.personalprivatevault.location.internal.domain.Brand;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.BrandRepository;
import com.vhvkhangg.personalprivatevault.location.view.BrandView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

@Service
public class BrandService implements BrandOperations {

    private final BrandRepository brandRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final ReferenceCatalog referenceCatalog;

    @Autowired
    public BrandService(
            BrandRepository brandRepository,
            VaultEntryOperations vaultEntryOperations,
            ReferenceCatalog referenceCatalog
    ) {
        this.brandRepository = Objects.requireNonNull(brandRepository, "brandRepository must not be null");
        this.vaultEntryOperations = Objects.requireNonNull(vaultEntryOperations, "vaultEntryOperations must not be null");
        this.referenceCatalog = Objects.requireNonNull(referenceCatalog, "referenceCatalog must not be null");
    }

    @Override
    @Transactional
    public BrandView create(CreateBrandCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        ValidatedBrand validated = validate(
                command.name(),
                command.logoUrl(),
                command.nationalityCode(),
                command.description(),
                command.minPrice(),
                command.maxPrice(),
                command.currencyCode(),
                command.review()
        );

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.BRAND);
        Brand brand = new Brand(
                vaultEntry.id(),
                validated.name(),
                validated.logoUrl(),
                validated.nationalityCode(),
                validated.description(),
                validated.minPrice(),
                validated.maxPrice(),
                validated.currencyCode(),
                validated.review(),
                true
        );
        Brand saved = brandRepository.save(brand);
        return toView(saved);
    }

    @Override
    @Transactional
    public BrandView update(UpdateBrandCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        if (command.id() == null) {
            throw new InvalidBrandException("Brand id must not be null");
        }

        Brand brand = brandRepository.findById(command.id())
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with id: " + command.id()));

        ValidatedBrand validated = validate(
                command.name(),
                command.logoUrl(),
                command.nationalityCode(),
                command.description(),
                command.minPrice(),
                command.maxPrice(),
                command.currencyCode(),
                command.review()
        );

        brand.update(
                validated.name(),
                validated.logoUrl(),
                validated.nationalityCode(),
                validated.description(),
                validated.minPrice(),
                validated.maxPrice(),
                validated.currencyCode(),
                validated.review()
        );
        Brand saved = brandRepository.save(brand);
        return toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandView findById(Long id) {
        if (id == null) {
            throw new InvalidBrandException("Brand id must not be null");
        }
        return brandRepository.findById(id)
                .map(this::toView)
                .orElseThrow(() -> new BrandNotFoundException("Brand not found with id: " + id));
    }

    private ValidatedBrand validate(
            String name,
            String logoUrl,
            String nationalityCode,
            String description,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String currencyCode,
            String review
    ) {
        if (name == null || name.isBlank()) {
            throw new InvalidBrandException("Brand name must not be blank");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() > 255) {
            throw new InvalidBrandException("Brand name must not exceed 255 characters");
        }

        String trimmedLogoUrl = null;
        if (logoUrl != null && !logoUrl.isBlank()) {
            trimmedLogoUrl = logoUrl.trim();
            if (trimmedLogoUrl.length() > 2048) {
                throw new InvalidBrandException("Brand logo URL must not exceed 2048 characters");
            }
        }

        String validatedNationalityCode = null;
        if (nationalityCode != null && !nationalityCode.isBlank()) {
            String upper = nationalityCode.trim().toUpperCase(Locale.ROOT);
            if (referenceCatalog.country(upper).isEmpty()) {
                throw new InvalidBrandException("Nationality code '" + nationalityCode + "' does not exist in reference catalog");
            }
            validatedNationalityCode = upper;
        }

        String validatedCurrencyCode = null;
        if (currencyCode != null && !currencyCode.isBlank()) {
            String upper = currencyCode.trim().toUpperCase(Locale.ROOT);
            if (referenceCatalog.currency(upper).isEmpty()) {
                throw new InvalidBrandException("Currency code '" + currencyCode + "' does not exist in reference catalog");
            }
            validatedCurrencyCode = upper;
        }

        validatePriceBounds(minPrice, "Brand minPrice");
        validatePriceBounds(maxPrice, "Brand maxPrice");
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidBrandException("Brand minPrice cannot exceed maxPrice");
        }
        if ((minPrice != null || maxPrice != null) && validatedCurrencyCode == null) {
            throw new InvalidBrandException("Brand currencyCode is required when minPrice or maxPrice is specified");
        }

        String trimmedDescription = description != null && !description.isBlank() ? description.trim() : null;
        String trimmedReview = review != null && !review.isBlank() ? review.trim() : null;

        return new ValidatedBrand(
                trimmedName,
                trimmedLogoUrl,
                validatedNationalityCode,
                trimmedDescription,
                minPrice,
                maxPrice,
                validatedCurrencyCode,
                trimmedReview
        );
    }

    private BrandView toView(Brand brand) {
        return new BrandView(
                brand.getId(),
                brand.getName(),
                brand.getLogoUrl(),
                brand.getNationalityCode(),
                brand.getDescription(),
                brand.getMinPrice(),
                brand.getMaxPrice(),
                brand.getCurrencyCode(),
                brand.getReview()
        );
    }

    private static final BigDecimal MAX_PRICE = new BigDecimal("999999999999999.9999");

    private void validatePriceBounds(BigDecimal price, String fieldName) {
        if (price == null) {
            return;
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBrandException(fieldName + " must be non-negative");
        }
        if (price.compareTo(MAX_PRICE) > 0) {
            throw new InvalidBrandException(fieldName + " exceeds maximum precision 19");
        }
        if (price.scale() > 4 && price.stripTrailingZeros().scale() > 4) {
            throw new InvalidBrandException(fieldName + " scale must not exceed 4 without rounding");
        }
    }

    private record ValidatedBrand(
            String name,
            String logoUrl,
            String nationalityCode,
            String description,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String currencyCode,
            String review
    ) {
    }
}
