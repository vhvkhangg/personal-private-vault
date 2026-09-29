package com.vhvkhangg.personalprivatevault.account.internal.domain;

import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountOwnership;
import com.vhvkhangg.personalprivatevault.account.enums.ExternalAccountType;
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

import java.util.Objects;

/**
 * External account entity sharing identity with {@code vault_entries.id} for type {@code EXTERNAL_ACCOUNT}.
 */
@Entity
@Table(name = "external_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalAccount implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "platform_id", nullable = false)
    private Long platformId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "ownership", nullable = false)
    private ExternalAccountOwnership ownership;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "account_type", nullable = false)
    private ExternalAccountType accountType;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Column(name = "display_name", length = 500)
    private String displayName;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Column(name = "banner_url", length = 2048)
    private String bannerUrl;

    @Column(name = "profile_description", columnDefinition = "text")
    private String profileDescription;

    @Column(name = "owner_name", length = 500)
    private String ownerName;

    @Column(name = "url", length = 2048)
    private String url;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Transient
    private boolean isNew = false;

    public ExternalAccount(
            Long id,
            Long platformId,
            ExternalAccountOwnership ownership,
            ExternalAccountType accountType,
            String username,
            String externalId,
            String displayName,
            String avatarUrl,
            String bannerUrl,
            String profileDescription,
            String ownerName,
            String url,
            String notes,
            boolean isNew
    ) {
        this.id = Objects.requireNonNull(id, "ExternalAccount id must not be null");
        this.platformId = Objects.requireNonNull(platformId, "platformId must not be null");
        this.ownership = Objects.requireNonNull(ownership, "ownership must not be null");
        this.accountType = Objects.requireNonNull(accountType, "accountType must not be null");
        this.username = username;
        this.externalId = externalId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.bannerUrl = bannerUrl;
        this.profileDescription = profileDescription;
        this.ownerName = ownerName;
        this.url = url;
        this.notes = notes;
        this.isNew = isNew;
    }

    public void update(
            Long platformId,
            ExternalAccountOwnership ownership,
            ExternalAccountType accountType,
            String username,
            String externalId,
            String displayName,
            String avatarUrl,
            String bannerUrl,
            String profileDescription,
            String ownerName,
            String url,
            String notes
    ) {
        this.platformId = Objects.requireNonNull(platformId, "platformId must not be null");
        this.ownership = Objects.requireNonNull(ownership, "ownership must not be null");
        this.accountType = Objects.requireNonNull(accountType, "accountType must not be null");
        this.username = username;
        this.externalId = externalId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.bannerUrl = bannerUrl;
        this.profileDescription = profileDescription;
        this.ownerName = ownerName;
        this.url = url;
        this.notes = notes;
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
        if (!(o instanceof ExternalAccount other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
