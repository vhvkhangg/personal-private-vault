> **PH-03/06/07 correction (2026-11-08):** Search batches are provider pageSize ≤5 and 2 root+1 list+≤5 child proofs; **Spring-owned** OIDC transaction and immutable Owner link require separate H1-AUTH with prospective V28 (only after H1-B V27). See [repair](ph03-ph06-ph07-repair-2026-11-08.md). H1-F follows reviewed/published H1-AUTH and H1-B. No implementation handoff or ACTIVE change is authorized by this note.

# H1 bounded scope, authority and non-goals

## User-visible functionality — KEEP/REUSE/REQUIRES_REVIEW/DEFER

| Decision | H1 target | Rationale / ownership |
| --- | --- | --- |
| **OWNER D26 / NEW REVIEW** | **Google Sign-In for routine PPV website login**; Spring remains singleton PPV authority, BFF holds PPV JWT/opaque refresh server-side | Main currently supports username/password only; a **separately reviewed Authentication extension** with explicit Owner Google `sub` binding is required. No implicit authority from Drive slot 1/2. |
| KEEP | Navigation: primary **Cá nhân**, secondary **Lưu trữ → Google Drive** | D14/D21 UI grouping only; backend `drive` stays independent of `personal` |
| KEEP | Two Google connection slots: status, authorize/consent, reconnect, disconnect | Reuse S1 exact routes and browser-binding cookie; never display/store Google tokens |
| KEEP | `ensure` one PPV-owned root per verified Google subject | Reuse S2 ensure; **not** generic folder creation or arbitrary Drive browse |
| KEEP | List direct app-created binary children with bounded cursor, one account at a time | S2 returned verified files only; no nested folder traversal/native-doc export |
| KEEP | Upload one original file per intent up to 50 MiB, explicit outcome/recovery, bounded download | S2 idempotency/provenance and real streaming semantics retained |
| REUSE | Existing .agents frontend role/4 skills/3 rules, current Owner policy/ACTIVE, hook guard and bounded runner | H0 accepted; no new agent platform, installer, telemetry or framework |
| REQUIRES_REVIEW | Exact name search, type filter, modified-date From/To across all authorized files for the selected slot | User requires real functional results; S2 only lists `limit,cursor`, not server-side filters. New bounded root-scoped Drive API needed; no broad My Drive search |
| REQUIRES_REVIEW | Real account storage quota and safe previews | New narrowly scoped provider reads/proxies with authentication/authorization, no fabricated metrics, no provider URL/token exposure; verify `drive.file` permissions |
| OWNER_SELECTED_D25_A / REQUIRES_NEW_REVIEW | “Mới” → **create PPV-owned direct child folder** under the unique PPV root, list/open its files and allow original upload there | One-level maximum in bounded initial scope, requires separate app-provenance, lost-reply recovery, verified parent and reviewed backend API; not S2 ensure, not upload local directory |
| REUSE | Grid/card display and UI infinite scroll | UI automatically loads bounded cursor pages, no visible pager/“Hiển thị x tệp”; preview fallback when truly unavailable |
| DEFER | Phase 16 modules, generic dashboard, remote deployment/Google Cloud, sync, Picker, delete/share/rename/move/copy, Media S3 replacement, RAG ingestion | Existing D00–D22 bounds and frozen architecture |

## Smallest production topology proposal for later review

One `frontend/` Next.js App Router TypeScript application (self-hosted local Node process, BFF integrated), one existing Spring backend with **separately scoped Google authentication addition**, existing PostgreSQL; no Redis, new DB, generic adapter, auth microservice, message broker or separate frontend component monorepo without evidence. Spring remains sole login/Drive/refresh/revocation policy authority; Google verifies an identity but does not replace Spring PPV issuance or Drive authorization. The future precise Node/Next/React/shadcn/TypeScript/test dependency matrix, lockfile, package manager and runtime startup/process topology must be selected and verified **against supported official versions and security advisories** before any H1 implementation. No install now.

No feature flag may be enabled merely for UI preview. Use isolated local fake-provider test fixtures only after explicit test scope in an executable handoff; no production Cloud/test account access is inferred. S1 localhost callback is fixed; no production redirect or OAuth scope changes.

## Hard prerequisites for H1 implementation

