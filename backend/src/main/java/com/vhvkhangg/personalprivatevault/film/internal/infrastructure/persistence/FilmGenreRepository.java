package com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link FilmGenre} entities.
 */
public interface FilmGenreRepository extends JpaRepository<FilmGenre, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<FilmGenre> findByNameIgnoreCase(String name);
}
