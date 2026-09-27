/**
 * Owns generic CSV/JSON/Markdown import orchestration and job state.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code import_jobs}</li>
 *   <li>{@code import_job_items}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>parse/preview/validate workflow</li>
 *   <li>duplicate detection</li>
 *   <li>user-selected update/skip decisions</li>
 *   <li>transactional import orchestration</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code vault, knowledge}.</p>
 * <p>Public contracts belong in this package. Entities, repositories, application services,
 * infrastructure adapters, and web implementation details belong under {@code internal} unless an
 * explicitly named interface is required. Other modules must not import this module's
 * {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Target-domain business validation remains in the owning knowledge APIs, not in generic import code.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "vault", "knowledge" })
package com.vhvkhangg.personalprivatevault.importdata;
