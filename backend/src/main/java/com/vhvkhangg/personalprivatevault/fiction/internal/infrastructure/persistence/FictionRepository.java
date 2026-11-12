package com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.fiction.internal.domain.Fiction;
import com.vhvkhangg.personalprivatevault.fiction.view.PublicFictionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Fiction} entities.
 */
public interface FictionRepository extends JpaRepository<Fiction, Long> {

    @Query("""
        SELECT new com.vhvkhangg.personalprivatevault.fiction.view.PublicFictionView(
            f.id,
            f.title,
            f.originalTitle,
            f.nationalityCode,
            f.format,
            f.isNsfw
        )
        FROM Fiction f
        WHERE f.id = :fictionId AND f.isNsfw = false
    """)
    Optional<PublicFictionView> findPublicProjectionById(@Param("fictionId") Long fictionId);
}
