package com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.location.internal.domain.LocationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationCategoryRepository extends JpaRepository<LocationCategory, Long> {

    Optional<LocationCategory> findByNameIgnoreCase(String name);
}
