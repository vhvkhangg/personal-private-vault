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
 * Brand entity sharing identity with {@code vault_entries.id} for type {@code BRAND}.
 */
@Entity
@Table(name = "brands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "logo_url", length = 2048)
    private String logoUrl;

    @Column(name = "nationality_code", length = 2)
    private String nationalityCode;

    @Column(name = "description", columnDefinition = "text")
    private String description;

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

    public Brand(
            Long id,
            String name,
            String logoUrl,
            String nationalityCode,
            String description,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String currencyCode,
            String review,
            boolean isNew
    ) {
        this.id = Objects.requireNonNull(id, "Brand id must not be null");
        this.name = Objects.requireNonNull(name, "Brand name must not be null");
        this.logoUrl = logoUrl;
        this.nationalityCode = nationalityCode;
        this.description = description;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.currencyCode = currencyCode;
        this.review = review;
        this.isNew = isNew;
    }

    public void update(
            String name,
            String logoUrl,
            String nationalityCode,
            String description,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String currencyCode,
            String review
    ) {
        this.name = Objects.requireNonNull(name, "Brand name must not be null");
        this.logoUrl = logoUrl;
        this.nationalityCode = nationalityCode;
        this.description = description;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.currencyCode = currencyCode;
        this.review = review;
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
        if (!(o instanceof Brand other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
