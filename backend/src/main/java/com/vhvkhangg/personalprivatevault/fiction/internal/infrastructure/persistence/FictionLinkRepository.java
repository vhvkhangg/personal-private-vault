package com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.fiction.internal.domain.FictionLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link FictionLink} entities.
 */
public interface FictionLinkRepository extends JpaRepository<FictionLink, Long> {

    List<FictionLink> findByFictionIdOrderByCreatedAtAsc(Long fictionId);

    Optional<FictionLink> findByIdAndFictionId(Long id, Long fictionId);
}
