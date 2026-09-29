package com.vhvkhangg.personalprivatevault.location.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Location entity sharing identity with {@code vault_entries.id} for type {@code LOCATION}.
 */
@Entity
@Table(name = "locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "address_id", nullable = false)
    private Long addressId;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "phone", length = 64)
    private String phone;

    @Column(name = "website_url", length = 2048)
    private String websiteUrl;

    @Column(name = "business_hours_known", nullable = false)
    private boolean businessHoursKnown = false;

    @Column(name = "min_price", precision = 19, scale = 4)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 19, scale = 4)
    private BigDecimal maxPrice;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "review", columnDefinition = "text")
    private String review;

    @Transient
    private boolean isNew = false;

    public Location(
            Long id,
            Long brandId,
            Long addressId,
            String name,
            String imageUrl,
            String description,
            String phone,
            String websiteUrl,
            boolean businessHoursKnown,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String currencyCode,
            String review,
            boolean isNew
    ) {
        this.id = Objects.requireNonNull(id, "Location id must not be null");
        this.brandId = brandId;
        this.addressId = Objects.requireNonNull(addressId, "Location addressId must not be null");
        this.name = Objects.requireNonNull(name, "Location name must not be null");
        this.imageUrl = imageUrl;
        this.description = description;
        this.phone = phone;
        this.websiteUrl = websiteUrl;
        this.businessHoursKnown = businessHoursKnown;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.currencyCode = currencyCode;
        this.review = review;
        this.isNew = isNew;
    }

    public void update(
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
        this.brandId = brandId;
        this.addressId = Objects.requireNonNull(addressId, "Location addressId must not be null");
        this.name = Objects.requireNonNull(name, "Location name must not be null");
        this.imageUrl = imageUrl;
        this.description = description;
        this.phone = phone;
        this.websiteUrl = websiteUrl;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.currencyCode = currencyCode;
        this.review = review;
    }

    public void setBusinessHoursKnown(boolean businessHoursKnown) {
        this.businessHoursKnown = businessHoursKnown;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
