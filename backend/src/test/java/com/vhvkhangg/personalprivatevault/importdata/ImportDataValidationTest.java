package com.vhvkhangg.personalprivatevault.importdata;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import com.vhvkhangg.personalprivatevault.importdata.internal.parsing.CsvImportParser;
import com.vhvkhangg.personalprivatevault.importdata.internal.parsing.ImportParsedItem;
import com.vhvkhangg.personalprivatevault.importdata.internal.parsing.JsonImportParser;
import com.vhvkhangg.personalprivatevault.importdata.internal.parsing.MarkdownImportParser;
import com.vhvkhangg.personalprivatevault.importdata.internal.parsing.TargetPayloadCanonicalizer;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportJobException;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJsonSnapshot;
import com.vhvkhangg.personalprivatevault.importdata.view.InvalidImportJsonException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImportDataValidationTest {

    // =========================================================================
    // 1. CSV Parser Unit Tests
    // =========================================================================
    @Nested
    @DisplayName("CSV Parser unit tests")
    class CsvParserTests {

        @Test
        @DisplayName("Parses quoted fields, commas inside quotes, and multi-line values correctly")
        void parsesQuotedFieldsAndNewlines() throws IOException {
            String csv = """
                    title,type,description
                    "Standard Title",BOOK,"A simple description"
                    "Complex, Title with Comma",BOOK,"Line 1
                    Line 2
                    Line 3"
                    """;

            List<ImportParsedItem> items = CsvImportParser.parse(ImportTargetType.STUDY, csv);
            assertThat(items).hasSize(2);

            ImportParsedItem item1 = items.get(0);
            assertThat(item1.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item1.payload()).containsEntry("title", "Standard Title");
            assertThat(item1.payload()).containsEntry("type", "BOOK");

            ImportParsedItem item2 = items.get(1);
            assertThat(item2.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item2.payload()).containsEntry("title", "Complex, Title with Comma");
            assertThat(item2.payload().get("description").toString()).contains("Line 1\nLine 2\nLine 3");
        }

        @Test
        @DisplayName("Preserves markdown whitespace, indentation, hard breaks, and trailing newlines in content fields")
        void preservesMarkdownWhitespace() throws IOException {
            String content = "  # Indented Header\n    code block\nline with hard break  \nfinal newline\n";
            String csv = "title,contentMarkdown\n\"My Note\",\"" + content.replace("\n", "\r\n") + "\"";

            List<ImportParsedItem> items = CsvImportParser.parse(ImportTargetType.NOTE, csv);
            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.payload().get("contentMarkdown").toString().replace("\r\n", "\n")).isEqualTo(content);
        }

        @Test
        @DisplayName("Flags rows missing required fields as invalid with safe error messages")
        void flagsMissingRequiredFields() throws IOException {
            String csv = """
                    title,type
                    "",BOOK
                    """;

            List<ImportParsedItem> items = CsvImportParser.parse(ImportTargetType.STUDY, csv);
            assertThat(items).hasSize(1);
            assertThat(items.getFirst().status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items.getFirst().errorMessage()).contains("Missing required field: title");
        }

        @Test
        @DisplayName("Rejects CSV with unknown columns without leaking column names in error message")
        void flagsUnknownColumnsPrivacySafely() throws IOException {
            String csv = """
                    title,SECRET_KEY_CREDENTIAL_XYZ
                    "Test",123
                    """;

            List<ImportParsedItem> items = CsvImportParser.parse(ImportTargetType.NOTE, csv);
            assertThat(items).hasSize(1);
            assertThat(items.getFirst().status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items.getFirst().errorMessage()).isEqualTo("Unknown field encountered in item payload");
            assertThat(items.getFirst().errorMessage()).doesNotContain("SECRET_KEY_CREDENTIAL_XYZ");
        }

        @Test
        @DisplayName("Preserves unquoted content whitespace, leading indentation and trailing spaces")
        void preservesUnquotedContentWhitespace() throws IOException {
            String csv = "title,contentMarkdown\nSynthetic,  body  \n";
            List<ImportParsedItem> items = CsvImportParser.parse(ImportTargetType.NOTE, csv);
            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.payload().get("title")).isEqualTo("Synthetic");
            assertThat(item.payload().get("contentMarkdown")).isEqualTo("  body  ");
        }

        @Test
        @DisplayName("Rejects rows with extra or missing cells with safe error message and preserves item index")
        void rejectsInconsistentColumnCountRows() throws IOException {
            String csv = """
                    title,contentMarkdown
                    Note 1,body 1
                    Note 2,body 2,extra_cell
                    Note 3
                    Note 4,body 4
                    """;
            List<ImportParsedItem> items = CsvImportParser.parse(ImportTargetType.NOTE, csv);
            assertThat(items).hasSize(4);

            assertThat(items.get(0).itemIndex()).isEqualTo(0);
            assertThat(items.get(0).status()).isEqualTo(ImportItemStatus.VALID);

            assertThat(items.get(1).itemIndex()).isEqualTo(1);
            assertThat(items.get(1).status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items.get(1).errorMessage()).isEqualTo("Inconsistent column count in CSV record");
            assertThat(items.get(1).payload()).isNull();

            assertThat(items.get(2).itemIndex()).isEqualTo(2);
            assertThat(items.get(2).status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items.get(2).errorMessage()).isEqualTo("Inconsistent column count in CSV record");
            assertThat(items.get(2).payload()).isNull();

            assertThat(items.get(3).itemIndex()).isEqualTo(3);
            assertThat(items.get(3).status()).isEqualTo(ImportItemStatus.VALID);
        }

        @Test
        @DisplayName("Rejects duplicate or ambiguous headers in CSV import")
        void rejectsDuplicateOrAmbiguousHeaders() {
            String csvDuplicateExact = "title,contentMarkdown,title\nNote,body,extra\n";
            assertThatThrownBy(() -> CsvImportParser.parse(ImportTargetType.NOTE, csvDuplicateExact))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Duplicate or ambiguous header in CSV import");

            String csvDuplicateCase = "Title,contentMarkdown,title\nNote,body,extra\n";
            assertThatThrownBy(() -> CsvImportParser.parse(ImportTargetType.NOTE, csvDuplicateCase))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Duplicate or ambiguous header in CSV import");

            String csvBlankHeader = "title,,contentMarkdown\nNote,extra,body\n";
            assertThatThrownBy(() -> CsvImportParser.parse(ImportTargetType.NOTE, csvBlankHeader))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Duplicate or ambiguous header in CSV import");
        }
    }

    // =========================================================================
    // 2. JSON Parser Unit Tests
    // =========================================================================
    @Nested
    @DisplayName("JSON Parser unit tests")
    class JsonParserTests {

        @Test
        @DisplayName("Rejects non-array root JSON")
        void rejectsNonArrayRoot() {
            String nonArrayJson = """
                    {
                      "title": "Single Object"
                    }
                    """;

            assertThatThrownBy(() -> JsonImportParser.parse(ImportTargetType.NOTE, nonArrayJson))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessageContaining("Top-level JSON value must be an array");
        }

        @Test
        @DisplayName("Rejects malformed JSON syntax with privacy-safe error message")
        void rejectsMalformedJsonSyntaxPrivacySafely() {
            String malformed = "[ { \"SECRET_PASS\": \"1234\", ";
            assertThatThrownBy(() -> JsonImportParser.parse(ImportTargetType.NOTE, malformed))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Invalid JSON syntax in import file")
                    .hasMessageNotContaining("SECRET_PASS");
        }

        @Test
        @DisplayName("Flags unknown JSON properties privacy-safely without leaking property names")
        void flagsUnknownJsonPropertiesPrivacySafely() throws IOException {
            String json = """
                    [
                      {
                        "title": "A Note",
                        "contentMarkdown": "Some markdown content",
                        "SUPER_SECRET_FIELD": "sensitive_value"
                      }
                    ]
                    """;

            List<ImportParsedItem> items = JsonImportParser.parse(ImportTargetType.NOTE, json);
            assertThat(items).hasSize(1);
            assertThat(items.getFirst().status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items.getFirst().errorMessage()).isEqualTo("Unknown field encountered in item payload");
            assertThat(items.getFirst().errorMessage()).doesNotContain("SUPER_SECRET_FIELD");
        }

        @Test
        @DisplayName("Flags non-object array elements as invalid items")
        void flagsNonObjectArrayElements() throws IOException {
            String arrayWithPrimitive = """
                    [
                      "just a string",
                      123
                    ]
                    """;

            List<ImportParsedItem> items = JsonImportParser.parse(ImportTargetType.NOTE, arrayWithPrimitive);
            assertThat(items).hasSize(2);
            assertThat(items.get(0).status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items.get(0).errorMessage()).contains("Array element must be a JSON object");
            assertThat(items.get(1).status()).isEqualTo(ImportItemStatus.INVALID);
        }

        @Test
        @DisplayName("Enforces exact integer conversion and rejects fractional IDs")
        void rejectsFractionalIds() throws IOException {
            String json = """
                    [
                      {
                        "title": "Study Book",
                        "type": "BOOK",
                        "authorPersonId": 3.9
                      }
                    ]
                    """;

            List<ImportParsedItem> items = JsonImportParser.parse(ImportTargetType.STUDY, json);
            assertThat(items).hasSize(1);
            assertThat(items.getFirst().status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items.getFirst().errorMessage()).contains("Invalid field format in study item");
        }

        @Test
        @DisplayName("Preserves high-precision decimal numbers without floating point conversion distortion")
        void preservesHighPrecisionDecimals() throws IOException {
            String json = """
                    [
                      {
                        "title": "Study Book",
                        "type": "BOOK",
                        "priceAmount": 123456789.987654321
                      }
                    ]
                    """;

            List<ImportParsedItem> items = JsonImportParser.parse(ImportTargetType.STUDY, json);
            assertThat(items).hasSize(1);
            assertThat(items.getFirst().status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(items.getFirst().payload().get("priceAmount")).isEqualTo(new BigDecimal("123456789.987654321"));
        }

        @Test
        @DisplayName("Rejects complex structures for scalar string fields")
        void rejectsComplexStructuresForScalarFields() throws IOException {
            String json = """
                    [
                      {
                        "title": { "nested": "object" },
                        "contentMarkdown": "Some content"
                      }
                    ]
                    """;

            List<ImportParsedItem> items = JsonImportParser.parse(ImportTargetType.NOTE, json);
            assertThat(items).hasSize(1);
            assertThat(items.getFirst().status()).isEqualTo(ImportItemStatus.INVALID);
        }
    }

    // =========================================================================
    // 3. Markdown Parser Unit Tests
    // =========================================================================
    @Nested
    @DisplayName("Markdown Parser unit tests")
    class MarkdownParserTests {

        @Test
        @DisplayName("Extracts YAML frontmatter while preserving entire raw markdown unchanged")
        void extractsFrontmatterAndPreservesRaw() {
            String raw = """
                    ---
                    title: My Custom Title
                    category: Reference
                    version: 1.0
                    ---
                    # Document Title
                    Markdown content goes here.
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(ImportTargetType.NOTE, "doc.md", "hash123", raw);
            assertThat(items).hasSize(1);

            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(item.payload()).containsEntry("title", "My Custom Title");
            assertThat(item.payload()).containsEntry("contentMarkdown", raw);
            assertThat(item.payload()).containsEntry("importedFileName", "doc.md");
            assertThat(item.payload()).containsEntry("importedFileHash", "hash123");

            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) item.payload().get("frontmatter");
            assertThat(fm).isNotNull();
            assertThat(fm).containsEntry("category", "Reference");
        }

        @Test
        @DisplayName("Falls back to filename stem when frontmatter title is omitted")
        void fallsBackToFilenameStem() {
            String raw = """
                    # No Frontmatter
                    Just raw notes.
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(ImportTargetType.NOTE, "my-notes.md", "hash456", raw);
            assertThat(items).hasSize(1);
            assertThat(items.getFirst().payload()).containsEntry("title", "my-notes");
            assertThat(items.getFirst().payload()).containsEntry("contentMarkdown", raw);
        }

        @Test
        @DisplayName("Rejects non-map array frontmatter as invalid syntax")
        void rejectsNonMapFrontmatter() {
            String raw = """
                    ---
                    - arrayItem1
                    - arrayItem2
                    ---
                    # Content
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(ImportTargetType.NOTE, "array.md", "hash789", raw);
            assertThat(items).hasSize(1);
            assertThat(items.getFirst().status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items.getFirst().errorMessage()).contains("Invalid frontmatter YAML syntax");
        }

        @Test
        @DisplayName("Canonicalizer rejects supplied frontmatter that is not a map or has invalid keys")
        void canonicalizerRejectsMalformedFrontmatter() {
            Map<String, Object> rawWithArrayFm = new HashMap<>();
            rawWithArrayFm.put("title", "Valid Title");
            rawWithArrayFm.put("contentMarkdown", "Valid Content");
            rawWithArrayFm.put("frontmatter", List.of("not", "a", "map"));

            ImportParsedItem item1 = TargetPayloadCanonicalizer.canonicalize(ImportTargetType.NOTE, 0, rawWithArrayFm);
            assertThat(item1.status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(item1.errorMessage()).contains("Malformed frontmatter in note item");

            Map<String, Object> rawWithBlankKeyFm = new HashMap<>();
            rawWithBlankKeyFm.put("title", "Valid Title");
            rawWithBlankKeyFm.put("contentMarkdown", "Valid Content");
            rawWithBlankKeyFm.put("frontmatter", Map.of("   ", "val"));

            ImportParsedItem item2 = TargetPayloadCanonicalizer.canonicalize(ImportTargetType.NOTE, 0, rawWithBlankKeyFm);
            assertThat(item2.status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(item2.errorMessage()).contains("Malformed frontmatter in note item");
        }

        @Test
        @DisplayName("Preserves high precision YAML decimal numbers in frontmatter without floating point distortion")
        void preservesHighPrecisionYamlDecimals() {
            String raw = """
                    ---
                    precise: 123456789.987654321
                    ---
                    # Precise Note
                    Content here.
                    """;

            List<ImportParsedItem> items = MarkdownImportParser.parse(ImportTargetType.NOTE, "precise.md", "hash_p", raw);
            assertThat(items).hasSize(1);
            ImportParsedItem item = items.getFirst();
            assertThat(item.status()).isEqualTo(ImportItemStatus.VALID);

            @SuppressWarnings("unchecked")
            Map<String, Object> fm = (Map<String, Object>) item.payload().get("frontmatter");
            assertThat(fm).isNotNull();
            assertThat(fm.get("precise")).isEqualTo(new BigDecimal("123456789.987654321"));
        }

        @Test
        @DisplayName("Rejects complex structures for scalar frontmatter fields like summary and source")
        void rejectsComplexStructuresForScalarFrontmatterFields() {
            String rawSummaryList = """
                    ---
                    summary: [one, two]
                    ---
                    # Content
                    """;
            List<ImportParsedItem> items1 = MarkdownImportParser.parse(ImportTargetType.NOTE, "complex.md", "hash_c", rawSummaryList);
            assertThat(items1).hasSize(1);
            assertThat(items1.getFirst().status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items1.getFirst().errorMessage()).isEqualTo("Invalid field format in note item");

            String rawTitleMap = """
                    ---
                    title:
                      nested: object
                    ---
                    # Content
                    """;
            List<ImportParsedItem> items2 = MarkdownImportParser.parse(ImportTargetType.NOTE, "complex2.md", "hash_c2", rawTitleMap);
            assertThat(items2).hasSize(1);
            assertThat(items2.getFirst().status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items2.getFirst().errorMessage()).isEqualTo("Invalid field format in note item");
        }

        @Test
        @DisplayName("Rejects blank and nested blank frontmatter keys with safe error message")
        void rejectsBlankAndNestedBlankFrontmatterKeys() {
            String rawBlankKey = """
                    ---
                    "   ": blank key value
                    ---
                    # Content
                    """;
            List<ImportParsedItem> items1 = MarkdownImportParser.parse(ImportTargetType.NOTE, "blank.md", "hash_b", rawBlankKey);
            assertThat(items1).hasSize(1);
            assertThat(items1.getFirst().status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items1.getFirst().errorMessage()).isEqualTo("Malformed frontmatter in note item");

            String rawNestedBlankKey = """
                    ---
                    nested:
                      "  ": nested blank key
                    ---
                    # Content
                    """;
            List<ImportParsedItem> items2 = MarkdownImportParser.parse(ImportTargetType.NOTE, "nested_blank.md", "hash_nb", rawNestedBlankKey);
            assertThat(items2).hasSize(1);
            assertThat(items2.getFirst().status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items2.getFirst().errorMessage()).isEqualTo("Malformed frontmatter in note item");
        }
    }

    // =========================================================================
    // 4. ImportJsonSnapshot Tests
    // =========================================================================
    @Nested
    @DisplayName("ImportJsonSnapshot unit tests")
    class ImportJsonSnapshotTests {

        @Test
        @DisplayName("Deeply isolates maps and lists against caller mutations")
        void deeplyIsolatesInput() {
            Map<String, Object> map = new HashMap<>();
            map.put("key", "val");

            Map<String, Object> snapshot = ImportJsonSnapshot.toUnmodifiableSnapshot(map);
            map.put("key", "MUTATED");
            map.put("extra", "NEW");

            assertThat(snapshot).containsEntry("key", "val");
            assertThat(snapshot).doesNotContainKey("extra");
        }

        @Test
        @DisplayName("Rejects null keys and cyclic maps")
        void rejectsNullKeysAndCycles() {
            Map<String, Object> nullKeyMap = new HashMap<>();
            nullKeyMap.put(null, "val");

            assertThatThrownBy(() -> ImportJsonSnapshot.toUnmodifiableSnapshot(nullKeyMap))
                    .isInstanceOf(InvalidImportJsonException.class);

            Map<String, Object> cycle = new HashMap<>();
            cycle.put("self", cycle);

            assertThatThrownBy(() -> ImportJsonSnapshot.toUnmodifiableSnapshot(cycle))
                    .isInstanceOf(InvalidImportJsonException.class)
                    .hasMessageContaining("Cyclic reference detected");
        }
    }
}
