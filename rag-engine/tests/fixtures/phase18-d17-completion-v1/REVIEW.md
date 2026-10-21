# D17 completion development review package

Status: **IMPLEMENTER-AUTHORED / HUMAN AND INDEPENDENT REVIEW PENDING**.
No human signature, adjudication or independent acceptance is claimed.

`HtmlVault.md` is a public-safe authored development note, not a held-out evaluation note.
It combines properties, HTML across blank blocks, optional list/paragraph/table closing tags,
native table cells, inert embed-like text, details, Markdown tables, callouts, Mermaid, math
and a footnote. Exact source and structure expectations are in `test_d17_completion.py`.
Existing realistic vault fixtures in `../phase18-d17-obsidian-v1/` are reused unchanged.

`profile-corpus-gold.json` has 87 source-hashed composition/admission records against the frozen
652 CommonMark 0.31.2 and 677 GFM 0.29 examples. These are explicit D17 interpretation labels,
not independent labels. They account for literal Markdown within HTML, empty properties,
wikilink priority, globally enabled GFM extensions, the newer CommonMark comment grammar,
and 63 mandatory whole-document failures (unsafe HTML, unsupported/unapproved embeds,
invalid properties and malformed intended tables). The other examples use their official
expected HTML. Do not describe 1,329 examples as 1,329 successful documents.

The oracle reconstructs HTML solely from extracted AST hierarchy, attributes and semantic text;
checks source-backed HTML token partitions and native hierarchy; and compares normalized
structural semantics. Its producer mode uses the real local CPU embedding path and captures
the existing grant/stage boundary. It does not test Java authority or publication. The oracle
reuses html5lib for HTML normalization; this is not an independent implementation oracle.
Strict unadapted corpus failures remain in the receipts alongside the profile-aware results.

Reviewers should verify all 87 interpretation records against their stated official basis and
the frozen D17 contract, inspect native HTML hierarchy/source-context locator semantics, and
check table/property/child-revision expectations in both fixture suites. Verify complete source
coverage, exact original character/line/cell locators and zero chunks on every required failure.
Record disagreements as findings; do not alter labels merely to match implementation output.
Complete the mandatory maximum-workload resource evidence before any readiness claim.

Manifest hashes and exact verification commands are in the dated corrective evidence inventory.
Any later human review must record the actual reviewer, date, reviewed source/hash set,
decisions and signature in a new review record, preserving this unsigned package.
