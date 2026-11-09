package com.vhvkhangg.personalprivatevault.authentication.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable linked Google subject binding for the singleton PPV Owner.
 */
@Entity
@Table(name = "ppv_owner_google_identities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnerGoogleIdentity implements Persistable<Short> {

    @Id
    @Column(name = "owner_id", nullable = false)
    private Short ownerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(name = "issuer", nullable = false, length = 128)
    private String issuer;

    @Column(name = "google_sub", nullable = false, length = 255)
    private String googleSub;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;

    @Transient
    private boolean isNew = true;

    public OwnerGoogleIdentity(AppUser owner, String issuer, String googleSub, Instant linkedAt) {
        this.owner = Objects.requireNonNull(owner, "owner must not be null");
        this.ownerId = owner.getId();
        this.issuer = Objects.requireNonNull(issuer, "issuer must not be null");
        this.googleSub = Objects.requireNonNull(googleSub, "googleSub must not be null");
        this.linkedAt = Objects.requireNonNull(linkedAt, "linkedAt must not be null");
        this.isNew = true;
    }

    @Override
    public Short getId() {
        return ownerId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}

