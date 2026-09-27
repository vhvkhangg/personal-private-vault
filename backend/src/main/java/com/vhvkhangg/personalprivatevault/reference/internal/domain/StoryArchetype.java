package com.vhvkhangg.personalprivatevault.reference.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "story_archetypes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryArchetype {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 150, nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    public StoryArchetype(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("StoryArchetype name must not be null or blank");
        }
        this.name = name;
        this.description = description;
    }
}
