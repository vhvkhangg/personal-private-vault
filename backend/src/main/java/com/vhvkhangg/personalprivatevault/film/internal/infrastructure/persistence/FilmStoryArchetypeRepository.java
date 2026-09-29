package com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmStoryArchetypeAssignment;
import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmStoryArchetypeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA repository for {@link FilmStoryArchetypeAssignment} join entities.
 */
public interface FilmStoryArchetypeRepository extends JpaRepository<FilmStoryArchetypeAssignment, FilmStoryArchetypeId> {

    List<FilmStoryArchetypeAssignment> findByIdFilmId(Long filmId);

    @Query("SELECT a.id.storyArchetypeId FROM FilmStoryArchetypeAssignment a WHERE a.id.filmId = :filmId")
    Set<Long> findStoryArchetypeIdsByFilmId(@Param("filmId") Long filmId);

    boolean existsByIdFilmIdAndIdStoryArchetypeId(Long filmId, Long storyArchetypeId);

    @Modifying(flushAutomatically = true)
    @Query(
            value = "INSERT INTO film_story_archetypes (film_id, story_archetype_id) VALUES (:filmId, :storyArchetypeId) ON CONFLICT (film_id, story_archetype_id) DO NOTHING",
            nativeQuery = true
    )
    void insertIfAbsent(@Param("filmId") Long filmId, @Param("storyArchetypeId") Long storyArchetypeId);
}
