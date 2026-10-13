package com.vhvkhangg.personalprivatevault.importdata.internal.parsing;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportJobException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MarkdownImportParser unit tests (SBA-004)")
class MarkdownImportParserTest {

    @Nested
    @DisplayName("Audit fixtures and delimiter edge cases")
    class AuditFixturesAndDelimiterTests {

        @Test
        @DisplayName("Audit fixture 1: Prefix-looking YAML key inside frontmatter retains summary and metadata")
        void auditFixturePrefixLookingYamlKeyRetainsSummary() {
            String raw = """
                    ---
                    title: Architectural Patterns
                    ---custom: retained-meta
                    summary: High level patterns summary
                    sourceName: Architecture Digest
                    sourceUrl: https://arch.example.com
                    ---
                    # Architectural Patterns
                    Content body here.
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "patterns.md", "hash-pat", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.errorMessage()).isNull();

            Map<String, Object> payload = item.payload();
            assertThat(payload).containsEntry("title", "Architectural Patterns");
            assertThat(payload).containsEntry("summary", "High level patterns summary");
            assertThat(payload).containsEntry("sourceName", "Architecture Digest");
            assertThat(payload).containsEntry("sourceUrl", "https://arch.example.com");
            assertThat(payload).containsEntry("importedFileName", "patterns.md");
            assertThat(payload).containsEntry("importedFileHash", "hash-pat");
            assertThat(payload).containsEntry("contentMarkdown", raw);

            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) payload.get("frontmatter");
            assertThat(fm).isNotNull();
            assertThat(fm).containsEntry("---custom", "retained-meta");
            assertThat(fm).containsEntry("summary", "High level patterns summary");
            assertThat(fm).containsEntry("title", "Architectural Patterns");
        }

        @Test
        @DisplayName("Audit fixture 2: Adjacent empty frontmatter block followed by body thematic break")
        void auditFixtureAdjacentEmptyBlockFollowedByBodyThematicBreak() {
            String raw = "---\n---\n# Body Title\n---\nTrailing section after horizontal rule.";

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "audit-empty.md", "hash-empty", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.errorMessage()).isNull();

            Map<String, Object> payload = item.payload();
            assertThat(payload).containsEntry("title", "audit-empty");
            assertThat(payload).containsEntry("contentMarkdown", raw);
            assertThat(payload).containsEntry("importedFileName", "audit-empty.md");
            assertThat(payload).containsEntry("importedFileHash", "hash-empty");

            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) payload.get("frontmatter");
            assertThat(fm).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Adjacent empty frontmatter block with CRLF followed by body thematic break")
        void adjacentEmptyBlockCrlf() {
            String raw = "---\r\n---\r\n# Body Title\r\n---\r\nTrailing section.";

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "audit-crlf.md", "hash-crlf", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);

            Map<String, Object> payload = item.payload();
            assertThat(payload).containsEntry("title", "audit-crlf");
            assertThat(payload).containsEntry("contentMarkdown", raw);

            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) payload.get("frontmatter");
            assertThat(fm).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Closing delimiter at EOF without trailing newline is recognized")
        void closingDelimiterAtEofWithoutTrailingNewline() {
            String raw = "---\ntitle: EOF Note\nsummary: Summary at EOF\n---";

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "eof.md", "hash-eof", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.payload()).containsEntry("title", "EOF Note");
            assertThat(item.payload()).containsEntry("summary", "Summary at EOF");
            assertThat(item.payload()).containsEntry("contentMarkdown", raw);
        }

        @Test
        @DisplayName("Adjacent empty block ending exactly at EOF without trailing newline")
        void adjacentEmptyBlockAtEof() {
            String raw = "---\n---";

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "bare.md", "hash-bare", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.payload()).containsEntry("title", "bare");
            assertThat(item.payload()).containsEntry("contentMarkdown", raw);

            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) item.payload().get("frontmatter");
            assertThat(fm).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Normal frontmatter with CRLF line endings throughout")
        void normalFrontmatterWithCrlf() {
            String raw = "---\r\ntitle: Windows CRLF Note\r\nsummary: CRLF summary\r\n---\r\n# Content\r\nBody here.\r\n";

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "crlf-doc.md", "hash-crlf2", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.payload()).containsEntry("title", "Windows CRLF Note");
            assertThat(item.payload()).containsEntry("summary", "CRLF summary");
            assertThat(item.payload()).containsEntry("contentMarkdown", raw);
        }

        @Test
        @DisplayName("Multiple prefix-lookalikes in frontmatter do not terminate block prematurely")
        void multiplePrefixLookalikesDoNotTerminateFrontmatter() {
            String raw = """
                    ---
                    title: Lookalike Note
                    ----four_dashes: not_closer
                    ---another-lookalike: retained_val
                    summary: Final summary
                    ---
                    # Content
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "lookalike.md", "hash-look", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.payload()).containsEntry("title", "Lookalike Note");
            assertThat(item.payload()).containsEntry("summary", "Final summary");
            assertThat(item.payload()).containsEntry("contentMarkdown", raw);

            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) item.payload().get("frontmatter");
            assertThat(fm).containsEntry("----four_dashes", "not_closer");
            assertThat(fm).containsEntry("---another-lookalike", "retained_val");
        }
    }

    @Nested
    @DisplayName("Structural fallbacks and invalid frontmatter tests")
    class StructuralFallbacksAndInvalidFrontmatterTests {

        @Test
        @DisplayName("No frontmatter opener falls back to filename stem and treats full text as content")
        void noFrontmatterFallsBackToStem() {
            String raw = """
                    # Just Markdown
                    No frontmatter delimiters present.
                    ---
                    A thematic break in the body.
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "pure-notes.md", "hash-pn", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.payload()).containsEntry("title", "pure-notes");
            assertThat(item.payload()).containsEntry("contentMarkdown", raw);

            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) item.payload().get("frontmatter");
            assertThat(fm).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Unterminated frontmatter opener (no closing delimiter) leaves frontmatter empty and preserves raw text")
        void unterminatedOpenerLeavesFrontmatterEmpty() {
            String raw = """
                    ---
                    title: Unclosed Frontmatter
                    This file never has a closing triple-dash delimiter.
                    Just more text.
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "unclosed.md", "hash-unc", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.payload()).containsEntry("title", "unclosed");
            assertThat(item.payload()).containsEntry("contentMarkdown", raw);

            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) item.payload().get("frontmatter");
            assertThat(fm).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Malformed YAML syntax produces INVALID item with privacy-safe error message")
        void malformedYamlSyntaxProducesInvalidItem() {
            String raw = """
                    ---
                    title: [unclosed array
                    ---
                    # Content
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "malformed.md", "hash-mal", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(item.errorMessage()).isEqualTo("Invalid frontmatter YAML syntax");
            assertThat(item.payload()).isNull();
        }

        @Test
        @DisplayName("Non-map YAML root (e.g. sequence) produces INVALID item")
        void nonMapYamlRootProducesInvalidItem() {
            String raw = """
                    ---
                    - item1
                    - item2
                    ---
                    # Content
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "list.md", "hash-list", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(item.errorMessage()).isEqualTo("Invalid frontmatter YAML syntax");
            assertThat(item.payload()).isNull();
        }

        @Test
        @DisplayName("Blank key in frontmatter produces INVALID item via canonicalizer guard")
        void blankKeyProducesInvalidItem() {
            String raw = """
                    ---
                    title: Valid Title
                    "   ": blank key value
                    ---
                    # Content
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "blank.md", "hash-blank", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(item.errorMessage()).isEqualTo("Malformed frontmatter in note item");
            assertThat(item.payload()).isNull();
        }

        @Test
        @DisplayName("Precise decimals in frontmatter are preserved without float precision loss")
        void preciseDecimalsPreservedAsBigDecimal() {
            String raw = """
                    ---
                    title: Precision Note
                    precision_metric: 12345678901234567890.123456789
                    ---
                    # Content
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "precise.md", "hash-prec", raw
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);

            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) item.payload().get("frontmatter");
            assertThat(fm.get("precision_metric")).isInstanceOf(BigDecimal.class);
            assertThat(fm.get("precision_metric").toString()).isEqualTo("12345678901234567890.123456789");
        }

        @ParameterizedTest
        @EnumSource(value = ImportTargetType.class, names = {"NOTE"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Markdown format is rejected for non-NOTE targets")
        void rejectsNonNoteTargetTypes(ImportTargetType nonNoteType) {
            assertThatThrownBy(() -> MarkdownImportParser.parse(
                    nonNoteType, "test.md", "hash", "# Content"
            ))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Markdown format is only supported for NOTE target");
        }

        @Test
        @DisplayName("Null rawText produces INVALID item due to missing contentMarkdown")
        void nullRawTextTreatedAsEmpty() {
            List<ImportParsedItem> items = MarkdownImportParser.parse(
                    ImportTargetType.NOTE, "empty.md", "hash-empty", null
            );

            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(item.errorMessage()).isEqualTo("Missing required field: contentMarkdown");
            assertThat(item.payload()).isNull();
        }
    }
}
