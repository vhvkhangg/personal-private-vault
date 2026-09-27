/**
 * Owns structured personal/family profile information.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code personal_profiles}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>self/family profiles</li>
 *   <li>relationship/birthday/contact/occupation fields</li>
 *   <li>nationality/address references</li>
 *   <li>private Markdown notes</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code reference, location}.</p>
 * <p>Public contracts belong in this package. Entities, repositories, application services,
 * infrastructure adapters, and web implementation details belong under {@code internal} unless an
 * explicitly named interface is required. Other modules must not import this module's
 * {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Only one active profile may represent self; database-level partial uniqueness is implemented in Flyway.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "reference", "location" })
package com.vhvkhangg.personalprivatevault.personal;
