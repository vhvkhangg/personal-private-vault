package com.vhvkhangg.personalprivatevault.film.view;

import java.time.Instant;

/**
 * Public immutable view of an external link associated with a film.
 *
 * @param id link ID
 * @param filmId parent film ID
 * @param languageCode optional ISO language code from the reference catalog
 * @param label optional descriptive label
 * @param url destination URL
 * @param isPrimary whether this link is flagged as primary
 * @param createdAt timestamp when the link was created
 */
public record FilmLinkView(
        Long id,
        Long filmId,
        String languageCode,
        String label,
        String url,
        boolean isPrimary,
        Instant createdAt
) {
}
