package com.vhvkhangg.personalprivatevault.importdata.internal.parsing;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportJobException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for Markdown import files extracting YAML frontmatter safely and preserving exact raw markdown content.
 */
public final class MarkdownImportParser {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);

    private MarkdownImportParser() {}

    public static List<ImportParsedItem> parse(
            ImportTargetType targetType,
            String originalFileName,
            String fileHash,
            String rawText
    ) {
        if (targetType != ImportTargetType.NOTE) {
            throw new InvalidImportJobException("Markdown format is only supported for NOTE target");
        }
        if (rawText == null) {
            rawText = "";
        }

        Map<String, Object> frontmatter = Collections.emptyMap();
        String title = null;

        // Frontmatter detection: begins with --- followed by newline
        if (rawText.startsWith("---\n") || rawText.startsWith("---\r\n")) {
            int startIdx = rawText.indexOf('\n') + 1;
            int endIdx = -1;

            // Search for closing delimiter
            int marker = rawText.indexOf("\n---", startIdx);
            if (marker != -1) {
                endIdx = marker;
            } else {
                marker = rawText.indexOf("\r\n---", startIdx);
                if (marker != -1) {
                    endIdx = marker;
                }
            }

            if (endIdx != -1) {
                String yamlContent = rawText.substring(startIdx, endIdx);
                try {
                    Object parsed = YAML_MAPPER.readValue(yamlContent, Object.class);
                    if (parsed != null) {
                        if (!(parsed instanceof Map<?, ?> rawMap)) {
                            return List.of(new ImportParsedItem(0, null, ImportItemStatus.INVALID, "Invalid frontmatter YAML syntax"));
                        }
                        if (!TargetPayloadCanonicalizer.isValidFrontmatter(rawMap)) {
                            return List.of(new ImportParsedItem(0, null, ImportItemStatus.INVALID, "Malformed frontmatter in note item"));
                        }
                        @SuppressWarnings("unchecked")
                        Map<String, Object> castMap = (Map<String, Object>) rawMap;
                        frontmatter = castMap;
                    }
                } catch (Exception ex) {
                    return List.of(new ImportParsedItem(0, null, ImportItemStatus.INVALID, "Invalid frontmatter YAML syntax"));
                }
            }
        }

        Object rawTitle = frontmatter.get("title");
        Object titleToUse = rawTitle;
        if (rawTitle == null || (rawTitle instanceof String s && s.isBlank())) {
            String stem = extractStem(originalFileName);
            titleToUse = (stem != null && !stem.isBlank()) ? stem : "Untitled Note";
        }

        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("title", titleToUse);
        raw.put("contentMarkdown", rawText);
        if (frontmatter.containsKey("summary")) {
            raw.put("summary", frontmatter.get("summary"));
        }
        if (frontmatter.containsKey("sourceName")) {
            raw.put("sourceName", frontmatter.get("sourceName"));
        } else if (frontmatter.containsKey("source_name")) {
            raw.put("source_name", frontmatter.get("source_name"));
        }
        if (frontmatter.containsKey("sourceUrl")) {
            raw.put("sourceUrl", frontmatter.get("sourceUrl"));
        } else if (frontmatter.containsKey("source_url")) {
            raw.put("source_url", frontmatter.get("source_url"));
        }
        raw.put("importedFileName", originalFileName);
        raw.put("importedFileHash", fileHash);
        raw.put("frontmatter", frontmatter);

        return List.of(TargetPayloadCanonicalizer.canonicalize(targetType, 0, raw));
    }

    private static String extractStem(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        String name = fileName.trim();
        int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (lastSlash != -1) {
            name = name.substring(lastSlash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            return name.substring(0, dot);
        }
        return name;
    }
}
