package com.vhvkhangg.personalprivatevault.fiction.link.command;

/**
 * Command to update an existing fiction link within its parent fiction scope.
 *
 * @param id required link ID
 * @param fictionId required parent fiction ID
 * @param languageCode optional ISO language code (e.g. "en", "vi", "ja")
 * @param linkType required link category (up to 50 characters, e.g. "ORIGINAL", "TRANSLATION")
 * @param label optional descriptive label (up to 255 characters)
 * @param url required URL destination (up to 2048 characters)
 * @param isPrimary whether this link is designated as primary (defaults to false if null)
 */
public record UpdateFictionLinkCommand(
        Long id,
        Long fictionId,
        String languageCode,
        String linkType,
        String label,
        String url,
        Boolean isPrimary
) {
}
