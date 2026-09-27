/**
 * Owns single-user authentication and credential lifecycle.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code app_users}</li>
 *   <li>{@code refresh_tokens}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>one-time account bootstrap</li>
 *   <li>email/username + password authentication</li>
 *   <li>six-digit private-mode PIN hashing/verification</li>
 *   <li>access-token issuance and refresh-token rotation/revocation</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code none}.</p>
 * <p>Public contracts belong in this package. Entities, repositories, application services,
 * infrastructure adapters, and web implementation details belong under {@code internal} unless an
 * explicitly named interface is required. Other modules must not import this module's
 * {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>No registration, account deletion, multi-user, 2FA, or passkey flow belongs to v1.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {})
package com.vhvkhangg.personalprivatevault.authentication;
