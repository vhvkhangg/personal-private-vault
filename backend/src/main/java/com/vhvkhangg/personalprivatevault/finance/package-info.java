/**
 * Owns wallets, ledger-based money movement, recurring rules, and subscriptions.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code wallets}</li>
 *   <li>{@code transaction_categories}</li>
 *   <li>{@code financial_transactions}</li>
 *   <li>{@code financial_transaction_entries}</li>
 *   <li>{@code recurring_transaction_rules}</li>
 *   <li>{@code recurring_rule_weekdays}</li>
 *   <li>{@code recurring_rule_entries}</li>
 *   <li>{@code subscriptions}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>multi-currency wallets</li>
 *   <li>income/expense/transfer ledger</li>
 *   <li>cross-currency transfer metadata</li>
 *   <li>recurring AUTO_POST/REQUIRE_CONFIRMATION rules</li>
 *   <li>subscriptions and renewal metadata</li>
 *   <li>expense statistics inputs</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code reference}.</p>
 * <p>Public contracts belong in this package. Entities, repositories, application services,
 * infrastructure adapters, and web implementation details belong under {@code internal} unless an
 * explicitly named interface is required. Other modules must not import this module's
 * {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>Current wallet balance is derived from opening balance + posted non-deleted ledger entries.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = { "reference" })
package com.vhvkhangg.personalprivatevault.finance;
