/**
 * Public facade for the nested study, information, vocabulary, and note modules.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code nested-module tables only}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>stable public facade for knowledge features</li>
 *   <li>cross-nested-module coordination when required</li>
 *   <li>public search/import contracts for knowledge content</li>
 * </ul>
 *
 * <h2>Module boundary</h2>
 * <p>Allowed top-level dependencies: {@code vault, people, reference, account}.</p>
 * <p>Public contracts belong in this package. Entities, repositories, application services,
 * infrastructure adapters, and web implementation details belong under {@code internal} unless an
 * explicitly named interface is required. Other modules must not import this module's
 * {@code internal} packages.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>External top-level modules should normally depend on this facade rather than nested-module internals.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
        "knowledge.study::study",
        "knowledge.study::enums",
        "knowledge.study::view",
        "knowledge.information::information",
        "knowledge.information::enums",
        "knowledge.information::view",
        "knowledge.vocabulary::vocabulary",
        "knowledge.vocabulary::enums",
        "knowledge.vocabulary::view",
        "knowledge.note::note",
        "knowledge.note::view"
})
package com.vhvkhangg.personalprivatevault.knowledge;
