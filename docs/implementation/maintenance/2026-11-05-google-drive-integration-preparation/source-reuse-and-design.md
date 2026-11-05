# GDI26 — Source-backed reuse and minimal implementation boundary

**Repo HEAD inspected:** `f4144e89ebc27b59225fd21f59dcb3dc6f1ebbf4`. All proposed changes below are **NOT IMPLEMENTED**.

| Existing source / contract | Observed behavior | Selected GDI26 treatment |
| --- | --- | --- |
| `authentication/internal/infrastructure/security/SecurityConfiguration.java` | JWT bearer resource server, `STATELESS`, CSRF disabled; other paths authenticated | Reuse owner JWT for initiation/status/disconnect. **Add one exact GET callback public matcher**, with independent state/cookie gate; never install generic OAuth-as-PPV-login. |
| `authentication/internal/web/controller/AuthController.java` | PPV local login/refresh/revoke private PIN | Keep as is; Google credentials never enter PPV refresh-token records. |
| `account/internal/domain/ExternalAccount.java` and `ExternalAccountType.java` | Vault-based profiles with types `SOCIAL/GAME/YOUTUBE_CHANNEL/OTHER` | No OAuth secret or Drive connection in `external_accounts`; keep Account frozen. |
| `media/internal/application/storage/MediaStoragePort.java` and `MediaStorageConfiguration.java` | S3/disabled app-owned binary media provider | Preserve existing Media S3; Drive storage is independent. No new Media adapter in S1–S3 by inference. |
| `docs/architecture/module-dependency-matrix.md` | Frozen 19-module baseline plus accepted RAG delta | S1 proposes one additive `drive` leaf, zero existing business module edges; future handoff must explicitly update matrix after review. |
| `backend/pom.xml` | Boot 4.1.1, Java 25, Modulith 2.1.1; OAuth2 resource-server only | Reuse Boot-managed baseline; add only verified necessary OAuth code-client/dependency, not parallel frameworks/upgrades. |
| `docs/implementation/handoffs/ACTIVE.md` | NO_ACTIVE_HANDOFF after DTR26-S2B | Leave unchanged during preparation. |

## Chosen ownership — not an application-framework expansion

**New `com.vhvkhangg.personalprivatevault.drive` top-level Spring Modulith module** with minimal public status/command operations, private domain state and JPA repository, OAuth server-side adapter, token/key utility, and narrow Drive web endpoints. It may use shared root `ApiResponse`/security framework abstractions but may not import `authentication.internal`, `account.internal`, `media.internal` or their repositories. No existing module consumes Drive public contracts in S1. Propose no changes to frozen Schema v1; only an additive Drive-owned migration/table. Keep `module-dependency-matrix.md`, architecture description and package-tree **as approved implementation-adjacent updates in S1**, subject to Codex handoff. Preserve ADR-0016 root envelope/module-local HTTP adapters. Avoid `common/`, `connector-platform/`, additional agents/skills/hooks and cross-domain event machinery.

## One first deliverable — S1

- `POST /api/v1/drive/connections/{slot}/authorize` owner JWT: create one short-lived browser-bound pending OAuth attempt and return a Google consent URL via the standard `ApiResponse` **plus** protected cookie.
- `GET /api/v1/drive/oauth/callback` public narrowly matched endpoint: validate pending state+cookie+PKCE and exchange code, verify ID-token Google subject, persist guarded slot; return a generic no-token result. No PPV-login session is created.
- `GET /api/v1/drive/connections` JWT: bounded two-slot status without secrets, verified account subject not exposed unless needed; status/email display only.
- `DELETE /api/v1/drive/connections/{slot}` JWT: local credential invalidation/revocation independent of Google network result, remote revoke best effort and explicit failure reporting; never delete third-party files.
- Reconnect for an existing slot uses the same **authorize** route; may replace an existing credential only after a fresh Google grant with the **same `sub`**. Different-subject slot substitution needs an explicit disconnect first. Duplicate `sub` across slots is rejected.
- **No file list/upload/download, no folder creation, no browser UI and no Google Picker in S1**. No Drive background job or PPV Media S3 change.

## Later separately gated capabilities

**S2**: one app-created folder per connection; persist opaque root ID; scoped list/upload/download and pagination with all operations verifying correct Google `sub`/slot, `drive.file` visibility **and current folder ancestry**. Externally copied existing files are not implicitly authorized; no full Drive scanning. **S3**: UI Explorer showing S2 records, account switch, safe uploads/downloads, no Picker requirement for app-created objects. If Picker is separately desired to import already-existing files, seek explicit future UI/scope approval; no automatic extension now.

**Tests/review:** targeted Spring security callback negative cases, OIDC/PKCE Google fake service, real PostgreSQL two-slot/migration/race, encryption/key-loss, module/HTTP inventory regression. Compare actual handler signatures, timeout/test parameters and documented endpoints at Codex handoff. Fake Google evidence is not proof of real Cloud consent or deployment. No new frontend work during S1.

**Next step:** Owner → Codex | Owner publishes the preparation accepted in [re-review 2](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-2.md), then Codex `$codex-create-handoff` | connection-only GDI26-S1 | READY_FOR_HANDOFF; no runtime PASS or implementation authority from this design | remediation 0/3 unchanged; no takeover.
