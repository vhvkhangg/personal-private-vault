package com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.reference.internal.domain.WorldSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorldSettingRepository extends JpaRepository<WorldSetting, Long> {
    @Query("SELECT w FROM WorldSetting w ORDER BY LOWER(w.name) ASC, w.id ASC")
    List<WorldSetting> findAllOrderByNameCaseInsensitive();
}
