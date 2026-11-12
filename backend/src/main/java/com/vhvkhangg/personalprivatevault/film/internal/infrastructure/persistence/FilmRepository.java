package com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.film.internal.domain.Film;
import com.vhvkhangg.personalprivatevault.film.view.PublicFilmView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Film} entities.
 */
public interface FilmRepository extends JpaRepository<Film, Long> {

    @Query("""
        SELECT new com.vhvkhangg.personalprivatevault.film.view.PublicFilmView(
            f.id,
            f.title,
            f.originalTitle,
            f.nationalityCode,
            f.format,
            f.productionStyle,
            f.isNsfw
        )
        FROM Film f
        WHERE f.id = :filmId AND f.isNsfw = false
    """)
    Optional<PublicFilmView> findPublicProjectionById(@Param("filmId") Long filmId);
}
