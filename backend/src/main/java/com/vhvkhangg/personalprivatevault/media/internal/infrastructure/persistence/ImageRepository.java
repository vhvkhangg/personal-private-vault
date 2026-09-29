package com.vhvkhangg.personalprivatevault.media.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.media.internal.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByObjectKey(String objectKey);

    Optional<Image> findByChecksumSha256(String checksumSha256);

    long countByAlbumId(Long albumId);

    @Query(value = "SELECT * FROM images WHERE album_id = :albumId ORDER BY id ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Image> findByAlbumIdPaged(@Param("albumId") Long albumId, @Param("limit") int limit, @Param("offset") int offset);
}
