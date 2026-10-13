package com.vhvkhangg.personalprivatevault.importdata.internal.parsing;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportJobException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JsonImportParser unit tests (SBA-006)")
class JsonImportParserTest {

    @Nested
    @DisplayName("Audit fixtures: Trailing tokens and invalid JSON documents")
    class AuditTrailingTokensTests {

        @Test
        @DisplayName("Audit fixture: Array followed by second JSON object is rejected with privacy-safe message")
        void rejectsArrayFollowedBySecondObject() {
            String raw = """
                    [{"title": "Valid Note", "contentMarkdown": "Note body"}]
                    {"ignored": true}
                    """;

            assertThatThrownBy(() -> JsonImportParser.parse(ImportTargetType.NOTE, raw))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Invalid JSON syntax in import file");
        }

        @Test
        @DisplayName("Audit fixture: Array followed by trailing garbage token is rejected with privacy-safe message")
        void rejectsArrayFollowedByGarbage() {
            String raw = """
                    [{"title": "Valid Note", "contentMarkdown": "Note body"}]
                    garbage
                    """;

            assertThatThrownBy(() -> JsonImportParser.parse(ImportTargetType.NOTE, raw))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Invalid JSON syntax in import file");
        }

        @Test
        @DisplayName("Array followed by second JSON array is rejected")
        void rejectsSecondArray() {
            String raw = """
                    [{"title": "First"}]
                    [{"title": "Second"}]
                    """;

            assertThatThrownBy(() -> JsonImportParser.parse(ImportTargetType.NOTE, raw))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Invalid JSON syntax in import file");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "[{\"title\": \"Valid\"}] 12345",
                "[{\"title\": \"Valid\"}] \"trailing string\"",
                "[{\"title\": \"Valid\"}] true",
                "[{\"title\": \"Valid\"}] false",
                "[{\"title\": \"Valid\"}] null",
                "[{\"title\": \"Valid\"}] ;;;",
                "[{\"title\": \"Valid\"}] }",
                "[{\"title\": \"Valid\"}] ]"
        })
        @DisplayName("Array followed by trailing scalar or symbol token is rejected")
        void rejectsTrailingScalarsAndSymbols(String raw) {
            assertThatThrownBy(() -> JsonImportParser.parse(ImportTargetType.NOTE, raw))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Invalid JSON syntax in import file");
        }

        @Test
        @DisplayName("Malformed first JSON root is rejected with privacy-safe message")
        void rejectsMalformedFirstRoot() {
            String raw = "[{\"title\": \"Unclosed string";

            assertThatThrownBy(() -> JsonImportParser.parse(ImportTargetType.NOTE, raw))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Invalid JSON syntax in import file");
        }
    }

    @Nested
    @DisplayName("Non-array roots and empty inputs")
    class NonArrayRootsAndEmptyInputsTests {

        @Test
        @DisplayName("Object root is rejected with Top-level JSON value must be an array")
        void rejectsObjectRoot() {
            String raw = "{\"title\": \"Single Object\", \"contentMarkdown\": \"Body\"}";

            assertThatThrownBy(() -> JsonImportParser.parse(ImportTargetType.NOTE, raw))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Top-level JSON value must be an array");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "\"string root\"",
                "12345",
                "99.99",
                "true",
                "false",
                "null"
        })
        @DisplayName("Scalar roots are rejected with Top-level JSON value must be an array")
        void rejectsScalarRoots(String raw) {
            assertThatThrownBy(() -> JsonImportParser.parse(ImportTargetType.NOTE, raw))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Top-level JSON value must be an array");
        }

        @Test
        @DisplayName("Null rawText throws Invalid JSON syntax in import file")
        void nullRawTextThrows() {
            assertThatThrownBy(() -> JsonImportParser.parse(ImportTargetType.NOTE, null))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Invalid JSON syntax in import file");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\r\n\t  "})
        @DisplayName("Empty or whitespace-only rawText throws Invalid JSON syntax in import file")
        void emptyOrWhitespaceRawTextThrows(String raw) {
            assertThatThrownBy(() -> JsonImportParser.parse(ImportTargetType.NOTE, raw))
                    .isInstanceOf(InvalidImportJobException.class)
                    .hasMessage("Invalid JSON syntax in import file");
        }
    }

    @Nested
    @DisplayName("Valid JSON arrays, trailing whitespace, and decimals")
    class ValidArraysAndDecimalsTests {

        @Test
        @DisplayName("Valid array with trailing spaces, tabs, and newlines is accepted")
        void validArrayWithTrailingWhitespaceAccepted() throws IOException {
            String raw = """
                    [
                      {
                        "title": "Clean Note",
                        "contentMarkdown": "Clean Body"
                      }
                    ] \t\r\n
                    """;

            List<ImportParsedItem> items = JsonImportParser.parse(ImportTargetType.NOTE, raw);
            assertThat(items).hasSize(1);
            assertThat(items.getFirst().status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(items.getFirst().payload()).containsEntry("title", "Clean Note");
            assertThat(items.getFirst().payload()).containsEntry("contentMarkdown", "Clean Body");
        }

        @Test
        @DisplayName("Empty JSON array is accepted and returns empty items list")
        void emptyJsonArrayAccepted() throws IOException {
            List<ImportParsedItem> items = JsonImportParser.parse(ImportTargetType.NOTE, "[]");
            assertThat(items).isEmpty();
        }

        @Test
        @DisplayName("Non-object array element produces INVALID item")
        void nonObjectArrayElementProducesInvalidItem() throws IOException {
            String raw = """
                    [
                      {"title": "Good Note", "contentMarkdown": "Good Body"},
                      "string element instead of object"
                    ]
                    """;

            List<ImportParsedItem> items = JsonImportParser.parse(ImportTargetType.NOTE, raw);
            assertThat(items).hasSize(2);
            assertThat(items.get(0).status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(items.get(1).status()).isEqualTo(ImportItemStatus.INVALID);
            assertThat(items.get(1).errorMessage()).isEqualTo("Array element must be a JSON object");
        }

        @Test
        @DisplayName("Decimals in JSON are parsed into BigDecimal without float loss")
        void decimalPrecisionPreserved() throws IOException {
            String raw = """
                    [
                      {
                        "title": "Study Course",
                        "type": "COURSE",
                        "priceAmount": 12345678901234567890.123456789,
                        "currencyCode": "USD"
                      }
                    ]
                    """;

            List<ImportParsedItem> items = JsonImportParser.parse(ImportTargetType.STUDY, raw);
            assertThat(items).hasSize(1);
            assertThat(items.getFirst().status()).isEqualTo(ImportItemStatus.VALID);
            assertThat(items.getFirst().payload().get("priceAmount")).isInstanceOf(BigDecimal.class);
            assertThat(items.getFirst().payload().get("priceAmount").toString())
                    .isEqualTo("12345678901234567890.123456789");
        }
    }
}
