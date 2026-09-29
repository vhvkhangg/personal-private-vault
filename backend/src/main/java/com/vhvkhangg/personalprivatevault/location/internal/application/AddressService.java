package com.vhvkhangg.personalprivatevault.location.internal.application;

import com.vhvkhangg.personalprivatevault.location.address.AddressNotFoundException;
import com.vhvkhangg.personalprivatevault.location.address.AddressOperations;
import com.vhvkhangg.personalprivatevault.location.address.CreateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.address.InvalidAddressException;
import com.vhvkhangg.personalprivatevault.location.address.UpdateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.internal.domain.Address;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.AddressRepository;
import com.vhvkhangg.personalprivatevault.location.view.AddressView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
public class AddressService implements AddressOperations {

    private final AddressRepository addressRepository;
    private final ReferenceCatalog referenceCatalog;

    @Autowired
    public AddressService(
            AddressRepository addressRepository,
            ReferenceCatalog referenceCatalog
    ) {
        this.addressRepository = Objects.requireNonNull(addressRepository, "addressRepository must not be null");
        this.referenceCatalog = Objects.requireNonNull(referenceCatalog, "referenceCatalog must not be null");
    }

    @Override
    @Transactional
    public AddressView create(CreateAddressCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        ValidatedAddress validated = validate(
                command.label(),
                command.addressType(),
                command.countryCode(),
                command.administrativeArea(),
                command.locality(),
                command.sublocality(),
                command.streetAddress(),
                command.postalCode()
        );

        Address address = new Address(
                validated.label(),
                validated.addressType(),
                validated.countryCode(),
                validated.administrativeArea(),
                validated.locality(),
                validated.sublocality(),
                validated.streetAddress(),
                validated.postalCode()
        );
        Address saved = addressRepository.save(address);
        return toView(saved);
    }

    @Override
    @Transactional
    public AddressView update(UpdateAddressCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        if (command.id() == null) {
            throw new InvalidAddressException("Address id must not be null");
        }

        Address address = addressRepository.findById(command.id())
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + command.id()));

        ValidatedAddress validated = validate(
                command.label(),
                command.addressType(),
                command.countryCode(),
                command.administrativeArea(),
                command.locality(),
                command.sublocality(),
                command.streetAddress(),
                command.postalCode()
        );

        address.update(
                validated.label(),
                validated.addressType(),
                validated.countryCode(),
                validated.administrativeArea(),
                validated.locality(),
                validated.sublocality(),
                validated.streetAddress(),
                validated.postalCode()
        );
        Address saved = addressRepository.save(address);
        return toView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressView findById(Long id) {
        if (id == null) {
            throw new InvalidAddressException("Address id must not be null");
        }
        return addressRepository.findById(id)
                .map(this::toView)
                .orElseThrow(() -> new AddressNotFoundException("Address not found with id: " + id));
    }

    private ValidatedAddress validate(
            String label,
            String addressType,
            String countryCode,
            String administrativeArea,
            String locality,
            String sublocality,
            String streetAddress,
            String postalCode
    ) {
        if (countryCode == null || countryCode.isBlank()) {
            throw new InvalidAddressException("Address countryCode must not be blank");
        }
        String upperCountry = countryCode.trim().toUpperCase(Locale.ROOT);
        if (referenceCatalog.country(upperCountry).isEmpty()) {
            throw new InvalidAddressException("Country code '" + countryCode + "' does not exist in reference catalog");
        }

        String trimmedLabel = trimIfPresent(label, 255, "Address label");
        String trimmedAddressType = trimIfPresent(addressType, 100, "Address type");
        String trimmedAdminArea = trimIfPresent(administrativeArea, 255, "Administrative area");
        String trimmedLocality = trimIfPresent(locality, 255, "Locality");
        String trimmedSublocality = trimIfPresent(sublocality, 255, "Sublocality");
        String trimmedStreetAddress = trimIfPresent(streetAddress, 500, "Street address");
        String trimmedPostalCode = trimIfPresent(postalCode, 32, "Postal code");

        return new ValidatedAddress(
                trimmedLabel,
                trimmedAddressType,
                upperCountry,
                trimmedAdminArea,
                trimmedLocality,
                trimmedSublocality,
                trimmedStreetAddress,
                trimmedPostalCode
        );
    }

    private String trimIfPresent(String value, int maxLength, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new InvalidAddressException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return trimmed;
    }

    private AddressView toView(Address address) {
        return new AddressView(
                address.getId(),
                address.getLabel(),
                address.getAddressType(),
                address.getCountryCode(),
                address.getAdministrativeArea(),
                address.getLocality(),
                address.getSublocality(),
                address.getStreetAddress(),
                address.getPostalCode(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }

    private record ValidatedAddress(
            String label,
            String addressType,
            String countryCode,
            String administrativeArea,
            String locality,
            String sublocality,
            String streetAddress,
            String postalCode
    ) {
    }
}
