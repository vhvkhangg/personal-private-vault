# P17-2 — Source eligibility, authoritative classification and field allowlists

**Status:** Owner-approved contract, amended 2026-10-15 under
[ADR-0020](../../adr/0020-phase-17-single-job-reservations-e1.md). This is required
behavior, not implemented proof. Containment remains; no execution or production
implementation is authorized by contract synchronization. The initial field
matrix remains unchanged. Live external capability stays disabled pending R7.

## 1. Mandatory two-authority proof (deny by default)

At admission/transfer/consumption/publication and every fresh index read, require
explicit individual item/field RAG consent AND fresh owning-domain proof of live,
right-type, non-NSFW source. NON_PRIVATE is an independent configuration veto;
approval cannot set a Private item NON_PRIVATE. Login/PIN/status/directory choice,
prior index or isNsfw=false alone never grant consent. Unknown/missing/malformed,
restricted quoted metadata or unapproved source/field/child fails closed.

RAG retains authorization_epoch, field_policy_version, approved fields, captured
Vault consent_reset_version and exact description confirmation hash. Vault retains
monotonic source_version/reset history even after deletion. Ordinary eligible
projected edits preserve standing participation, advance source_version and make
old indexes OUTDATED. Relevant dependency remove/readd also advances affected
scope/version. NSFW/trash/delete/restore resets consent history; Private/revoke/
field changes advance RAG policy epochs and preserve effective denial. Eligibility
restoration never restores consent. Settings are independent and retained.

### Approval/renewal intent CAS

An owner view exposes current sourceVersion, consentResetVersion,
authorizationEpoch and fieldPolicyVersion. Explicit approve/renew supplies
expectedSourceVersion, expectedConsentResetVersion, expectedAuthorizationEpoch,
expectedFieldPolicyVersion and the exact approvedFields. Under the stable source
and policy guards compare all expected values to fresh current state, verify
NON_PRIVATE/eligible/type/fields and any description confirmation, then capture
reset and advance consent epoch/policy atomically. Mismatch returns HTTP 409
SOURCE_CHANGED with no approval. Missing expected values never mean latest.
Do not auto-refresh/retry stale intent in background. A delayed pre-reset,
pre-revoke or pre-field-reduction request cannot grant fresh consent.

Comparing sourceVersion protects the approval command's observed intent; it
does not revoke standing participation on ordinary eligible edits. New approval
never reauthorizes an old job/generation. Item configuration and consent remain
separate; switching Private back to NON_PRIVATE leaves approval invalid.

### Exact description-hash rule

Retain explicit description opt-in and last confirmed exact description hash,
separate from item participation. At manual Index/Reindex, selected description
with the same confirmed hash needs no repeated prompt. Title/other edits alone
need neither item reapproval nor description confirmation. Changed description
must be explicitly confirmed with the current exact hash shown in preview; the
manual-start action may carry that confirmation, with no redundant separate
approval dialog. Missing/stale hash returns SOURCE_CHANGED/confirmation-required;
never silently include changed text or omit a requested field to report success.

Hash a coherent version-bound snapshot outside DB guards, then compare captured
source version and exact hash under fresh guards. Persist confirmation and job/
reservation admission atomically, or reserve first and safely abort before input
if confirmation fails. Preserve authoritative encoding and null/empty/whitespace
distinctions. Returning to identical confirmed description bytes may reuse that
confirmation on manual start, but source_version still makes the old index
OUTDATED. Description confirmation cannot renew reset/Private/revoked consent,
expand fields or authorize a child. Restrictive/unknown metadata still vetoes.

### Precedence, reservations and lifetimes

Domain trash/deletion/NSFW/Private/revocation and restrictive source metadata veto
all permissive claims, then exact source/field policy applies. public:true or
private:false cannot approve; malformed/conflicting or quoted restrictive values
deny/quarantine. Classifiers may flag risk but never certify eligibility. Preserve
all original exact initial field exclusions below and no source/secret logging.

