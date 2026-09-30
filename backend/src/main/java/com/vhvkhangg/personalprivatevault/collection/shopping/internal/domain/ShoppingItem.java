package com.vhvkhangg.personalprivatevault.collection.shopping.internal.domain;

import com.vhvkhangg.personalprivatevault.collection.shopping.enums.ShoppingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Shopping item entity sharing identity with {@code vault_entries.id} for type {@code SHOPPING}.
 */
@Entity
@Table(name = "shopping_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingItem implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "price_amount", precision = 19, scale = 4)
    private BigDecimal priceAmount;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "platform_id")
    private Long platformId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private ShoppingStatus status;

    @Column(name = "url", length = 2048)
    private String url;

    @Column(name = "purchased_at")
    private Instant purchasedAt;

    @Transient
    private boolean isNew = false;

    public ShoppingItem(
            Long id,
            String name,
            String avatarUrl,
            String description,
            BigDecimal priceAmount,
            String currencyCode,
            Long platformId,
            ShoppingStatus status,
            String url,
            Instant purchasedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.avatarUrl = avatarUrl;
        this.description = description;
        this.priceAmount = priceAmount;
        this.currencyCode = currencyCode;
        this.platformId = platformId;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.url = url;
        this.purchasedAt = purchasedAt;
        this.isNew = true;
    }

    public void update(
            String name,
            String avatarUrl,
            String description,
            BigDecimal priceAmount,
            String currencyCode,
            Long platformId,
            ShoppingStatus status,
            String url,
            Instant purchasedAt
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.avatarUrl = avatarUrl;
        this.description = description;
        this.priceAmount = priceAmount;
        this.currencyCode = currencyCode;
        this.platformId = platformId;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.url = url;
        this.purchasedAt = purchasedAt;
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
        if (!(o instanceof ShoppingItem other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
