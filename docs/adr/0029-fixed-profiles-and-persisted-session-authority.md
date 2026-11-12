# ADR-0029: Fixed Profiles and Persisted Session Authority (BF26-DP-A)

- **Status:** Proposed / Implemented Awaiting Codex Review (2026-11-11)
- **Deciders:** Owner, Codex, Antigravity
- **Date:** 2026-11-11
- **Context:** [BF26-DP-A Executable Handoff](../implementation/maintenance/2026-11-11-dual-login-profiles-preparation/executable-handoff-dp-a-2026-11-11.md), [D1–D6 Ratification](../implementation/maintenance/2026-11-11-dual-login-profiles-preparation/owner-ratification-d1-d6-2026-11-11.md)

## Context

Personal Private Vault (PPV) is a strictly single-user vault governed by ADR-0006 (`app_users.id = 1`) and ADR-0008 (HS256 access JWTs, opaque rotated refresh tokens, and private PIN). In BF26-DP-A, the Owner authorized two fixed access profiles (`PRIVATE` and `PUBLIC`) for the single immutable Owner identity `app_users.id = 1`, with persisted session authority, safe profile switching, and strict request-boundary default denial:

1. **One Immutable Owner, Two Fixed Credentials:**
   - `PRIVATE`: Original username/email + password, full authorized access to business and data capabilities.
   - `PUBLIC`: Identifier-free profile login via password proof alone. PUBLIC is auth-only in DP-A; all business and data APIs are strictly default-denied (403 `ACCESS_DENIED`).
2. **Immediate Post-Switch Invalidation (D3 Admission Boundary):**
   - Pure stateless JWTs cannot be invalidated immediately upon profile switch or credential reset.
   - A single-SQL coherent snapshot admission join (`ppv_login_sessions` + `ppv_public_login_credential`) validates live authority on every incoming dispatch at the security filter boundary.
3. **Circular Deferred Session-Refresh Binding:**
   - Circular foreign keys bind `ppv_login_sessions.current_refresh_token_id` and `refresh_tokens.session_id`.
   - Deferred constraints evaluated at transaction commit guarantee atomic lineage progression without invalid intermediate states.
4. **Google Sign-In Adaptation (D4):**
   - Google Sign-In defaults remain OFF.
   - When enabled locally for testing, unmapped `LOGIN` start returns 503 `GOOGLE_SIGNIN_UNAVAILABLE`; `LINK` start and callback remain functional for Owner linking with fresh PRIVATE password proof; handoff redemption returns 503 before ticket consumption or token issuance.

## Decision

1. **Schema Additions (V29):**
   - `ppv_public_login_credential`: Singleton table (`owner_id = 1 PRIMARY KEY REFERENCES app_users(id)`), nullable `password_hash`, and monotonic `credential_epoch`.
   - `ppv_login_sessions`: Entity table tracking session UUID, `owner_id = 1`, `active_profile` (`PUBLIC|PRIVATE`), `generation` (monotonic positive integer), `public_credential_epoch`, and `current_refresh_token_id`.
   - `refresh_tokens`: Extended with `session_id`, `profile`, and `session_generation`. Legacy unmigrated tokens are revoked.
   - Circular foreign keys are declared `DEFERRABLE INITIALLY DEFERRED`.
2. **Canonical Lock Ordering:**
   - Lock `ppv_public_login_credential` first (`FOR SHARE` for read/transition operations, `FOR UPDATE` for setup/reset).
   - Lock `ppv_login_sessions` lineage row `FOR UPDATE`.
   - Lock presented/current `refresh_tokens` row `FOR UPDATE`.
   - Flushes and commits occur within a single database transaction.
3. **Request-Boundary Default Denial:**
   - `SessionAuthorityFilter` validates persisted lineage on every request dispatch.
   - PUBLIC tokens are granted `PROFILE_PUBLIC`; PRIVATE tokens are granted `PROFILE_PRIVATE`.
   - All non-auth business routes default to requiring `PROFILE_PRIVATE`. PUBLIC caller receives 403 `ACCESS_DENIED`.
   - Unknown routes with PUBLIC token receive 403 `ACCESS_DENIED` (default deny); with PRIVATE token receive standard 404 `NOT_FOUND`.
4. **Portability Snapshots & Backup Boundary:**
   - `ppv_public_login_credential` and `ppv_login_sessions` are added to `EXCLUDED_SECURITY_TABLES` in Portability snapshots.
   - Restricted full PostgreSQL database backups retain Auth session state; on disaster recovery restore to a new environment, session authority must be invalidated and signing keys rotated before exposure.

## Consequences

- **Positive:** Two distinct access profiles for the singleton Owner with safe password-based elevation and downgrade.
- **Positive:** Immediate revocation and profile switch enforcement via single-SQL persisted authority admission.
- **Positive:** Zero data leakage to PUBLIC caller; strict 403 default-deny across all business modules.
- **Negative:** Additional single-SQL index lookup per HTTP dispatch. Mitigated by primary-key indexes on `ppv_login_sessions` and `ppv_public_login_credential`.
- **Negative:** Circular deferred FKs require transactional management during token creation.

## Related ADRs

- [ADR-0006: Permanent Single-User Model](0006-permanent-single-user-model.md)
- [ADR-0008: JWT Access Tokens, Refresh Token Rotation, and Private PIN](0008-jwt-access-refresh-and-private-pin.md)
- [ADR-0016: Root HTTP Contract and Module-Local Web Adapters](0016-root-http-contract-module-local-adapters.md)
- [ADR-0017: Portability Read-Only Snapshot Module](0017-portability-readonly-snapshot-module.md)
- [ADR-0028: Google Sign-In and Singleton Owner Identity Linking](0028-google-sign-in-singleton-owner.md)
