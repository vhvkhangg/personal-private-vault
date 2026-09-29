package com.vhvkhangg.personalprivatevault.account.internal.domain;

import com.vhvkhangg.personalprivatevault.account.enums.FollowStatus;
import com.vhvkhangg.personalprivatevault.account.enums.FollowerStatus;
import com.vhvkhangg.personalprivatevault.account.enums.RelationshipSource;
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
 * Entity mapping the {@code external_account_relationships} table.
 */
@Entity
@Table(name = "external_account_relationships")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalAccountRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "owner_account_id", nullable = false)
    private Long ownerAccountId;

    @Column(name = "target_account_id", nullable = false)
    private Long targetAccountId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "follower_status")
    private FollowerStatus followerStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "source", nullable = false)
    private RelationshipSource source;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "follow_status", nullable = false)
    private FollowStatus followStatus;

    @Column(name = "has_liked_post", nullable = false)
    private boolean hasLikedPost;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ExternalAccountRelationship(
            Long ownerAccountId,
            Long targetAccountId,
            FollowerStatus followerStatus,
            RelationshipSource source,
            FollowStatus followStatus,
            boolean hasLikedPost,
            String note,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.ownerAccountId = Objects.requireNonNull(ownerAccountId, "ownerAccountId must not be null");
        this.targetAccountId = Objects.requireNonNull(targetAccountId, "targetAccountId must not be null");
        this.followerStatus = followerStatus;
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.followStatus = Objects.requireNonNull(followStatus, "followStatus must not be null");
        this.hasLikedPost = hasLikedPost;
        this.note = note;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public void update(
            FollowerStatus followerStatus,
            RelationshipSource source,
            FollowStatus followStatus,
            boolean hasLikedPost,
            String note,
            Instant updatedAt
    ) {
        this.followerStatus = followerStatus;
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.followStatus = Objects.requireNonNull(followStatus, "followStatus must not be null");
        this.hasLikedPost = hasLikedPost;
        this.note = note;
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalAccountRelationship other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
