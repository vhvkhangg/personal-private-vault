/**
 * Public facade for the nested music, shopping, and software modules.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code nested-module tables only}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>stable public facade for collection features</li>
 *   <li>cross-nested-module coordination when required</li>
 *   <li>public search contracts for collection content</li>
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
 *   <li>External top-level modules should normally depend on this facade rather than nested-module internals.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "vault", "people", "reference" })
package com.vhvkhangg.personalprivatevault.collection;
