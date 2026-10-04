/**
 * Owns brands, physical places, addresses, categories, prices, and business hours.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code brands}</li>
 *   <li>{@code location_categories}</li>
 *   <li>{@code addresses}</li>
 *   <li>{@code locations}</li>
 *   <li>{@code location_category_assignments}</li>
 *   <li>{@code location_dining_service_styles}</li>
 *   <li>{@code location_business_hours}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>brand metadata</li>
 *   <li>physical locations</li>
 *   <li>international addresses</li>
 *   <li>multiple location categories</li>
 *   <li>F&B dining styles</li>
 *   <li>weekly/split business hours</li>
 *   <li>location/brand reviews and price ranges</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code vault, reference}.</p>
 * <p>Public contracts belong in this package. Entities, repositories, application services,
 * infrastructure adapters, and web implementation details belong under {@code internal} unless an
 * explicitly named interface is required. Other modules must not import this module's
 * {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Latitude/longitude are intentionally not part of v1.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
        "vault::entry",
        "vault::enums",
        "vault::view",
        "vault::search",
        "reference::catalog",
        "reference::view"
})
package com.vhvkhangg.personalprivatevault.location;
