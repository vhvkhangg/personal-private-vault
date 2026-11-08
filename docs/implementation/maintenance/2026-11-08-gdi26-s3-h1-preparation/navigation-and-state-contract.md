# H1 information architecture, wireflow and truthful states

## Pages and navigation (proposal, not routes frozen for implementation)

```text
PPV
├── Login / expired session / logout
└── Authenticated shell
    ├── Primary icon rail: Home (placeholder, not built), Personal [selected], ...
    └── Personal contextual sidebar
        ├── Overview (placeholder / non-navigable if not implemented)
        ├── Profile (placeholder / non-navigable if not implemented)
        └── Storage
            └── Google Drive [H1 implemented]
                ├── Account slot 1 / slot 2
                ├── Disconnected / reconnect-required / connected
                ├── Ensure PPV root (if absent; not arbitrary folder CRUD)
                ├── File/folder card grid with bounded cursor-backed infinite scroll (D25=A one-level folder backend extension review pending)
                ├── One Mới menu: upload original file / create direct child folder (D25=A, new backend review pending)
                ├── Name search, type filter, modified-date From/To (Drive-root API extension pending)
                ├── Per-slot storage usage (Drive about quota endpoint pending)
                ├── Authorized preview/fallback (backend source pending)
                └── Download (bounded stream)
```

**Do not create dead links** for non-H1 modules just to fill a sidebar. If a label is shown for visual context, clearly indicate unavailable and avoid an actionable navigation promise. No separately designed mobile navigation is required in this H1: D27 limits implementation review to 14-inch desktop; ordinary keyboard/zoom/reflow checks still apply.

## User flows

1. **Google Sign-In (D26):** browser begins a distinct Google OIDC login redirect with state/nonce/PKCE, separate from S1 Drive OAuth. **Spring** owns the one-use OIDC transaction, authorization-code exchange and ID-token validation; after issuer+`sub` singleton proof it creates a short-lived HttpOnly handoff ticket, and the BFF redeems it with a separate server-held verifier before receiving Spring PPV JWT + opaque refresh server-to-server. Unbound Google accounts are denied. Existing password login is for controlled first-time binding/recovery, **not** ordinary PPV website sign-in. See [identity design](h1-google-sign-in-owner-identity-contract-v0.5.md).
2. **Connect:** choose slot; BFF POST initiate to Spring, preserve exact backend OAuth browser binding cookie `PPV_DRIVE_BINDING_1/2` and navigate browser to authorization URL. Google returns directly to Spring fixed `http://localhost:8080/api/v1/drive/oauth/callback` with same localhost cookie jar/path; browser receives text/plain result; user returns to PPV and refreshes status. No invented BFF callback or token in URL.
3. **Switch slots:** abort/invalidate in-flight queries and stale upload UI for prior slot; do not show old slot data as if new; distinguish two separate Google subjects by slot/status, not by guessed emails.
4. **Ensure/list:** existing S2 only ensures one PPV root and lists verified direct binary children. New user-requested child-folder behavior requires D25 and separate accepted API extension. The **approved desktop visual uses grid/cards**, not desktop tables. Retain opaque `nextCursor` internally; trigger next bounded request on scrolling, including empty visible pages with a next cursor, without a visible numbered pager/count footer.
4a. **Search/filters:** “Tìm trong Drive hiện tại...” searches names within the current slot's **PPV-managed subset**, not arbitrary Google Drive; type and *modified-date* inclusive From/To must cover all eligible records, not just those already loaded. Clear/cancel prior cursor and stale results whenever slot, query or filters change. S2 does not yet expose these API parameters.
4b. **Quota/preview:** only show true per-slot quota from a reviewed Drive-owned endpoint; unknown/unlimited state is explicit. Preview loads lazily from authorized backend proxy or safe derivatives; unsupported formats render clearly labelled fallback, never synthetic imagery as live file content.
5. **Upload:** browser chooses one file; BFF guards max size/admission without whole-file buffering; one `Idempotency-Key=yyyyMMddTHHmmssZ.lowercase-UUIDv4` per intentional action; same key+bytes/name/MIME for explicit retry after unknown outcome; distinguish 201 new, 200 reconciled, `UPLOAD_IN_PROGRESS`, `UPLOAD_KEY_CONFLICT`, `UPLOAD_OUTCOME_UNKNOWN`, size/permission errors. No automatic new action on ambiguity.
6. **Download:** BFF streams authorized binary with no-store/nosniff/expected length and cancellation; do not claim browser completed saving when only the response began. A short/excess body or disconnect is a failed/incomplete transfer, not a success toast.

