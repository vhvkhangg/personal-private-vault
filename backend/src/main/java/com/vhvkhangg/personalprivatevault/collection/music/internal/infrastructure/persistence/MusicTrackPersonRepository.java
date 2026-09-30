package com.vhvkhangg.personalprivatevault.collection.music.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.collection.music.internal.domain.MusicTrackPerson;
import com.vhvkhangg.personalprivatevault.collection.music.internal.domain.MusicTrackPersonId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link MusicTrackPerson}.
 */
@Repository
public interface MusicTrackPersonRepository extends JpaRepository<MusicTrackPerson, MusicTrackPersonId> {

    @Query("SELECT p FROM MusicTrackPerson p WHERE p.id.musicId = :musicId ORDER BY p.id.personId ASC, p.id.role ASC")
    List<MusicTrackPerson> findCreditsByMusicId(@Param("musicId") Long musicId, Pageable pageable);

    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT INTO music_track_people (music_id, person_id, role) " +
            "VALUES (:musicId, :personId, CAST(:role AS music_credit_role)) " +
            "ON CONFLICT (music_id, person_id, role) DO NOTHING",
            nativeQuery = true)
    void insertIfAbsent(@Param("musicId") Long musicId, @Param("personId") Long personId, @Param("role") String role);
}
