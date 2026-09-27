/**
 * Owns application-wide settings for the single-user vault.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code app_settings}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>timezone</li>
 *   <li>default currency</li>
 *   <li>pagination defaults</li>
 *   <li>private-mode auto-lock timeout</li>
 *   <li>backup settings</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code reference}.</p>
 * <p>Public contracts belong in this package. Entities, repositories, application services,
 * infrastructure adapters, and web implementation details belong under {@code internal} unless an
 * explicitly named interface is required. Other modules must not import this module's
 * {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Frontend theme/UI preferences are deferred until the frontend phase.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "reference" })
package com.vhvkhangg.personalprivatevault.settings;
