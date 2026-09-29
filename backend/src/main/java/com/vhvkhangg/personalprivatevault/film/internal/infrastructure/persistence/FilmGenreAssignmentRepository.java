package com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmGenreAssignment;
import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmGenreAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA repository for {@link FilmGenreAssignment} join entities.
 */
public interface FilmGenreAssignmentRepository extends JpaRepository<FilmGenreAssignment, FilmGenreAssignmentId> {

    List<FilmGenreAssignment> findByIdFilmId(Long filmId);

    @Query("SELECT a.id.genreId FROM FilmGenreAssignment a WHERE a.id.filmId = :filmId")
    Set<Long> findGenreIdsByFilmId(@Param("filmId") Long filmId);

    boolean existsByIdFilmIdAndIdGenreId(Long filmId, Long genreId);

    @Modifying(flushAutomatically = true)
    @Query(
            value = "INSERT INTO film_genre_assignments (film_id, genre_id) VALUES (:filmId, :genreId) ON CONFLICT (film_id, genre_id) DO NOTHING",
            nativeQuery = true
    )
    void insertIfAbsent(@Param("filmId") Long filmId, @Param("genreId") Long genreId);
}
