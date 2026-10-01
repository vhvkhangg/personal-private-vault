package com.vhvkhangg.personalprivatevault.importdata.internal.parsing;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;

import java.util.Map;

/**
 * Result of parsing a single raw item before database persistence.
 */
public record ImportParsedItem(
        int itemIndex,
        Map<String, Object> payload,
        ImportItemStatus status,
        String errorMessage
) {
}
