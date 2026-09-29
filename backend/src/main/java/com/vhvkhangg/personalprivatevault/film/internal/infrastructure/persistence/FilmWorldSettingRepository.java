package com.vhvkhangg.personalprivatevault.film.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmWorldSettingAssignment;
import com.vhvkhangg.personalprivatevault.film.internal.domain.FilmWorldSettingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA repository for {@link FilmWorldSettingAssignment} join entities.
 */
public interface FilmWorldSettingRepository extends JpaRepository<FilmWorldSettingAssignment, FilmWorldSettingId> {

    List<FilmWorldSettingAssignment> findByIdFilmId(Long filmId);

    @Query("SELECT a.id.worldSettingId FROM FilmWorldSettingAssignment a WHERE a.id.filmId = :filmId")
    Set<Long> findWorldSettingIdsByFilmId(@Param("filmId") Long filmId);

    boolean existsByIdFilmIdAndIdWorldSettingId(Long filmId, Long worldSettingId);

    @Modifying(flushAutomatically = true)
    @Query(
            value = "INSERT INTO film_world_settings (film_id, world_setting_id) VALUES (:filmId, :worldSettingId) ON CONFLICT (film_id, world_setting_id) DO NOTHING",
            nativeQuery = true
    )
    void insertIfAbsent(@Param("filmId") Long filmId, @Param("worldSettingId") Long worldSettingId);
}
