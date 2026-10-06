/**
 * Root package of the Personal Private Vault backend.
 *
 * <p>The application is a Spring Boot modular monolith structured with Spring Modulith. Direct
 * sub-packages are top-level business application modules. Per ADR-0016, this root package contains
 * only bootstrap-level types and the narrow, explicitly bounded shared REST/HTTP wire contract:
 * <ul>
 *   <li>{@link com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication}</li>
 *   <li>{@link com.vhvkhangg.personalprivatevault.ApiResponse}</li>
 *   <li>{@link com.vhvkhangg.personalprivatevault.ApiError}</li>
 *   <li>{@link com.vhvkhangg.personalprivatevault.ApiFieldError}</li>
 *   <li>{@link com.vhvkhangg.personalprivatevault.ApiMeta}</li>
 *   <li>{@link com.vhvkhangg.personalprivatevault.ApiPageMeta}</li>
 *   <li>{@link com.vhvkhangg.personalprivatevault.ApiResponses}</li>
 *   <li>{@link com.vhvkhangg.personalprivatevault.ApiExceptionHandler}</li>
 *   <li>{@link com.vhvkhangg.personalprivatevault.OpenApiConfiguration}</li>
 * </ul>
 *
 * <p>Architecture source of truth:
 * <ul>
 *   <li>{@code docs/architecture/module-boundaries.md}</li>
 *   <li>{@code docs/architecture/module-dependency-matrix.md}</li>
 *   <li>{@code docs/architecture/api-architecture.md}</li>
 *   <li>{@code docs/adr/0016-root-http-contract-module-local-adapters.md}</li>
 *   <li>{@code docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml}</li>
 * </ul>
 */
package com.vhvkhangg.personalprivatevault;
