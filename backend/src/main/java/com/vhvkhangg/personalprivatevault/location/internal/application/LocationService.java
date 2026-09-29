package com.vhvkhangg.personalprivatevault.location.internal.application;

import com.vhvkhangg.personalprivatevault.location.enums.DiningServiceStyle;
import com.vhvkhangg.personalprivatevault.location.internal.domain.Location;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.AddressRepository;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.BrandRepository;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationDiningServiceStyleRepository;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationRepository;
import com.vhvkhangg.personalprivatevault.location.location.CreateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.location.InvalidLocationException;
import com.vhvkhangg.personalprivatevault.location.location.LocationNotFoundException;
import com.vhvkhangg.personalprivatevault.location.location.LocationOperations;
import com.vhvkhangg.personalprivatevault.location.location.UpdateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.view.LocationView;
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
import java.util.Set;

@Service
public class LocationService implements LocationOperations {

    private final LocationRepository locationRepository;
    private final BrandRepository brandRepository;
    private final AddressRepository addressRepository;
    private final LocationDiningServiceStyleRepository diningServiceStyleRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final ReferenceCatalog referenceCatalog;

    @Autowired
    public LocationService(
            LocationRepository locationRepository,
            BrandRepository brandRepository,
            AddressRepository addressRepository,
            LocationDiningServiceStyleRepository diningServiceStyleRepository,
            VaultEntryOperations vaultEntryOperations,
            ReferenceCatalog referenceCatalog
    ) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepository must not be null");
        this.brandRepository = Objects.requireNonNull(brandRepository, "brandRepository must not be null");
        this.addressRepository = Objects.requireNonNull(addressRepository, "addressRepository must not be null");
        this.diningServiceStyleRepository = Objects.requireNonNull(diningServiceStyleRepository, "diningServiceStyleRepository must not be null");
        this.vaultEntryOperations = Objects.requireNonNull(vaultEntryOperations, "vaultEntryOperations must not be null");
        this.referenceCatalog = Objects.requireNonNull(referenceCatalog, "referenceCatalog must not be null");
    }

    @Override
    @Transactional
    public LocationView create(CreateLocationCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        ValidatedLocation validated = validate(
                command.brandId(),
                command.addressId(),
                command.name(),
                command.imageUrl(),
                command.description(),
                command.phone(),
                command.websiteUrl(),
                command.minPrice(),
                command.maxPrice(),
                command.currencyCode(),
                command.review()
        );

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.LOCATION);
        Location location = new Location(
                vaultEntry.id(),
                validated.brandId(),
                validated.addressId(),
                validated.name(),
                validated.imageUrl(),
                validated.description(),
                validated.phone(),
                validated.websiteUrl(),
                false,
                validated.minPrice(),
                validated.maxPrice(),
                validated.currencyCode(),
                validated.review(),
                true
        );
        Location saved = locationRepository.save(location);
        return toView(saved);
    }

    @Override
    @Transactional
    public LocationView update(UpdateLocationCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        if (command.id() == null) {
            throw new InvalidLocationException("Location id must not be null");
        }

        Location location = locationRepository.findById(command.id())
                .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + command.id()));

        ValidatedLocation validated = validate(
                command.brandId(),
                command.addressId(),
                command.name(),
                command.imageUrl(),
                command.description(),
                command.phone(),
                command.websiteUrl(),
                command.minPrice(),
                command.maxPrice(),
                command.currencyCode(),
                command.review()
        );

        location.update(
                validated.brandId(),
                validated.addressId(),
                validated.name(),
                validated.imageUrl(),
                validated.description(),
                validated.phone(),
                validated.websiteUrl(),
                validated.minPrice(),
                validated.maxPrice(),
                validated.currencyCode(),
                validated.review()
        );
        Location saved = locationRepository.save(location);
        return toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationView findById(Long id) {
        if (id == null) {
            throw new InvalidLocationException("Location id must not be null");
        }
        return locationRepository.findById(id)
                .map(this::toView)
                .orElseThrow(() -> new LocationNotFoundException("Location not found with id: " + id));
    }

    @Override
    @Transactional
    public void assignDiningServiceStyle(Long locationId, DiningServiceStyle style) {
        if (locationId == null) {
            throw new InvalidLocationException("locationId must not be null");
        }
        if (style == null) {
            throw new InvalidLocationException("style must not be null");
        }
        if (!locationRepository.existsById(locationId)) {
            throw new LocationNotFoundException("Location not found with id: " + locationId);
        }
        diningServiceStyleRepository.insertIfAbsent(locationId, style.name());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<DiningServiceStyle> findDiningServiceStylesByLocationId(Long locationId) {
        if (locationId == null) {
            throw new InvalidLocationException("locationId must not be null");
        }
        if (!locationRepository.existsById(locationId)) {
            throw new LocationNotFoundException("Location not found with id: " + locationId);
        }
        return diningServiceStyleRepository.findServiceStylesByLocationId(locationId);
    }

    private ValidatedLocation validate(
            Long brandId,
            Long addressId,
            String name,
            String imageUrl,
            String description,
            String phone,
            String websiteUrl,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String currencyCode,
            String review
    ) {
        if (addressId == null) {
            throw new InvalidLocationException("Location addressId must not be null");
        }
        if (!addressRepository.existsById(addressId)) {
            throw new InvalidLocationException("Address not found with id: " + addressId);
        }

        if (brandId != null && !brandRepository.existsById(brandId)) {
            throw new InvalidLocationException("Brand not found with id: " + brandId);
        }

        if (name == null || name.isBlank()) {
            throw new InvalidLocationException("Location name must not be blank");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() > 500) {
            throw new InvalidLocationException("Location name must not exceed 500 characters");
        }

        String trimmedImageUrl = trimIfPresent(imageUrl, 2048, "Location imageUrl");
        String trimmedPhone = trimIfPresent(phone, 64, "Location phone");
        String trimmedWebsiteUrl = trimIfPresent(websiteUrl, 2048, "Location websiteUrl");

        String validatedCurrencyCode = null;
        if (currencyCode != null && !currencyCode.isBlank()) {
            String upper = currencyCode.trim().toUpperCase(Locale.ROOT);
            if (referenceCatalog.currency(upper).isEmpty()) {
                throw new InvalidLocationException("Currency code '" + currencyCode + "' does not exist in reference catalog");
            }
            validatedCurrencyCode = upper;
        }

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLocationException("Location minPrice must be non-negative");
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLocationException("Location maxPrice must be non-negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidLocationException("Location minPrice cannot exceed maxPrice");
        }
        if ((minPrice != null || maxPrice != null) && validatedCurrencyCode == null) {
            throw new InvalidLocationException("Location currencyCode is required when minPrice or maxPrice is specified");
        }

        String trimmedDescription = description != null && !description.isBlank() ? description.trim() : null;
        String trimmedReview = review != null && !review.isBlank() ? review.trim() : null;

        return new ValidatedLocation(
                brandId,
                addressId,
                trimmedName,
                trimmedImageUrl,
                trimmedDescription,
                trimmedPhone,
                trimmedWebsiteUrl,
                minPrice,
                maxPrice,
                validatedCurrencyCode,
                trimmedReview
        );
    }

    private String trimIfPresent(String value, int maxLength, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new InvalidLocationException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return trimmed;
    }

    private LocationView toView(Location location) {
        return new LocationView(
                location.getId(),
                location.getBrandId(),
                location.getAddressId(),
                location.getName(),
                location.getImageUrl(),
                location.getDescription(),
                location.getPhone(),
                location.getWebsiteUrl(),
                location.isBusinessHoursKnown(),
                location.getMinPrice(),
                location.getMaxPrice(),
                location.getCurrencyCode(),
                location.getReview()
        );
    }

    private record ValidatedLocation(
            Long brandId,
            Long addressId,
            String name,
            String imageUrl,
            String description,
            String phone,
            String websiteUrl,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String currencyCode,
            String review
    ) {
    }
}
