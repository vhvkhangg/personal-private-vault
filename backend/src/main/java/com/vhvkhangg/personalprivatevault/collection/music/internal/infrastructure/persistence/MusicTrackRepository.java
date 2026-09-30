package com.vhvkhangg.personalprivatevault.collection.music.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.collection.music.internal.domain.MusicTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link MusicTrack}.
 */
@Repository
public interface MusicTrackRepository extends JpaRepository<MusicTrack, Long> {
}
