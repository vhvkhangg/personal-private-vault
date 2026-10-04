/**
 * Owns external/social/game/YouTube accounts and relationship history.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code external_accounts}</li>
 *   <li>{@code external_account_relationships}</li>
 *   <li>{@code follower_snapshots}</li>
 *   <li>{@code follower_snapshot_entries}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>owned/tracked accounts</li>
 *   <li>social/game/YouTube channel metadata</li>
 *   <li>follow/follower states</li>
 *   <li>Douyin follow + liked-post markers</li>
 *   <li>historical follower snapshots</li>
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
 *   <li>Manual/import/API sources are tracked separately from relationship status.</li>
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
package com.vhvkhangg.personalprivatevault.account;
