# D17 whole-profile human checklist — 12 PENDING confirmations

Complete these **in addition to all 213 individual records**. Every human decision, identity, date
and signature remains blank. Use profile-checklist.csv or the JSON template to return decisions.
Machine dispositions are already VERIFIED by R03; these prompts request genuine content/quality
judgment and do not replace, waive or reopen machine verification by implication.

| Requirement | Human inspection prompt | Review references |
| --- | --- | --- |
| MD17-01 | Do headings, paragraphs, breaks, emphasis, code spans, escapes/entities, ordinary/reference/autolinks and GFM strike/tasks preserve authored meaning and hierarchy? Are official/profile interpretation differences explicit? | SYN-001–005, SYN-021–022, INT-001–087, ADJ-001–007 |
| MD17-02 | Inspect **every represented cell**, row/header/column/alignment, escaped pipe, nested formatting/link, implicit empty and overflow cell in Research; inspect quoted/list tables and native HTML tables. Are locators honest and hierarchy correct? | VAULT-Research, VAULT-HtmlVault, SYN-005–007/018, HTML-006, interpretation book |
| MD17-03 | Are original YAML names/values/spelling, aliases/tags/cssclasses, dates, nested values, booleans and numbers retained? Are empty maps truthful and malformed/unsafe/duplicate/expanding values whole-root failures? | VAULT-Research/HtmlVault, SYN-008, FAIL-005–008, interpretation book |
| MD17-04 | Are list/task/quote nesting, task states and heading ownership correct? Do HTML boundaries stop at siblings/outdents without annexing another item's source? | SYN-004/006–007/010, VAULT-Research, ADJ-003/006, PROD-001–052 |
| MD17-05 | Are wikilink targets, aliases, heading paths and block definitions/selections faithful? Do ordinary links remain inert? Are missing/ambiguous selectors denied explicitly? | SYN-009, VAULT-Research/Child/Grand, AUTH-004–006, LINK-002, interpretation book |
| MD17-06 | Trace Research v3 → Child v7 selected Section → Grand v11 answer, and R03 root v3 → child v19. Are authority bindings/edges attached to the actual source and parent node? Are conflicts, cycles, missing grants and excessive depth whole-root denials? | Four vault records, AUTH-001–007, LINK-001–002, PROD-001–052 |
| MD17-07 | Are arbitrary/nested callout types, titles and expanded/collapsed states preserved, with collapsed body and nested child content retained? | VAULT-Research/Child/Grand/HtmlVault, SYN-010 |
| MD17-08 | Are footnote definitions/references/continuations, inline footnotes, comments, Unicode/nested tags and all six highlights retained? Are inactive embed-like markers inert? | VAULT-Research/HtmlVault, SYN-011–012/017/021, FAIL-009 |
| MD17-09 | Are fenced/indented code, longer nested fences, Mermaid and LaTeX preserved verbatim/inertly? Do executable query/template directives reject whole roots rather than masquerading as computed content? | VAULT-Research/HtmlVault, SYN-013–014, FAIL-010–013 |
| MD17-10 | Review native HTML5 hierarchy and lexical source separately: blank blocks, optional ends, EOF, RCDATA, non-void solidus and owning Markdown containers. Are unsafe tags/attributes/URLs denied without rejecting harmless literal RCDATA? | ADJ-001–007, SYN-015/019–020/023–025, HTML-001–009, UNSAFE-001–005, PROD-001–052 |
| MD17-11 | Check exact source/revision attribution, Unicode/CRLF, noninvented line/block/cell locators, split context and child selectors. Retain R03's bounded resource/cancellation caveats; do not infer universal throughput or global-zero-OS-writes. | All actual-output chunk/node/cell tables; VAULT records; SYN-018; R03 review and preparation fingerprints |
| MD17-12 | Does every required failure reject the entire affected root with zero successful chunks, including valid prefixes? Are independent roots, captured producer boundaries and deferred Java/publication scope honestly described? Are all 213 individual decisions actually complete? | FAIL-001–015, UNSAFE-001–005, AUTH-001–007, negative PROD records, full ledger and R03 review |

For each MD17-01–12 row return: **ACCEPT / REJECT / UNCERTAIN**, actual reviewer identity, review date,
signature/attestation reference and notes. Do not prefill any decision from the prompts or machine
dispositions. Any missing, rejected, uncertain, unsigned or contradictory judgment remains pending
reconciliation; it is never silently converted to acceptance.
