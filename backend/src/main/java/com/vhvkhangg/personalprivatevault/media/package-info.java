/**
 * Owns album and image metadata while binary files live in object storage.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code albums}</li>
 *   <li>{@code images}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>album metadata</li>
 *   <li>image metadata</li>
 *   <li>object keys/checksums</li>
 *   <li>capture time and free-text location</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code vault}.</p>
 * <p>Public contracts belong in this package. Entities, repositories, application services,
 * infrastructure adapters, and web implementation details belong under {@code internal} unless an
 * explicitly named interface is required. Other modules must not import this module's
 * {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>One image belongs to at most one album; image_count is derived, not stored.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
        "vault::entry",
        "vault::enums",
        "vault::view",
        "vault::search"
})
package com.vhvkhangg.personalprivatevault.media;
