package com.vhvkhangg.personalprivatevault.collection.music.music;

import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicVersion;

/**
 * Command to create a new music track.
 */
public record CreateMusicCommand(
        String title,
        MusicVersion version,
        Long platformId,
        String url
) {}
