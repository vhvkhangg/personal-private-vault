package com.vhvkhangg.personalprivatevault.fiction.internal.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entity mapping the {@code fiction_story_archetypes} join table.
 */
@Entity
@Table(name = "fiction_story_archetypes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FictionStoryArchetypeAssignment {

    @EmbeddedId
    private FictionStoryArchetypeId id;

    public FictionStoryArchetypeAssignment(FictionStoryArchetypeId id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public FictionStoryArchetypeAssignment(Long fictionId, Long storyArchetypeId) {
        this(new FictionStoryArchetypeId(fictionId, storyArchetypeId));
    }
}
