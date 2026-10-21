# Slice 2 fresh held-out label review package — human review pending

**No human has reviewed or signed this package. No parser/OCR quality evaluation has run on it.**
The [manifest](manifest.json) is frozen at SHA-256
`406e3aeedc78e943cd494e30452819d18e3ceba4e1bc2e4612845d5eea225e99`.
[Adjudication](adjudication.json) deliberately has null reviewer/signature/error decisions.
This package is preparation, not genuine human-adjudicated acceptance evidence.

The 15 synthetic source documents/negative fixtures have 7 source-family IDs, 7 actual PDF pages,
59 labeled material/structural blocks, 8 table instances and 35 logical cells. Merged cells count
as one logical cell; their covered-cell span remains labeled. Zero pages for non-page formats
means pagination is not owned by that format, not missing evaluation. The format/script ledgers
include corrupt and locator-negative files; multi-script document rows are not additive.
All 15 remain **NOT_RUN**, not passing or skipped.

The [structural closure audit](closure-audit.json) derives **5 connected family/dependency units**
from those 7 family IDs: courier, workshop and their mixed document are one connected unit.
It supplies per-format/script connected-group denominators, including negative cases. Do not treat
the 7 declared IDs as 7 independent groups. This automated membership audit does not replace human
assessment of semantic family independence or near-duplicates.

| Sources | Human check against original source and manifest gold |
| --- | --- |
| [English scan](courier-en.pdf), [Vietnamese scan](courier-vi.pdf), [Simplified scan](courier-zh-Hans.pdf), [Traditional scan](courier-zh-Hant.pdf) | Exact diacritics, script forms, digits, punctuation, original ink boxes, heading before table, ruled/merged cells and reading order. |
| [Native PDF](workshop-native.pdf), [mixed PDF](courier-mixed.pdf) | Courier glyph metrics and original 1-based pages; the mixed document includes both original sources, not a new independent family across splits. |
| [TXT](maintenance.txt) | Exact UTF-8 source characters, original spans/lines; no replacement decoding. |
| [Markdown parent](maintenance.md), [approved child](tools.md) | Whole-file frontmatter/comments and original child source/revision/hash/line/block anchor. Both are one held-out closure. |
| [DOCX](maintenance.docx) | Original paragraph, hidden run, anchored comment, core property and two physical table cells; no invented page. |
| [XLSX](inspection.xlsx), [XLS](inspection.xls) | Original cells, merged anchor/covered span, grouped hidden columns and comments; formula text stays inert and its missing cache is explicit. |
| [Corrupt DOCX](broken.docx), [corrupt PDF](broken.pdf), [missing locator](missing-locator.json) | Expected whole-file failure/locator rejection; never discard these from denominators. |

Labels come from the source-authoring strings, original OOXML/BIFF/PDF structures and font-renderer
ink boxes, not OCR or parser output. They are **candidate labels**, including unreviewed native
Courier geometry. Independently confirm them and source-family/semantic near-duplicate separation
before acceptance. The manifest's hashes and closure links prove byte identity and split membership,
not semantic independence or human approval. All translations, native/scan variants, the mixed
document's union and Markdown children remain held-out; historical shelf/invoice and current
warehouse/public examples are exposed development material.

To adjudicate, inspect every source beside its `gold` records. Record the actual reviewer,
review date, exact manifest hash, any label disagreements and each material/locator decision in a
separate signed record; second-check acceptance-critical ambiguities. A label correction requires
a **new dataset revision**, preserving this one. Do not simply mark this package approved without
examining it. Freeze a capable producer configuration before a separate independent held-out run;
then adjudicate every result against the accepted labels. No configuration is accepted yet.
