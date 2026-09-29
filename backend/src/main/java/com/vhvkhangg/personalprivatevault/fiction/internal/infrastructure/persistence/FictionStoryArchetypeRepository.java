package com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.fiction.internal.domain.FictionStoryArchetypeAssignment;
import com.vhvkhangg.personalprivatevault.fiction.internal.domain.FictionStoryArchetypeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA repository for {@link FictionStoryArchetypeAssignment} join entities.
 */
public interface FictionStoryArchetypeRepository extends JpaRepository<FictionStoryArchetypeAssignment, FictionStoryArchetypeId> {

    List<FictionStoryArchetypeAssignment> findByIdFictionId(Long fictionId);

    @Query("SELECT a.id.storyArchetypeId FROM FictionStoryArchetypeAssignment a WHERE a.id.fictionId = :fictionId")
    Set<Long> findStoryArchetypeIdsByFictionId(@Param("fictionId") Long fictionId);

    boolean existsByIdFictionIdAndIdStoryArchetypeId(Long fictionId, Long storyArchetypeId);

    @Modifying(flushAutomatically = true)
    @Query(
            value = "INSERT INTO fiction_story_archetypes (fiction_id, story_archetype_id) VALUES (:fictionId, :storyArchetypeId) ON CONFLICT (fiction_id, story_archetype_id) DO NOTHING",
            nativeQuery = true
    )
    void insertIfAbsent(@Param("fictionId") Long fictionId, @Param("storyArchetypeId") Long storyArchetypeId);
}
