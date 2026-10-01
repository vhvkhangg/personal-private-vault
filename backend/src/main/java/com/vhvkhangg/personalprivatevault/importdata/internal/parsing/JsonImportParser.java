package com.vhvkhangg.personalprivatevault.importdata.internal.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportJobException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parser for JSON import files using Jackson.
 */
public final class JsonImportParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

    private JsonImportParser() {}

    public static List<ImportParsedItem> parse(ImportTargetType targetType, String rawText) throws IOException {
        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(rawText);
        } catch (JsonProcessingException ex) {
            throw new InvalidImportJobException("Invalid JSON syntax in import file");
        }
        if (root == null || !root.isArray()) {
            throw new InvalidImportJobException("Top-level JSON value must be an array");
        }

        List<ImportParsedItem> items = new ArrayList<>();
        int itemIndex = 0;
        for (JsonNode element : root) {
            if (!element.isObject()) {
                items.add(new ImportParsedItem(itemIndex++, null, ImportItemStatus.INVALID, "Array element must be a JSON object"));
                continue;
            }
            Map<String, Object> rawMap = OBJECT_MAPPER.convertValue(element, new TypeReference<Map<String, Object>>() {});
            items.add(TargetPayloadCanonicalizer.canonicalize(targetType, itemIndex++, rawMap));
        }

        return items;
    }
}
