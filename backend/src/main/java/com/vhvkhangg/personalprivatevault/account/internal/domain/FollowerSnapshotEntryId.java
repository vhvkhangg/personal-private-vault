package com.vhvkhangg.personalprivatevault.account.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@code follower_snapshot_entries} table.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FollowerSnapshotEntryId implements Serializable {

    @Column(name = "snapshot_id", nullable = false)
    private Long snapshotId;

    @Column(name = "target_account_id", nullable = false)
    private Long targetAccountId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FollowerSnapshotEntryId that)) return false;
        return Objects.equals(snapshotId, that.snapshotId) && Objects.equals(targetAccountId, that.targetAccountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshotId, targetAccountId);
    }
}