## States Codex must design/test under D28 (no additional derivative Owner visual approval)

| Context | Required states | Visual requirement |
| --- | --- | --- |
| Shell/auth | Google login start/callback/error/unbound Google subject, expired, logged in, logout, one-time binding/recovery | Keyboard/focus recovery, one-use state/nonce and no leaked credentials |
| Slot | OFF, disconnected, connecting/pending, connected, reconnect required, disconnect/revocation uncertainty | Distinct labels + safe status, no color-only meaning |
| Root | ABSENT, explicit ensure, PENDING/UNKNOWN, READY, revalidation failure | No false folder-ready projection or general folder creation |
| List | Loading, empty, populated, next cursor, filtered-empty page + cursor, timeout/error, filter/date/reset | Desktop grid/cards and actual previews/fallback; cursor-backed auto load-more; no visible pager/count footer; long VI names accessible |
| Upload | Ready, upload in progress, 201/200 verified, 400/413, 409 conflict, in-progress, outcome unknown, explicit recovery | Same-action retry guidance, do not conflate dispatch and success |
| Download | Pending, started, transport failed/short, canceled, permission error | No false completed-save badge |
| OAuth | Backend callback success/failure as text/plain, browser return to UI | Same-host/path cookie proof, no invented callback redirect |
| Desktop 14-inch / accessibility | 1366×768 and 1536×864 CSS px, keyboard and browser zoom/reflow | Desktop actions/filters/card grid remain usable; **mobile-specific UI deferred**; 320 CSS-pixel zoom reflow is separate accessibility evidence, not a mobile mockup |

**Approved desktop screen:** [v0.3](owner-visual-approval-record-v0.3.md); date filter uses `modifiedAt` unless revised by Owner. **D25=A folder semantics are chosen; the new API design/acceptance remain pending** [capability review](capability-gap-and-decision-gate.md). The older table/mobile layout in the historical v0.1 was superseded for the desktop Explorer.

API fields, error strings and exact response wrappers must be derived from running S1/S2 source/OpenAPI when H1 implementation becomes authorized. This preparation does not assert a live Google integration or a working browser.

## D25=A — root / child user flow (new scope, not S2 implementation)

- Initial account selection resolves current S1 slot and its PPV root. If root ABSENT, show a distinct **Khởi tạo thư mục PPV** action; it invokes S2 ensure only, with pending/uncertain/failure handling. A root already READY never shows the ensure action as a way to create child folders.
- In the root grid, verified child folders appear as folder tiles. `Mới` → **Thư mục mới** creates a *direct child of the root* through the separately gated Drive API; success only on independently verified provider metadata and durable intent. `Mới` → **Tải tệp lên** uploads an original file to the currently selected root or verified child.
- Opening a child shows breadcrumb `Google Drive / <child>`, that child's actual app-created binary files, and a back-to-root control. No grandchild listing/creation and no arbitrary external Drive folder. Reconnect/slot switch wipes stale folder selection/filters/cursors/previews.
- Search label **“Tìm trong Drive hiện tại...”** remains the Owner-approved literal; its scope is the currently selected account's PPV-managed root+one-level subtree, not whole Google Drive. Explain this via hint/accessible description. Backend search must include verified files not yet loaded by infinite scroll.
- A folder tile's item count is displayed **only if a verified API supplies it**; the approved image's numbers are illustrative. Actual selected-account quota data may refer to Google Drive or all Google services/pool; the text labels must be accurate, or show unknown.
- Uploading a whole existing local folder/directory tree is **not** authorized by D25=A. Do not infer that from the word “folder” in `Mới`.

See [backend extension](backend-capability-extension-contract.md), [Google auth](h1-google-sign-in-owner-identity-contract-v0.5.md) and [visual state delegation](design-state-coverage-and-approval-boundary.md).


### Desktop-only acceptance clarification

**H1 current D27 desktop-only:** auxiliary states are Codex-delegated under D28; no mobile layout/drawer is a present deliverable. (Earlier mobile guidance is historical.)
