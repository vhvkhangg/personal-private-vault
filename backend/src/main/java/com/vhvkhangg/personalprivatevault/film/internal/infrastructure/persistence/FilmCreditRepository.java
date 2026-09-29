package com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmCredit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link FilmCredit} entities.
 */
public interface FilmCreditRepository extends JpaRepository<FilmCredit, Long> {

    List<FilmCredit> findByFilmIdOrderByIdAsc(Long filmId);
}
