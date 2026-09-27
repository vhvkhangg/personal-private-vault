/**
 * Owns private diary/journal entries.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code diary_entries}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>multiple Markdown diary entries per day</li>
 *   <li>search/filter inputs</li>
 *   <li>recycle-bin lifecycle</li>
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
 *   <li>Journal remains independent from finance even if a future calendar view combines both.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {})
package com.vhvkhangg.personalprivatevault.journal;
