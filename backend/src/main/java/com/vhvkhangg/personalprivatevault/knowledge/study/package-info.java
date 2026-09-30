/**
 * Nested knowledge module for courses, books, GitHub repositories, websites, and YouTube channels.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code study_items}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>study-item metadata and URLs</li>
 *   <li>learning status/progress percentage/free-form progress text</li>
 *   <li>price/review metadata</li>
 *   <li>person/group authorship and optional YouTube-channel account reference</li>
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
        "people::person",
        "people::group",
        "people::view",
        "reference::catalog",
        "reference::view",
        "account::account",
        "account::enums",
        "account::view"
})
package com.vhvkhangg.personalprivatevault.knowledge.study;
