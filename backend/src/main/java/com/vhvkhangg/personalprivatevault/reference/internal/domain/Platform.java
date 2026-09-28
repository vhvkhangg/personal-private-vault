package com.vhvkhangg.personalprivatevault.reference.internal.domain;

import com.vhvkhangg.personalprivatevault.reference.enums.PlatformKind;
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

@Entity
@Table(name = "platforms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "kind", nullable = false)
    private PlatformKind kind;

    @Column(name = "url", length = 2048)
    private String url;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Platform(String name, PlatformKind kind, String url, Instant createdAt) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Platform name must not be null or blank");
        }
        this.name = name;
        this.kind = Objects.requireNonNull(kind, "Platform kind must not be null");
        this.url = url;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
