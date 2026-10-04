/**
 * Nested knowledge module for Markdown/Obsidian-style notes.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code notes}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>Markdown content</li>
 *   <li>YAML frontmatter</li>
 *   <li>source/import metadata</li>
 *   <li>duplicate hash handling</li>
 *   <li>preservation of Obsidian-oriented syntax</li>
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
        "vault::search"
})
package com.vhvkhangg.personalprivatevault.knowledge.note;
