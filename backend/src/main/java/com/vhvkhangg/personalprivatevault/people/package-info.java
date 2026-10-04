/**
 * Owns people/creator records shared by fiction, film, music, and study.
 *
 * <h2>Owned persistence</h2>
 * <ul>
 *   <li>{@code persons}</li>
 *   <li>{@code person_roles}</li>
 *   <li>{@code creator_groups}</li>
 *   <li>{@code creator_group_members}</li>
 * </ul>
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>person profiles</li>
 *   <li>actor/singer/director/author/artist roles</li>
 *   <li>creator groups and memberships</li>
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
 *   <li>Callers use this module public API; they do not access person repositories/entities directly.</li>
 * </ul>
 *
 * <p>Canonical architecture references: {@code docs/architecture/module-boundaries.md} and
 * {@code docs/architecture/module-dependency-matrix.md}.</p>
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
                "vault :: entry",
                "vault :: enums",
                "vault :: view",
                "vault :: search",
                "reference :: catalog",
                "reference :: view"
        }
)
package com.vhvkhangg.personalprivatevault.people;
