package com.vhvkhangg.personalprivatevault.location.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Location Category entity.
 */
@Entity
@Table(name = "location_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 150, nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    public LocationCategory(String name, String description) {
        this.name = Objects.requireNonNull(name, "LocationCategory name must not be null");
        this.description = description;
    }

    public void update(String name, String description) {
        this.name = Objects.requireNonNull(name, "LocationCategory name must not be null");
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationCategory other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
