package com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.reference.internal.domain.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlatformRepository extends JpaRepository<Platform, Long> {
    @Query("SELECT p FROM Platform p ORDER BY LOWER(p.name) ASC, p.id ASC")
    List<Platform> findAllOrderByNameCaseInsensitive();
}
