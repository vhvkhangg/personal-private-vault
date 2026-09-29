package com.vhvkhangg.personalprivatevault.fiction.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.fiction.internal.domain.FictionWorldSettingAssignment;
import com.vhvkhangg.personalprivatevault.fiction.internal.domain.FictionWorldSettingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA repository for {@link FictionWorldSettingAssignment} join entities.
 */
public interface FictionWorldSettingRepository extends JpaRepository<FictionWorldSettingAssignment, FictionWorldSettingId> {

    List<FictionWorldSettingAssignment> findByIdFictionId(Long fictionId);

    @Query("SELECT a.id.worldSettingId FROM FictionWorldSettingAssignment a WHERE a.id.fictionId = :fictionId")
    Set<Long> findWorldSettingIdsByFictionId(@Param("fictionId") Long fictionId);

    boolean existsByIdFictionIdAndIdWorldSettingId(Long fictionId, Long worldSettingId);

    @Modifying(flushAutomatically = true)
    @Query(
            value = "INSERT INTO fiction_world_settings (fiction_id, world_setting_id) VALUES (:fictionId, :worldSettingId) ON CONFLICT (fiction_id, world_setting_id) DO NOTHING",
            nativeQuery = true
    )
    void insertIfAbsent(@Param("fictionId") Long fictionId, @Param("worldSettingId") Long worldSettingId);
}
