package com.vhvkhangg.personalprivatevault.knowledge.study.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.knowledge.study.internal.domain.StudyItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudyItemRepository extends JpaRepository<StudyItem, Long> {

    Optional<StudyItem> findByYoutubeChannelAccountId(Long youtubeChannelAccountId);

    boolean existsByYoutubeChannelAccountId(Long youtubeChannelAccountId);
}
