/**
 * Owns stable lookup/reference data shared across business modules.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code countries}</li>
 *   <li>{@code languages}</li>
 *   <li>{@code currencies}</li>
 *   <li>{@code platforms}</li>
 *   <li>{@code story_archetypes}</li>
 *   <li>{@code world_settings}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>country/language/currency lookup</li>
 *   <li>external platform lookup</li>
 *   <li>shared narrative taxonomy lookup</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code none}.</p>
 * <p>The base package owns the module descriptor. Public contracts are grouped by semantic capability in
 * explicitly exposed {@code @NamedInterface} subpackages (for example {@code catalog}, {@code view},
 * or {@code enums}). Entities, repositories, application services,
 * infrastructure adapters, and web implementation details remain under {@code internal}. Other modules must
 * not import this module's {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>This is a foundation module and must not depend on feature modules.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {})
package com.vhvkhangg.personalprivatevault.reference;
