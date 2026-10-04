/**
 * Orchestrates cross-module global search without owning v1 tables.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code none}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>fan-out/search orchestration</li>
 *   <li>cross-module result normalization</li>
 *   <li>module/type/tag filters</li>
 *   <li>case-insensitive partial/fuzzy matching</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code vault, people, fiction, film, media, location, knowledge, collection, account, feed}.</p>
 * <p>Public contracts belong in this package. Entities, repositories, application services,
 * infrastructure adapters, and web implementation details belong under {@code internal} unless an
 * explicitly named interface is required. Other modules must not import this module's
 * {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Business modules must never depend back on search. PostgreSQL-first search can later move to a projection without changing this boundary.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
                "vault :: enums",
                "vault :: search",
                "people :: search",
                "fiction :: search",
                "film :: search",
                "media :: search",
                "location :: search",
                "knowledge :: search",
                "collection :: search",
                "account :: search",
                "feed :: search"
        }
)
package com.vhvkhangg.personalprivatevault.search;
