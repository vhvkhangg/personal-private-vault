/**
 * Owns shared vault identity and cross-content capabilities.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code vault_entries}</li>
 *   <li>{@code favorites}</li>
 *   <li>{@code ratings}</li>
 *   <li>{@code tags}</li>
 *   <li>{@code vault_entry_tags}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>common content identity</li>
 *   <li>favorites</li>
 *   <li>ratings</li>
 *   <li>global tags</li>
 *   <li>soft-delete/recycle-bin semantics</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code none}.</p>
 * <p>Public contracts belong in this package. Entities, repositories, application services,
 * infrastructure adapters, and web implementation details belong under {@code internal} unless an
 * explicitly named interface is required. Other modules must not import this module's
 * {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Global search is not owned here; search is an orchestration module to avoid dependency cycles.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {})
package com.vhvkhangg.personalprivatevault.vault;