`H1_IMPLEMENTATION_ALLOWED = H0_ACCEPTED_AND_OWNER_PUBLISHED AND H1_PREPARATION_INDEPENDENTLY_ACCEPTED_AND_OWNER_PUBLISHED AND H1_EXECUTABLE_HANDOFF_READY AND OWNER_DESIGN_APPROVED_FOR_EXACT_REVIEWED_VERSION`. The H1-B/H1-AUTH/H1-F three-slice split is a proposal and does **not** waive this predicate for either slice.

**The Owner approved v0.3 Explorer and later **delegated auxiliary state design to Codex** (D28), deferred mobile (D27) and accepted bounded H1-B/BFF v0.4 scope (D29). A new Google login identity/binding design still needs independent security review. Do not treat a desktop visual as approval of unseen screens or runtime/API behavior. Preparation acceptance alone is not a substitute. Mockups, design-direction approval D11–D22 and a passing snapshot are not Owner approval. No automatic visual revision limit. Any material post-approval UI change invalidates the prior approved visual version for that change.

`ACTIVE.md` stays `NO_ACTIVE_HANDOFF` during this preparation/design iteration. No root/backend/AGENTS/skill/rule/hook code changes, schema changes, frontend files, app installs, model downloads or CLI Git publishing.

## D25-A realization and bounded sequencing

Use [backend extension contract](backend-capability-extension-contract.md) and [design approval boundary](design-state-coverage-and-approval-boundary.md). Before any implementation, reviewer independently verifies whether **H1-B Drive → H1-AUTH Google Sign-In → H1-F desktop Explorer** is the smallest safe, separately reviewable sequence. Any approved backend change requires an **independent exact-file, schema, risk and tests handoff**; a frontend-only authorization does not grant backend edits. No unreviewed changes to D01 `drive.file`, OAuth callback, authentication, Media S3, frozen modules or operational flags.

## 2026-11-08 selected preparation repair — no implementation authority

Independent PH-01–05 review is [historical finding evidence](reviews/2026-11-08-gdi26-s3-h1-independent-pre-handoff-review.md). The Owner has now **approved** the bounded prospective [Drive backend](h1-b-authority-schema-api-contract-v0.4.md), [search](h1-search-and-cursor-contract-v0.4.md), [thumbnail](h1-thumbnail-security-contract-v0.4.md), [BFF session](h1-f-bff-session-csrf-contract-v0.4.md) scope; **independent review remains outstanding**. Visual v0.4 is historical; D28 delegates auxiliary states. A separate [Google Sign-In auth contract](h1-google-sign-in-owner-identity-contract-v0.5.md) is a new review item. The v0.4 docket is historical; [D26–D30](owner-decision-addendum-v0.5.md) is current. Authentication owner linking needs security review; no executable handoff. H1-B, H1-AUTH and H1-F retain distinct scope and independent handoff/review; neither `frontend/` nor V27 may be created now.


## Updated slice sequence and desktop authority (D26–D30)

1. **H1-B Drive backend** first: new one-level child-folder V27, verified quota/search/thumbnail plus existing S2 preservation, exactly scoped independent Codex review and Owner publication.
2. **H1-AUTH Google Sign-In backend**, independently scoped: bind a pre-existing singleton PPV Owner to one verified Google OIDC `sub`, issue unchanged PPV access JWT+opaque refresh, protect login/recovery and preserve S1 Drive slot independence. An Authentication-owned migration is **tentatively V28 if V27 was published first**; independent reviewer must resolve version ordering. See [identity proposal](h1-google-sign-in-owner-identity-contract-v0.5.md). **Not** part of the previously approved Drive scope; needs independent security acceptance.
3. **H1-F Next.js BFF + desktop Explorer** last: initial 14-inch desktop only; one approved main visual v0.3; Codex chooses secondary states under the harness; no mobile app/layout deliverable. Replace normal password UI with Google Sign-In and preserve v0.4 session/CSRF/refresh/logout behavior.

All three are **proposed slices, not active handoffs**. No `frontend/` or backend source yet. Owner approval of product direction is not authority to implement. Full H1 preparation must pass independent `$codex-pre-handoff-review` and be Owner-published first; each actual slice requires separate executable handoff and independent final review. H0 is complete. [Owner addendum](owner-decision-addendum-v0.5.md) controls later choices.
