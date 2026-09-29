package com.vhvkhangg.personalprivatevault.account.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entity mapping the {@code follower_snapshot_entries} table.
 */
@Entity
@Table(name = "follower_snapshot_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowerSnapshotEntry {

    @EmbeddedId
    private FollowerSnapshotEntryId id;

    @Column(name = "username_snapshot", length = 255)
    private String usernameSnapshot;

    @Column(name = "display_name_snapshot", length = 500)
    private String displayNameSnapshot;

    @Column(name = "external_id_snapshot", length = 255)
    private String externalIdSnapshot;

    @Column(name = "profile_url_snapshot", length = 2048)
    private String profileUrlSnapshot;

    public FollowerSnapshotEntry(
            FollowerSnapshotEntryId id,
            String usernameSnapshot,
            String displayNameSnapshot,
            String externalIdSnapshot,
            String profileUrlSnapshot
    ) {
        this.id = Objects.requireNonNull(id, "FollowerSnapshotEntryId must not be null");
        this.usernameSnapshot = usernameSnapshot;
        this.displayNameSnapshot = displayNameSnapshot;
        this.externalIdSnapshot = externalIdSnapshot;
        this.profileUrlSnapshot = profileUrlSnapshot;
    }

    public FollowerSnapshotEntry(
            Long snapshotId,
            Long targetAccountId,
            String usernameSnapshot,
            String displayNameSnapshot,
            String externalIdSnapshot,
            String profileUrlSnapshot
    ) {
        this(
                new FollowerSnapshotEntryId(snapshotId, targetAccountId),
                usernameSnapshot,
                displayNameSnapshot,
                externalIdSnapshot,
                profileUrlSnapshot
        );
    }

    public Long getSnapshotId() {
        return id != null ? id.getSnapshotId() : null;
    }

    public Long getTargetAccountId() {
        return id != null ? id.getTargetAccountId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FollowerSnapshotEntry that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
