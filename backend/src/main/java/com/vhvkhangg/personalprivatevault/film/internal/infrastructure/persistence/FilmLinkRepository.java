package com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link FilmLink} entities.
 */
public interface FilmLinkRepository extends JpaRepository<FilmLink, Long> {

    List<FilmLink> findByFilmIdOrderByCreatedAtAscIdAsc(Long filmId);

    Optional<FilmLink> findByIdAndFilmId(Long id, Long filmId);
}
