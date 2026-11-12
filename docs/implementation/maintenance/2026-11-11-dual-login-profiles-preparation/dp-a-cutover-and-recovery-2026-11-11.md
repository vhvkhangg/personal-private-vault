# Dual Login Profiles (BF26-DP-A) Cutover & Recovery Runbook

- **Status:** Proposed / Implemented Awaiting Codex Review (2026-11-11)
- **Authority:** [BF26-DP-A Executable Handoff](executable-handoff-dp-a-2026-11-11.md), [D1–D6 Ratification](owner-ratification-d1-d6-2026-11-11.md), [ADR-0029](../../../adr/0029-fixed-profiles-and-persisted-session-authority.md)

## 1. Scope & Security Boundaries

BF26-DP-A introduces schema V29 (`ppv_public_login_credential`, `ppv_login_sessions`, and additions to `refresh_tokens`).

### Portability Snapshots vs. Restricted Full Database Backup
1. **Portability Snapshots (ZIP/JSONL):**
   - Both `ppv_public_login_credential` and `ppv_login_sessions` are strictly excluded from snapshot exports (`PortabilitySnapshotAdapter.EXCLUDED_SECURITY_TABLES`).
   - Exported ZIP contains `manifest.json` reporting `highestFlywayVersion: 29`.
   - No password hashes, credential epochs, session identifiers, or refresh tokens ever leak into user portability archives.
   - PUBLIC profile caller is denied export access with 403 `ACCESS_DENIED` before any byte emission or metadata inspection.
2. **Restricted Full PostgreSQL Database Backup (DR):**
   - Full PostgreSQL backups (`pg_dump`) retain all security and auth relations including `ppv_public_login_credential` and `ppv_login_sessions`.
   - Full backups contain cryptographic and session credentials and must be treated as restricted secret material.

## 2. Planned Cutover Order

To transition a production instance to V29:
1. **Maintenance Window Announcement:** Schedule a compatible-binary maintenance window. Stop active frontend or external client requests.
2. **Restricted Full Database Backup:** Create an isolated `pg_dump` of the PostgreSQL database before migration.
3. **Shutdown Old Application Services:** Stop serving processes running the pre-V29 binary to prevent mixed old/new fleet execution against the evolving schema.
4. **Apply Flyway Migration V29:**
   - Run Flyway migrate to apply `V29__authentication_profiles_and_sessions.sql`.
   - The migration:
     - Creates `ppv_public_login_credential` and inserts an unconfigured row (`credential_epoch = 0`, `password_hash = NULL`) for `owner_id = 1`.
     - Creates `ppv_login_sessions`.
     - Extends `refresh_tokens` and revokes all historical legacy refresh tokens (`revoked_at = COALESCE(revoked_at, now())`).
     - Adds circular deferred foreign key constraints and indexes.
5. **Deploy & Start V29 Compatible Binary:** Launch the reviewed Spring Boot application.
6. **Post-Deployment Verification:**
   - Verify unprofiled legacy JWTs and legacy refresh tokens fail admission (401 `AUTHENTICATION_REQUIRED`).
   - Verify master vault password login (`POST /api/v1/auth/login`) succeeds and issues a valid PRIVATE token.
   - Verify PUBLIC profile remains unconfigured until explicitly set by the Owner via `POST /api/v1/auth/profiles/public-credential`.

## 3. Disaster Recovery & Invalidation Runbook

When restoring a restricted full PostgreSQL backup to a new host or disaster-recovery environment:
1. **Restore Backup in Isolated Network:** Restore database dump to an isolated target instance before exposing to public or client traffic.
2. **Invalidate Session and Refresh Authority:** Execute an administrative transactional invalidation:
   ```sql
   BEGIN;
   -- Revoke all active login sessions
   UPDATE ppv_login_sessions SET revoked_at = now() WHERE revoked_at IS NULL;
   -- Revoke all refresh tokens
   UPDATE refresh_tokens SET revoked_at = now() WHERE revoked_at IS NULL;
   -- Bump credential epoch to invalidate any ever-PUBLIC tokens if public credential was configured
   UPDATE ppv_public_login_credential SET credential_epoch = credential_epoch + 1 WHERE owner_id = 1 AND password_hash IS NOT NULL;
   COMMIT;
   ```
3. **Rotate JWT Signing Secret:** Update `ppv.security.jwt.secret` configuration to ensure all previously signed tokens are cryptographically rejected.
4. **Verify Clean Access:**
   - Old tokens are rejected with 401.
   - Fresh master login succeeds with username/email and private password.
   - Owner can reconfigure or reset the public credential if desired.

## 4. Rollback and Recovery Policy

- Schema V29 contains circular deferred foreign keys, session-profile invariants, and irreversible token revocation.
- There is **no automated down-migration** to V28.
- **Transactional Cutover Failure (Pre-Commit):** If Flyway migration fails before V29 transaction commits, PostgreSQL automatically rolls back the migration DDL/DML, leaving the V28 schema intact.
- **Post-Commit Failure / Forward Repair:** Once V29 commits, restarting or rolling back to an older pre-cutover application binary is strictly forbidden because older binaries lack profile-aware session enforcement and would permit unprofiled or stale authority.
- In the event of post-cutover operational anomalies, recovery MUST proceed via forward repair or restoring a verified backup into an isolated disaster recovery environment running the forward-compatible binary, executing the step-2 invalidation script and rotating the JWT signing secret before restoring network ingress.
