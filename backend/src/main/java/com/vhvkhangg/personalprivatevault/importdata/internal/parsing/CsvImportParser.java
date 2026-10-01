package com.vhvkhangg.personalprivatevault.importdata.internal.parsing;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportJobException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parser for CSV import files using Apache Commons CSV.
 */
public final class CsvImportParser {

    private CsvImportParser() {}

    public static List<ImportParsedItem> parse(ImportTargetType targetType, String rawText) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setAllowDuplicateHeaderNames(false)
                .get();

        List<ImportParsedItem> items = new ArrayList<>();
        try (CSVParser parser = CSVParser.parse(new StringReader(rawText), format)) {
            List<String> headerNames = parser.getHeaderNames();
            if (headerNames == null || headerNames.isEmpty()) {
                throw new InvalidImportJobException("Missing header row in CSV import");
            }
            Set<String> seenHeaders = new HashSet<>();
            for (String h : headerNames) {
                if (h == null || h.isBlank()) {
                    throw new InvalidImportJobException("Duplicate or ambiguous header in CSV import");
                }
                String normalized = h.trim().toLowerCase();
                if (!seenHeaders.add(normalized)) {
                    throw new InvalidImportJobException("Duplicate or ambiguous header in CSV import");
                }
            }

            int itemIndex = 0;
            for (CSVRecord record : parser) {
                if (record.size() != headerNames.size()) {
                    items.add(new ImportParsedItem(itemIndex++, null, ImportItemStatus.INVALID, "Inconsistent column count in CSV record"));
                    continue;
                }

                Map<String, Object> raw = new LinkedHashMap<>();
                for (int i = 0; i < headerNames.size(); i++) {
                    String key = headerNames.get(i);
                    String value = record.get(i);
                    if (key != null && !key.isBlank()) {
                        String trimmedKey = key.trim();
                        if (value == null || value.isEmpty()) {
                            raw.put(trimmedKey, null);
                        } else if (isContentField(trimmedKey)) {
                            // Preserve decoded text for content fields (leading/trailing spaces, newlines, markdown)
                            raw.put(trimmedKey, value.isBlank() ? null : value);
                        } else {
                            String trimmed = value.trim();
                            raw.put(trimmedKey, trimmed.isEmpty() ? null : trimmed);
                        }
                    }
                }
                items.add(TargetPayloadCanonicalizer.canonicalize(targetType, itemIndex++, raw));
            }
        } catch (IllegalArgumentException ex) {
            throw new InvalidImportJobException("Duplicate or ambiguous header in CSV import");
        }
        return items;
    }

    private static boolean isContentField(String key) {
        return key.equalsIgnoreCase("contentMarkdown")
                || key.equalsIgnoreCase("content_markdown")
                || key.equalsIgnoreCase("description")
                || key.equalsIgnoreCase("example")
                || key.equalsIgnoreCase("review")
                || key.equalsIgnoreCase("meaning");
    }
}
