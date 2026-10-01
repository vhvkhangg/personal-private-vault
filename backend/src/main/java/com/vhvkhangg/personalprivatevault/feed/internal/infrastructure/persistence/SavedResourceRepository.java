package com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.feed.internal.domain.SavedResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link SavedResource}.
 */
public interface SavedResourceRepository extends JpaRepository<SavedResource, Long> {

    Optional<SavedResource> findByResourceUrlHash(String resourceUrlHash);

    @Query("SELECT r FROM SavedResource r ORDER BY r.savedAt DESC, r.id DESC")
    List<SavedResource> findRecent(Pageable pageable);
}
