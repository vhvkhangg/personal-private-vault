package com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.account.internal.domain.FollowerSnapshot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowerSnapshotRepository extends JpaRepository<FollowerSnapshot, Long> {

    @Query("SELECT s FROM FollowerSnapshot s WHERE s.ownerAccountId = :ownerAccountId ORDER BY s.capturedAt DESC, s.id DESC")
    List<FollowerSnapshot> findRecentByOwnerAccountId(
            @Param("ownerAccountId") Long ownerAccountId,
            Pageable pageable
    );
}
