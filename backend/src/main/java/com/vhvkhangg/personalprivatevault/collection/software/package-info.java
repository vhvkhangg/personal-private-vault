/**
 * Nested collection module for applications and extensions.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code software_items}</li>
 *   <li>{@code software_item_platforms}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>application/extension metadata</li>
 *   <li>price/URL/review</li>
 *   <li>many supported platforms via shared platform reference data</li>
 * </ul>
 *
 * <h2>Boundary</h2>
 * <p>This is a nested Spring Modulith application module under the {@code collection} parent.
 * External top-level modules should normally use the parent module facade rather than importing
 * nested internals or repositories directly.</p>
 *
 * <p>Implementation details belong under {@code internal}. Keep this package focused on the stable
 * nested-module contract and package-level documentation.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
        "vault::entry",
        "vault::enums",
        "vault::view",
        "vault::search",
        "reference::catalog",
        "reference::view"
})
package com.vhvkhangg.personalprivatevault.collection.software;
