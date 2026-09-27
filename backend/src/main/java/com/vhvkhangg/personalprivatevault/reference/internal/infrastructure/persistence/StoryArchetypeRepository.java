package com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.reference.internal.domain.StoryArchetype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoryArchetypeRepository extends JpaRepository<StoryArchetype, Long> {
    @Query("SELECT s FROM StoryArchetype s ORDER BY LOWER(s.name) ASC, s.id ASC")
    List<StoryArchetype> findAllOrderByNameCaseInsensitive();
}
