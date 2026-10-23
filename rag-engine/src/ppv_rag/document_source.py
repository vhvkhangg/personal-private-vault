"""Accepted D17 extraction reused by production and Java-owned admission replay.

No filesystem child resolution, SQL, model downloads or alternative Markdown parser.
"""

import hashlib

from ppv_rag.contracts import DocumentRootTask, PreparedDocumentField
from ppv_rag.limits import MAX_TEXT_CHARS, checkpoint
from ppv_rag.parsers.markdown import ApprovedDependency, parse_markdown
from ppv_rag.parsers.models import ParsedDocument
from ppv_rag.parsers.obsidian import MarkdownFailure, _note
from ppv_rag.parsers.text import parse_text


def document_binding(root: DocumentRootTask) -> dict[str, object]:
    return dict(
        source_id=root.source_id,
        source_version=root.source_version,
        snapshot=root.snapshot.model_dump() if root.snapshot else None,
        dependencies={
            key: dict(
                source_key=v.source_key,
                source_version=v.source_version,
                approved_field=v.approved_field,
                snapshot=v.snapshot.model_dump() if v.snapshot else None,
            )
            for key, v in root.transclusions.items()
        },
    )


def parse_prepared_document(root: DocumentRootTask) -> ParsedDocument:
    if root.format == "DOMAIN":
        if root.content_text is not None or root.content_b64 is not None or root.transclusions:
            raise PermissionError("FIELD_NOT_APPROVED")
        doc = ParsedDocument(root.root_key, "TXT", root.source_version)
        for field in root.prepared_fields:
            doc.elements.extend(
                parse_text(
                    field.text, field.source_key, field.source_version, field.approved_field
                ).elements
            )
        return doc
    if root.format != "MD" or root.content_text is None or root.content_b64 is not None:
        raise ValueError("UNSUPPORTED_FORMAT")
    supplied = root.transclusions
    if root.snapshot:
        supplied = _snapshot_dependencies(root)
    return parse_markdown(
        root.content_text,
        root.source_id if root.snapshot else root.root_key,
        source_version=root.source_version,
        dependencies={
            key: ApprovedDependency(
                value.source_key, value.source_version, value.approved_field, value.text
            )
            for key, value in supplied.items()
        },
        approved_metadata_fields=frozenset(root.approved_fields),
        whole_file=True,
    )


def _snapshot_dependencies(root: DocumentRootTask) -> dict[str, PreparedDocumentField]:
    """Resolve only supplied approved S/R using D17's inert source context.

    Capture suffix hashes are lexical names, not filesystem capabilities. Ambiguity
    includes collisions between a real name, a UUID, and a frontmatter alias.
    """
    if not root.transclusions:
        return {}
    candidates: dict[str, PreparedDocumentField] = {}
    total = len(root.content_text or "")
    for value in root.transclusions.values():
        previous = candidates.setdefault(value.source_key, value)
        if previous != value:
            raise MarkdownFailure("MD_CONFLICTING_REVISION")
    total += sum(len(v.text) for v in candidates.values())
    if total > MAX_TEXT_CHARS:
        raise BufferError("RESOURCE_LIMIT_EXCEEDED: supplied Markdown payloads")
    names: dict[str, set[str]] = {}
    fingerprints: dict[str, set[str]] = {}
    targets: set[str] = set()
    values = [(root.source_id, root.source_version, "content", root.content_text or "")]
    values.extend(
        (v.source_key, v.source_version, v.approved_field, v.text) for v in candidates.values()
    )
    for source, version, field, text in values:
        checkpoint()
        note = _note(text, source, version, field)
        for unit in note.units:
            targets.update(e["target"] for e in unit.metadata["embeds"] if e["target"])
            if source in candidates:
                aliases = unit.metadata.get("properties", {}).get("aliases", [])
                if isinstance(aliases, str):
                    aliases = [aliases]
                if isinstance(aliases, list):
                    for alias in aliases:
                        if isinstance(alias, str):
                            names.setdefault(alias, set()).add(source)
        if source not in candidates:
            continue
        snapshot = candidates[source].snapshot
        if snapshot is None:
            raise PermissionError("FIELD_NOT_APPROVED")
        for name in (source, snapshot.source_name, snapshot.source_name[:-3]):
            names.setdefault(name, set()).add(source)
        for digest in snapshot.target_digests:
            fingerprints.setdefault(digest, set()).add(source)
    resolved: dict[str, PreparedDocumentField] = {}
    for target in targets:
        # Alias labels are literal approved properties, not filesystem paths.
        # Only captured relative path syntax participates in suffix lookup.
        matches = set(names.get(target, set()))
        unsafe_path = (
            target.startswith("/")
            or any(part in {"", ".", ".."} for part in target.split("/"))
            or any(char in target for char in "\\:\x00")
        )
        if not unsafe_path:
            matches |= fingerprints.get(hashlib.sha256(target.encode("utf-8")).hexdigest(), set())
        # D17 checks the names actually expanded by the requested heading/block.
        # An inactive target elsewhere in a supplied note is not a child read.
        if len(matches) == 1:
            resolved[target] = candidates[next(iter(matches))]
    return resolved
