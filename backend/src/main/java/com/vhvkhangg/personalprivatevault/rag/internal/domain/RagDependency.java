package com.vhvkhangg.personalprivatevault.rag.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "dependencies", schema = "rag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_source_id", nullable = false)
    private Long parentSourceId;

    @Column(name = "child_source_id", nullable = false)
    private Long childSourceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RagDependency(Long parentSourceId, Long childSourceId, Instant now) {
        this.parentSourceId = Objects.requireNonNull(parentSourceId, "parentSourceId must not be null");
        this.childSourceId = Objects.requireNonNull(childSourceId, "childSourceId must not be null");
        this.createdAt = Objects.requireNonNull(now, "now must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RagDependency other)) return false;
        return Objects.equals(parentSourceId, other.parentSourceId) && Objects.equals(childSourceId, other.childSourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentSourceId, childSourceId);
    }
}
