package com.vhvkhangg.personalprivatevault.collection.music.view;

import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicCreditRole;

/**
 * Immutable view of a person credit on a music track.
 */
public record MusicCreditView(
        Long musicId,
        Long personId,
        MusicCreditRole role
) {}
