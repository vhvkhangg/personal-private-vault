package com.vhvkhangg.personalprivatevault.location.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

/**
 * Address entity for physical locations.
 */
@Entity
@Table(name = "addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "address_type", length = 100)
    private String addressType;

    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Column(name = "administrative_area", length = 255)
    private String administrativeArea;

    @Column(name = "locality", length = 255)
    private String locality;

    @Column(name = "sublocality", length = 255)
    private String sublocality;

    @Column(name = "street_address", length = 500)
    private String streetAddress;

    @Column(name = "postal_code", length = 32)
    private String postalCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Address(
            String label,
            String addressType,
            String countryCode,
            String administrativeArea,
            String locality,
            String sublocality,
            String streetAddress,
            String postalCode
    ) {
        this.label = label;
        this.addressType = addressType;
        this.countryCode = Objects.requireNonNull(countryCode, "countryCode must not be null");
        this.administrativeArea = administrativeArea;
        this.locality = locality;
        this.sublocality = sublocality;
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
    }

    public void update(
            String label,
            String addressType,
            String countryCode,
            String administrativeArea,
            String locality,
            String sublocality,
            String streetAddress,
            String postalCode
    ) {
        this.label = label;
        this.addressType = addressType;
        this.countryCode = Objects.requireNonNull(countryCode, "countryCode must not be null");
        this.administrativeArea = administrativeArea;
        this.locality = locality;
        this.sublocality = sublocality;
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
