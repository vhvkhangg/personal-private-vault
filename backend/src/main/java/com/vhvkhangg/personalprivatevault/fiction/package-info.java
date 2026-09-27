/**
 * Owns the fiction library and fiction-specific classification/linking.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code fictions}</li>
 *   <li>{@code fiction_genres}</li>
 *   <li>{@code fiction_story_archetypes}</li>
 *   <li>{@code fiction_world_settings}</li>
 *   <li>{@code fiction_links}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>fiction CRUD</li>
 *   <li>format/NSFW/genre metadata</li>
 *   <li>story-archetype and world-setting assignments</li>
 *   <li>reading/progress state</li>
 *   <li>source/translation/convert links and review text</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code vault, people, reference}.</p>
 * <p>Public contracts belong in this package. Entities, repositories, application services,
 * infrastructure adapters, and web implementation details belong under {@code internal} unless an
 * explicitly named interface is required. Other modules must not import this module's
 * {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>One fiction has one fiction genre but may have multiple story archetypes and world settings.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "vault", "people", "reference" })
package com.vhvkhangg.personalprivatevault.fiction;
