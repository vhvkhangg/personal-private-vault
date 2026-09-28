package com.vhvkhangg.personalprivatevault.people.internal.domain;

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

/**
 * Creator group entity representing a band, studio, author group, or collective.
 */
@Entity
@Table(name = "creator_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreatorGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CreatorGroup(String name, String description, Instant createdAt) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = createdAt;
    }

    public void update(String name, String description, Instant updatedAt) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }
}
