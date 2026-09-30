/**
 * Nested knowledge module for vocabulary and spaced-repetition learning.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code vocabulary_items}</li>
 *   <li>{@code vocabulary_reviews}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>word/meaning/example/pronunciation/IPA/POS metadata</li>
 *   <li>learning status</li>
 *   <li>SRS scheduling state</li>
 *   <li>review history</li>
 * </ul>
 *
 * <h2>Boundary</h2>
 * <p>This is a nested Spring Modulith application module under the {@code knowledge} parent.
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
        "reference::catalog",
        "reference::view"
})
package com.vhvkhangg.personalprivatevault.knowledge.vocabulary;
