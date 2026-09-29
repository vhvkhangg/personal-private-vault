package com.vhvkhangg.personalprivatevault.film.link.command;

/**
 * Command to create a new link for a film.
 *
 * @param filmId required parent film ID
 * @param languageCode optional 2-character language code
 * @param label optional link label (up to 255 characters)
 * @param url required destination URL (up to 2048 characters)
 * @param isPrimary whether this link is flagged as primary (defaults to false if null)
 */
public record CreateFilmLinkCommand(
        Long filmId,
        String languageCode,
        String label,
        String url,
        Boolean isPrimary
) {
}
