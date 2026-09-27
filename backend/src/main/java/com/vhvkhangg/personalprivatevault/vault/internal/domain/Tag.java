package com.vhvkhangg.personalprivatevault.vault.internal.domain;

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
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Tag(String name, Instant createdAt) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tag name must not be null or blank");
        }
        this.name = name.trim();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("Tag name must not be blank after trim");
        }
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
