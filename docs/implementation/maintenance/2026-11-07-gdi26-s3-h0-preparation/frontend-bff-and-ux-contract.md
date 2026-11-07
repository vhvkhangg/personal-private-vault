# GDI26-S3-H0 — Frontend architecture, BFF, browser and Drive boundaries

**Classification:** H0 harness must encode these **proposed verification requirements** so H1 cannot silently weaken S1/S2. No frontend/BFF server is implemented, hosted, tested or authorized by this document.

## Future minimum web architecture

- Future `frontend/`: Next.js App Router + TypeScript strict + shadcn/ui (with accessible underlying primitives). One SPA-like user experience; server/client responsibilities are explicit. Backend Spring Boot remains sole business/policy authority; BFF adapts transport/session, not Drive ownership. A Next route handler is publicly callable and must authenticate/authorize every sensitive operation.
- Browser communicates with Next BFF using a same-site, server-managed session (D13-A). **Google refresh tokens, PPV backend JWT access/refresh tokens, credential key and provider auth headers never enter browser-visible JS, Redux, localStorage/sessionStorage, public serialized props, URLs or ordinary logs.** Use HttpOnly/Secure-in-HTTPS/SameSite+Path cookies with bounded lifetime and fixation/CSRF protections. Choice of session library/encrypted store must be justified against a single-owner/local-first system; no hidden multi-tenant store by default.
- BFF server stores/refreshes backend PPV tokens confidentially, handles login/logout/session expiry and forces backend permission checks for each request. Spring remains authoritative for PPV JWT authentication and root/file/subject guards; front-end disabled controls and role claims are not authorization.
- Prevent browser-side access to internal backend if remote topology later places Spring on private network; this is an operator gate, **not** permission to silently deploy. No `NEXT_PUBLIC_` secrets or provider credentials.
- Configure BFF upload as bounded streaming or legitimate multipart pass-through; do not buffer 50MiB per upload in React/Next memory. Every intentional upload generates one UTC timestamp+UUIDv4 idempotency key before the first attempt and **retains the identical key and payload/name/MIME for retries**. A fresh intentional file action gets a new key. In `UPLOAD_OUTCOME_UNKNOWN`, never automatically allocate a new key or auto-retry a mutation; expose explicit same-action recovery. Browser-to-BFF retry semantics must not accidentally create a new provider ID.
- BFF download stream must preserve status/content-type/content-disposition/no-store/nosniff and cancellation, not JSON-buffer binary or cache private bytes; abort and notify on truncated/failed stream, avoiding false “download complete” UI.

## S1 browser OAuth caveat: localhost callback + cookie proof

S1 `POST /api/v1/drive/connections/{slot}/authorize` sets a browser binding cookie and yields an authorization URL; `GET /api/v1/drive/oauth/callback` is an exact **backend** callback and returns `text/plain` status. Cookie is host-only with Path `/api/v1/drive/oauth/callback` (does not follow the Next app path). A server-only BFF request to authorize, followed by a browser flow without the required cookie, **fails binding**. H1 must design and test safe `Set-Cookie` forwarding or a verified same-host interaction that preserves the correct browser jar and callback path. `localhost` front at one port and Spring at another does not waive host/cookie rules. Do **not** silently invent a callback redirect or change S1 OAuth state/cookie/JWT/public matcher. Remote host/HTTPS consent is separately gated and may require new approval.

## Reuse S2 four private operations, do not duplicate business authority

| Feature/UX | Backend route | Required front-end correctness |
|---|---|---|
| Ensure app-owned root | `POST /api/v1/drive/connections/{slot}/root-folder` | Expose status (`PENDING`, reconciliation etc.) without pretending success; cannot create arbitrary folder. |
| Direct-file list | `GET /api/v1/drive/connections/{slot}/files` | Honor opaque next cursor, 1–50 page contract, account switching invalidates old pages; no folder recursion or broad search. |
| Single file upload | `POST /api/v1/drive/connections/{slot}/files` | 50MiB/file + total request bounds, stable `Idempotency-Key`, visible admission/error/progress, no force-retry with new key on uncertainty. |
| Authorized download | `GET /api/v1/drive/connections/{slot}/files/{fileId}/download` | Stream original authorized binary without exposing raw Google URL or provider tokens; handle abort and failure. |

S1 connect/status/disconnect continues to serve *two slots* under one PPV owner, NOT multi-user SaaS. `account` (external account catalog), `personal` (profiles), `drive` (Google integration) are distinct backend Spring modules. UI navigation under **Personal → Storage** must not introduce a backend `personal → drive` dependency or new cross-module imports.

## UI interaction constraints to encode into H0 skills

- No independent Primary Drive icon; context-specific secondary navigation under Personal, with slot 1/2 switcher, connection state, root status, file table/list, upload/download affordances.
- Explicit skeleton/loading, disabled, disconnected, reconnect-required, empty, failure, pending/unknown upload, expired session, upload-size and download-stream errors. Show recovery actions only if supported by backend API; avoid fake success toast.
- Keyboard navigable, named buttons/labels/focus, no interactive div-only controls, tooltip not sole cue, accessible forms/progress/live announcements, safe filenames and text escaping, no untrusted Markdown rendering from Drive names.
- Privacy: neither analytics/telemetry nor third-party font/JS/CDN or remote UI image requests are presumed authorized. Any required network dependency must be reviewed/licensed and pinned.
- Feature OFF means no browser trying Google endpoints, no Drive UI implying a connected account, no credential capture to “make demo work.” H1 local fake-provider testing may be separately authorized by its own handoff.

## Decisions explicitly left for H1 architectural review

Selected BFF session library and session persistence model; actual developer host/callback routing; CSRF defense detail; proxy streaming/cache implementation; package manager and lockfile; exact frontend-origin to backend connectivity; whether a minimal backend API adaptation is actually needed. An adjustment of S1/S2 contracts, OAuth redirect, broad scope, exposure of credentials or live Cloud activation needs **new Owner approval**, not agent preference.

**Next step:** Codex verify the H0 harness instructs developers and reviewers to preserve these boundaries before it can be accepted; no browser execution is claimed here.
