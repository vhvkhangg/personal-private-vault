package com.vhvkhangg.personalprivatevault.collection.software.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.collection.software.internal.domain.SoftwareItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link SoftwareItem}.
 */
@Repository
public interface SoftwareItemRepository extends JpaRepository<SoftwareItem, Long> {
}
