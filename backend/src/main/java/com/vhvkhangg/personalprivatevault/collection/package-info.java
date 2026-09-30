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
 * <p>Allowed dependencies: nested collection module named interfaces.</p>
 * <p>Public contracts belong in the {@code api} package. Implementations belong under
 * {@code internal}. External modules must not import this module's {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>External top-level modules depend on this facade rather than nested-module internals.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
        "collection.music::music",
        "collection.music::enums",
        "collection.music::view",
        "collection.shopping::shopping",
        "collection.shopping::enums",
        "collection.shopping::view",
        "collection.software::software",
        "collection.software::enums",
        "collection.software::view"
})
package com.vhvkhangg.personalprivatevault.collection;
