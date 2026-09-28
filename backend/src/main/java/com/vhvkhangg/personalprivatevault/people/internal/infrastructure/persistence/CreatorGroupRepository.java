package com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.people.internal.domain.CreatorGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link CreatorGroup} entities.
 */
public interface CreatorGroupRepository extends JpaRepository<CreatorGroup, Long> {

    boolean existsByName(String name);

    Optional<CreatorGroup> findByName(String name);
}
