/**
 * Nested collection module for wishlist and purchased shopping items.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code shopping_items}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>product metadata</li>
 *   <li>price/currency/platform/URL</li>
 *   <li>wishlist vs purchased lifecycle</li>
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
@org.springframework.modulith.ApplicationModule
package com.vhvkhangg.personalprivatevault.collection.shopping;
