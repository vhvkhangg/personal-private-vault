# D17-R03 HTML interpretation correction — 2026-10-20

**Codex corrective implementer record; independent re-review pending.** The owner authorized only
D17-FR-01/02 from the [independent final re-review](reviews/2026-10-20-d17-obsidian-markdown-independent-final-re-review.md).
This record changes no frozen syntax requirement and supplies no human signature.

For HTML-namespace non-void elements, a trailing solidus does not end the native container.
`<div/>` and inline `<span/>` therefore retain literal authored Markdown and `![[Child]]` inside
their HTML context. The checked HTML5 parser still rejects active content. Void elements keep their
existing terminating behavior; textarea/title retain RCDATA and optional closing tags retain native
repair. No foreign-content or executable capability is added.

An open native container belongs to its **owning Markdown container**. At document root, blank
lines and EOF do not terminate it. Within a list item or quote, that rule cannot annex a sibling
item or outdented structure. The existing Markdown parser supplies adjusted indentation, quote
prefixes, source boundaries and line limits; HTML continuation now respects those values. It does
not guess container ownership from stripped text or reinterpret literal Markdown inside valid HTML.

CommonMark **175** and GFM **144** both contain the exact source:

```markdown
- <div>
- foo
```

The first item contains open native HTML. The second unindented marker starts a **second sibling
Markdown item**. Its source is not HTML text owned by the first item. The corrected projection
therefore restores the official two-item expectation. The previous one-item overlay was an
implementation error, as the independent reviewer identified; source-character equality did not
establish correct hierarchy. Outdented headings and independently approved sibling embeds likewise
remain outside the first item. Missing bindings outside HTML still fail the complete root.

The new [source-hashed interpretation overlay](d17-r03-container-interpretation-2026-10-20.json)
restores those two official expectations and retains the other **five independently supported**
interpretations unchanged. The original gold, prior seven-record overlay and independent
adjudication are preserved byte-for-byte. The new overlay is implementation evidence for fresh
independent adjudication, not a retroactive amendment to either historical record.

The focused tests cover block/inline solidus with and without child bindings, void/RCDATA/optional
tags, list siblings/outdents, nested lists/quotes/mixed containers, exact CRLF source/locators,
child revision attribution, real CPU embedding and serialized producer output. Broader corpus and
compatibility receipts are recorded in the [R03-only handback](slice-2-obsidian-markdown-d17-r03-only-handback-2026-10-20.md).

D17-R01/R02/R04/R05 retain independent CLOSED status. Phase 17 FROZEN; Slice 1/D14 CLOSED;
FR18-3 OPEN; indexing INACTIVE; historical Java gate FAILED; other formats and Slices 3/4 deferred
or unauthorized. No human approval, publication or further corrective round follows from this record.
