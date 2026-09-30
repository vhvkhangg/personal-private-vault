/**
 * Nested collection module for saved music tracks and person credits.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code music_tracks}</li>
 *   <li>{@code music_track_people}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>track title/version/platform/URL</li>
 *   <li>multiple singer/artist credits</li>
 * </ul>
 *
 * <h2>Boundary</h2>
 * <p>This is a nested Spring Modulith application module under the {@code collection} parent.
 * External top-level modules should normally use the parent module facade rather than importing
 * nested internals or repositories directly.</p>
 *
 * <p>Implementation details belong under {@code internal}. Keep this package focused on the stable
 * nested-module contract and package-level documentation.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
        "vault::entry",
        "vault::enums",
        "vault::view",
        "people::person",
        "people::view",
        "reference::catalog",
        "reference::view"
})
package com.vhvkhangg.personalprivatevault.collection.music;
