/**
 * Owns the film library, film classifications, and person credits.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code films}</li>
 *   <li>{@code film_genres}</li>
 *   <li>{@code film_genre_assignments}</li>
 *   <li>{@code film_story_archetypes}</li>
 *   <li>{@code film_world_settings}</li>
 *   <li>{@code film_links}</li>
 *   <li>{@code film_credits}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>film CRUD</li>
 *   <li>movie/series + animation/live-action metadata</li>
 *   <li>genres/archetypes/world settings</li>
 *   <li>watch/progress state</li>
 *   <li>film links</li>
 *   <li>film credits/characters</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed dependencies: narrowed to exact named interfaces.</p>
 * <p>Public contracts belong under {@code film}, {@code genre}, {@code link}, {@code credit}, {@code enums},
 * and {@code view}. Entities, repositories, application services, and infrastructure adapters remain under
 * {@code internal}. Other modules must not import this module's {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Film credits may be favorited but are intentionally not rated or tagged.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
                "vault :: entry",
                "vault :: enums",
                "vault :: view",
                "people :: person",
                "people :: view",
                "reference :: catalog",
                "reference :: view"
        }
)
package com.vhvkhangg.personalprivatevault.film;