During indexing every relevant selected-source edit/revoke/field/classification/
dependency change conflicts with 409 RAG_SOURCE_BUSY and commits no change.
Cancel, await verified safe termination/SQL sealing/release, then retry the edit;
never queue a denied change. After authoritative exclusion COMMIT, related data
is inaccessible immediately and enters verified cleanup. Unrelated items editable.

Completed persistent index authority uses current consent/reset/source versions/
fields/full dependency state and its own selection/retention policy. It is
independent of completed worker execution lease/service session/boot. Execution
observed-expiry gates remain under E1 in P17-3; ad-hoc selection expiry remains a
separate artifact scope, not a global consent or persistent-index TTL.

## 2. Initial source and field eligibility matrix

The first Phase 17 integration slice intentionally starts with **Fiction, Film and individually approved external documents**; this is not a reduction of the owner's eventual all-suitable-module requirement. Adding another module requires its owning public projection, field matrix, classification authority, revocation tests and owner acceptance. No generic `search` document/whole-DTO serialization is a RAG projection.

| Source | Authoritative domain/file checks | Approved initial payload **only after owner non-private approval** | Explicit exclusion and extra conditions |
| --- | --- | --- | --- |
| `FICTION` | `fiction` owns current `isNsfw`, content values; `vault` owns `deletedAt`, correct entry type; RAG catalog owns NON_PRIVATE grant. Fresh revisable projection required. | `id` locator; `title`, `originalTitle`, `format`, `nationalityCode`, `genreId`, `storyArchetypeIds`, `worldSettingIds`, `progressStatus`, `consumptionStatus`, `totalChapters` as typed tags/scalars. `description` **only via separately approved free-text field grant** tied to its content revision. | Exclude `posterUrl`, `currentProgressText`, `review`, person/group IDs and all other fields initially; opt-in extensions require field review. All consumption states eligible; no status filter by default. `isNsfw=true`, trashed, missing proof deny. |
| `FILM` | `film` owns `isNsfw` and content; `vault` owns trash/type; RAG catalog owns grant; fresh revisable projection required. | `id`; `title`, `originalTitle`, `format`, `productionStyle`, `nationalityCode`, `genreIds`, `storyArchetypeIds`, `worldSettingIds`, `progressStatus`, `consumptionStatus`, `totalEpisodes`. `description` only via separate free-text field grant/revision. | Exclude `posterUrl`, `directorPersonId`, `currentProgressText`, `review`, credits/links initially; no metadata expansion by inference. All statuses eligible. NSFW/trash/unknown deny. |
| External `.md`, `.txt`, `.docx`, `.pdf`, `.xlsx`, `.xls` | `rag` owner-grant record to one canonical local file or manifest entry; re-open with permitted filesystem adapter and match current version/hash; reject symlink escape, unexpected roots/paths, inaccessible source. | Approved **body portions** (text and tables) and minimal provenance: stable source ID, safe basename alias, kind, page/line/heading/block/table/sheet/cell locator; bounded parser/OCR confidence. | Directory selection only creates a candidate manifest, not approval. Unknown/unreviewed files, arbitrary YAML frontmatter/custom properties and Obsidian `%%comments%%` (default omit; later per-field approval required), macros/formulas execution, external URLs, hidden/embedded resources not separately approved: deny or omit and flag. `hidden worksheets` and spreadsheet notes/comments: excluded initially unless separately reviewed. |
| Embedded/transcluded targets | Each child is independently approved as above, with source revision/epoch and own field scope; canonical path inside approved root AND selected scope; recursive depth/cycle cap. | Approved child content with **child's own** provenance and dependency edge in parent's generation. | A safe parent does **not** authorize an in-scope unclassified, private, NSFW, revoked, escaped or missing child. Do not silently copy child text into a parent-only chunk. Skip/redact offending embed; deny parent generation if clean separation cannot be proven. |
| All other Vault modules (including Knowledge/Note/Study/Information/Vocabulary, Media, Collection, People, Location, Feed, Reference) | No approved RAG-specific classification/field projection in first slice. | **None in first slice.** | Future capability requires separately approved per-type matrix/projection; never copy existing whole views, `contentMarkdown` or user notes automatically. |
| Authentication, Settings secrets, Finance, Personal, Journal, Account and security-state fields in any module | Excluded by default; no blanket read authority from single-user login. | **None in first slice.** | Sensitive records, credentials, PIN/JWT/session/refresh material, bank/ledger/profile/diary contents and their derivatives may **not** be indexed; future narrow non-sensitive field proposal requires fresh explicit owner/Codex review. |

