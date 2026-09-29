package com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.fiction.internal.domain.FictionGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link FictionGenre} entities.
 */
public interface FictionGenreRepository extends JpaRepository<FictionGenre, Long> {

    Optional<FictionGenre> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
