/**
 * Nested knowledge module for structured knowledge snippets.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code information_items}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>Finance/Technology/Health/Other information entries</li>
 *   <li>description/Markdown content/examples</li>
 *   <li>source metadata</li>
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
@org.springframework.modulith.ApplicationModule
package com.vhvkhangg.personalprivatevault.knowledge.information;
