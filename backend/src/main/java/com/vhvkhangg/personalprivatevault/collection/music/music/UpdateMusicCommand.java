package com.vhvkhangg.personalprivatevault.collection.music.music;

import com.vhvkhangg.personalprivatevault.collection.music.enums.MusicVersion;

/**
 * Command to update an existing music track with full scalar replacement semantics.
 */
public record UpdateMusicCommand(
        String title,
        MusicVersion version,
        Long platformId,
        String url
) {}
