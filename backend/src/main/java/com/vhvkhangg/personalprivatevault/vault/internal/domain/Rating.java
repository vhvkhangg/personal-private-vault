package com.vhvkhangg.personalprivatevault.vault.internal.domain;

import com.vhvkhangg.personalprivatevault.vault.RatingGrade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "ratings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rating {

    @Id
    @Column(name = "vault_entry_id", nullable = false)
    private Long vaultEntryId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "grade", nullable = false)
    private RatingGrade grade;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Rating(Long vaultEntryId, RatingGrade grade, Instant createdAt) {
        this.vaultEntryId = Objects.requireNonNull(vaultEntryId, "vaultEntryId must not be null");
        this.grade = Objects.requireNonNull(grade, "grade must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = createdAt;
    }

    public void updateGrade(RatingGrade grade, Instant updatedAt) {
        this.grade = Objects.requireNonNull(grade, "grade must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }
}
