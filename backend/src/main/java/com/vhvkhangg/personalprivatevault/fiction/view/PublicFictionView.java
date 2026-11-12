package com.vhvkhangg.personalprivatevault.fiction.view;

import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;

/**
 * Public immutable view of an eligible fiction work (PROFILE_PUBLIC view).
 *
 * @param id fiction ID (identical to underlying vault entry ID)
 * @param title primary title
 * @param originalTitle optional original/native title
 * @param nationalityCode optional 2-character country code
 * @param format format of the work (NOVEL or COMIC)
 * @param isNsfw whether the work is marked NSFW (always false on success)
 */
public record PublicFictionView(
        Long id,
        String title,
        String originalTitle,
        String nationalityCode,
        FictionFormat format,
        boolean isNsfw
) {
}