The matrix names match current `FictionView`, `FilmView`, and `VaultEntryView` source fields; these **are existing general views, not approved indexable contracts**. Future `RagFictionProjection`/`RagFilmProjection` are conceptual typed projections owned by the original domain, with only the field list above, source revision and classification/NSFW/trash proof. Do not implement these type names or new `@NamedInterface` now. Titles/descriptions can themselves contain private facts: field allowlisting alone is never a substitute for explicit owner approval and safety exclusions.

## 3. External file and Obsidian approval workflow

`choose path/directory -> canonicalize root and enumerate candidates -> display safe path aliases/types/hash and classification UNKNOWN -> owner reviews and approves each file and allowed body/metadata components -> mint scoped grant -> parse/index eligible bytes only`. Reapproval required on content/hash changes to free-text sources; watcher merely proposes a changed candidate, **never** silently blesses new bytes. No background crawl beyond explicitly approved roots; manual ad-hoc file still needs explicit classification/approval and TTL; empty/unavailable approval cannot fall back to previous index. Revoke candidate on path replacement, vanished file, symlink/scope change, grant expiry or inaccessible bytes. Do not use raw path as public ID or leak denied file names in counts/rankings.

Obsidian links, embeds, YAML properties, inline tags, comments and notes are **data**, not executable policy. Any frontmatter key not in a reviewed metadata allowlist is omitted from indexed metadata; useful user-chosen content may be indexed only after explicit field/region consent. Every embed target separately passes source approval, eligibility, exact hash, root/scope and epoch; transcluded content is attributed to the **child**, with parent dependency graph for invalidation. Do not dereference remote image/HTTP URLs or execute Dataview/scripts. Without a secure parser split, fail/omit the affected source rather than accidentally indexing excluded child/metadata.

## 4. Mandatory deny and acceptance fixtures

- Unclassified `.txt` or scanned `.pdf` selected ad-hoc, even inside an approved directory: **no OCR/embed/index/LLM/judge**, no content in logs or returned counts; only sanitized denial.
- Fiction/Film `isNsfw=false` but lacking owner NON_PRIVATE approval: deny. Fiction/Film `isNsfw=true` despite owner approval: deny. Trashed, wrong VaultEntryType or source revision missing: deny.
- A permitted Fiction title but an unapproved `review`, `currentProgressText` or credential-like metadata: only explicitly approved scalar fields leave Java; ensure neither embeddings nor metrics/prompt/citation contain forbidden field.
- Parent Obsidian `.md` approved, `![[child]]` targets an in-scope but **unclassified or revoked** child: no child text appears in parent chunk, retrieval, highlights, answer, history, judge prompt or recommendation feature. Cyclic and escaped links also deny.
- An approved external Markdown document gains `private: true` or revokes its approval while an old grant exists: strict restrictive-metadata/revocation precedence denies new indexing, retrieval, judging and answer/history use. `private: false` alone cannot approve an unclassified document.
- Owner chooses an entire directory, adds a new file later: new file remains `UNKNOWN` until its own approval. Changed document hash invalidates previous proof (no stale successful indexing). Hidden Excel sheets and external formulas/macros never execute/index by default.
- Across all positive fixtures, assert the exact approved field set, current revision, short-lived grant and provenance; test category/status search over all Fiction/Film states without violating eligibility.

See [revocation & fencing](revocation-and-derived-data.md) for mutations after a grant was issued and [benchmark acceptance](benchmarks-and-acceptance.md) for where the tests belong.
