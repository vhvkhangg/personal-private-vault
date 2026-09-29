package com.vhvkhangg.personalprivatevault.account.internal.domain;

import com.vhvkhangg.personalprivatevault.account.enums.FollowerSnapshotSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity mapping the {@code follower_snapshots} table.
 */
@Entity
@Table(name = "follower_snapshots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowerSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "owner_account_id", nullable = false)
    private Long ownerAccountId;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "source", nullable = false)
    private FollowerSnapshotSource source;

    @Column(name = "reported_total_count")
    private Integer reportedTotalCount;

    @Column(name = "imported_file_name", length = 500)
    private String importedFileName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public FollowerSnapshot(
            Long ownerAccountId,
            Instant capturedAt,
            FollowerSnapshotSource source,
            Integer reportedTotalCount,
            String importedFileName,
            Instant createdAt
    ) {
        this.ownerAccountId = Objects.requireNonNull(ownerAccountId, "ownerAccountId must not be null");
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.reportedTotalCount = reportedTotalCount;
        this.importedFileName = importedFileName;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FollowerSnapshot other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
