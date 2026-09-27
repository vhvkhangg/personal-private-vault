/**
 * Owns external feed ingestion state, feed items, and permanently saved web resources.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code feed_sources}</li>
 *   <li>{@code feed_items}</li>
 *   <li>{@code saved_resources}</li>
 *   <li>{@code saved_resource_conversions}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>GitHub Trending/Hacker News/Reddit/RSS/website feeds</li>
 *   <li>manual and scheduled refresh metadata</li>
 *   <li>saved resources/social-post links</li>
 *   <li>conversion provenance into knowledge content</li>
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
 *   <li>Saved social posts store metadata/link only, not a mirrored copy of source content.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "vault", "knowledge" })
package com.vhvkhangg.personalprivatevault.feed;
