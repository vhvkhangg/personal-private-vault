# Storage, Import, Export, and Backup Architecture

## 1. Media storage

Large media binaries are not stored in PostgreSQL.

PostgreSQL stores metadata such as:

- object key;
- MIME type;
- dimensions;
- size;
- checksum;
- timestamps;
- logical album/location metadata.

The binary object is stored through an S3-compatible storage abstraction.

The production provider is deferred. Local development initially stores only small test objects, not the owner's multi-GB personal media library.

## 2. Import pipeline

Supported structured import targets include Study, Information, Vocabulary, and Note.

Supported input formats include CSV, JSON, and Markdown where applicable.

The required workflow is:

```text
Upload
  -> Parse
  -> Preview
  -> Validate
  -> Detect duplicates/errors
  -> User chooses update/skip/cancel decisions
  -> Transactional import
```

Upload must not write final business records immediately.

## 3. Duplicate handling

Duplicate detection may use stable external identifiers, URLs, normalized natural keys, or file hashes depending on the target type.

Markdown re-import must report duplicates and allow the owner to update the existing record or abandon the import.

## 4. Obsidian/Markdown preservation

Imported notes may preserve:

- YAML frontmatter;
- `[[wikilinks]]`;
- `#tags`;
- `![[embeds]]`;
- Obsidian callouts;
- unknown frontmatter fields.

Unknown frontmatter is retained rather than silently discarded.

## 5. Saved resources

Feed/social/web resources are first stored with provenance as `SavedResource` records. They may later be converted into Information, Note, or Study through explicit application use cases.

The original provenance remains available after conversion.

## 6. Export and portability

The system must support owner-controlled export independent of the UI.

Target portable forms include:

- structured JSON for relational records;
- Markdown for note-like content;
- media manifests/object keys for binary media;
- metadata needed to reconstruct associations.

Export design must avoid binding personal data to an internal Java serialization format.

## 7. Backup

Planned backup scope:

- PostgreSQL dump;
- media manifest/metadata;
- necessary application configuration that is safe to back up;
- retained raw import files where configured;
- binary media according to the selected object-storage provider strategy.

Backup destination, retention, encryption, and automation details are deferred until deployment architecture is selected.
