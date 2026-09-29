package com.vhvkhangg.personalprivatevault.fiction.view;

import java.time.Instant;

/**
 * Public immutable view of a fiction link.
 *
 * @param id unique identifier of the link
 * @param fictionId ID of the parent fiction
 * @param languageCode optional ISO language code
 * @param linkType type or category of the link (e.g. ORIGINAL, TRANSLATION, CONVERT)
 * @param label optional display label
 * @param url destination URL
 * @param isPrimary indicates if this link is marked primary
 * @param createdAt timestamp when the link was created
 */
public record FictionLinkView(
        Long id,
        Long fictionId,
        String languageCode,
        String linkType,
        String label,
        String url,
        boolean isPrimary,
        Instant createdAt
) {
}
