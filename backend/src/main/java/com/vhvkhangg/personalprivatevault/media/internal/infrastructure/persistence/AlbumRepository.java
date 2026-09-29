package com.vhvkhangg.personalprivatevault.media.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.media.internal.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
}
