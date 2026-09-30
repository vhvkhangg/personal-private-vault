package com.vhvkhangg.personalprivatevault.collection.music.music;

import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicCreditRole;
import com.vhvkhangg.personalprivatevault.collection.music.view.MusicCreditView;
import com.vhvkhangg.personalprivatevault.collection.music.view.MusicView;

import java.util.List;
import java.util.Optional;

/**
 * Public capability interface for the {@code collection.music} nested module.
 */
public interface MusicOperations {

    MusicView create(CreateMusicCommand command);

    MusicView update(Long id, UpdateMusicCommand command);

    Optional<MusicView> findById(Long id);

    void addCredit(Long musicId, Long personId, MusicCreditRole role);

    List<MusicCreditView> findCredits(Long musicId, int limit);
}
