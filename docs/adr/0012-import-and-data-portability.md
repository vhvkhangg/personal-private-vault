# ADR-0012 — Treat Import and Data Portability as First-Class Capabilities

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

The vault will ingest AI/Obsidian Markdown and structured CSV/JSON data and is intended to hold long-lived personal information. The owner must be able to recover/export data even if the application is abandoned later.

## Decision

Use an explicit import pipeline with parse/preview/validate/duplicate-review/transactional-import stages. Preserve Markdown and unknown Obsidian frontmatter where practical. Provide portable export formats such as JSON, Markdown, and media manifests.

## Rationale

A staged import prevents silent destructive writes, while portable export prevents application lock-in of personal data.

## Consequences

- Import jobs keep provenance/status.
- Re-import exposes duplicate/update/skip decisions.
- Binary backup/export is coordinated with the object-storage provider.

## Alternatives considered

- **Upload and immediately persist:** rejected because validation/duplicate conflicts need user review.
- **Application-specific binary export only:** rejected because it would undermine portability.
