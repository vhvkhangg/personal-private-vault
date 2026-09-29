package com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.account.internal.domain.FollowerSnapshotEntry;
import com.vhvkhangg.personalprivatevault.account.internal.domain.FollowerSnapshotEntryId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FollowerSnapshotEntryRepository extends JpaRepository<FollowerSnapshotEntry, FollowerSnapshotEntryId> {

    @Query("SELECT e FROM FollowerSnapshotEntry e WHERE e.id.snapshotId = :snapshotId ORDER BY e.id.targetAccountId ASC")
    List<FollowerSnapshotEntry> findBySnapshotId(@Param("snapshotId") Long snapshotId, Pageable pageable);

    @Query("SELECT count(e) FROM FollowerSnapshotEntry e WHERE e.id.snapshotId = :snapshotId")
    int countBySnapshotId(@Param("snapshotId") Long snapshotId);

    @Query("SELECT e.id.snapshotId, count(e) FROM FollowerSnapshotEntry e WHERE e.id.snapshotId IN :snapshotIds GROUP BY e.id.snapshotId")
    List<Object[]> countGroupedBySnapshotIds(@Param("snapshotIds") Collection<Long> snapshotIds);
}
