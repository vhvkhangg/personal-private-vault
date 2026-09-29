package com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.film.internal.domain.Film;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Film} entities.
 */
public interface FilmRepository extends JpaRepository<Film, Long> {
}
