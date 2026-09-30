package com.vhvkhangg.personalprivatevault.collection.music.view;

import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicVersion;

/**
 * Immutable view of a music track.
 */
public record MusicView(
        Long id,
        String title,
        MusicVersion version,
        Long platformId,
        String url
) {}
