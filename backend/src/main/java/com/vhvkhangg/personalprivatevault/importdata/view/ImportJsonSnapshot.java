package com.vhvkhangg.personalprivatevault.importdata.view;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility for isolating and deeply validating parsed import JSON payloads.
 *
 * <p>Enforces deep defensive isolation across commands, entities, and public views,
 * preventing dirty-checking mutations from caller or view references. Validates keys, leaves, and cycle
 * prevention consistently, throwing {@link InvalidImportJsonException} without leaking sensitive payloads.</p>
 */
public final class ImportJsonSnapshot {

    private ImportJsonSnapshot() {}

    /**
     * Creates a detached, fully mutable deep copy of the source JSON map suitable for JPA entity storage.
     *
     * @param source the input JSON map (may be null)
     * @return a detached mutable copy, or null if source was null
     * @throws InvalidImportJsonException if the JSON contains invalid keys, cycles, or unsupported value types
     */
    public static Map<String, Object> deepCopy(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        return deepCopyMap(source, visited);
    }

    /**
     * Creates a recursively unmodifiable snapshot of the source JSON map for safe exposure in public views
     * and command records. Supports JSON null values without NPE.
     *
     * @param source the input JSON map (may be null)
     * @return an unmodifiable snapshot, or null if source was null
     * @throws InvalidImportJsonException if the JSON contains invalid keys, cycles, or unsupported value types
     */
    public static Map<String, Object> toUnmodifiableSnapshot(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        return toUnmodifiableMap(source, visited);
    }

    private static Map<String, Object> deepCopyMap(Map<?, ?> map, Set<Object> visited) {
        if (!visited.add(map)) {
            throw new InvalidImportJsonException("Cyclic reference detected in import JSON payload");
        }
        try {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object keyObj = entry.getKey();
                if (keyObj == null) {
                    throw new InvalidImportJsonException("Import JSON map contains null key");
                }
                if (!(keyObj instanceof String key)) {
                    throw new InvalidImportJsonException("Import JSON map key must be a string");
                }
                copy.put(key, deepCopyValue(entry.getValue(), visited));
            }
            return copy;
        } finally {
            visited.remove(map);
        }
    }

    private static List<Object> deepCopyList(Collection<?> col, Set<Object> visited) {
        if (!visited.add(col)) {
            throw new InvalidImportJsonException("Cyclic reference detected in import JSON payload");
        }
        try {
            List<Object> copy = new ArrayList<>(col.size());
            for (Object elem : col) {
                copy.add(deepCopyValue(elem, visited));
            }
            return copy;
        } finally {
            visited.remove(col);
        }
    }

    private static Object deepCopyValue(Object value, Set<Object> visited) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof AtomicBoolean ab) {
            return Boolean.valueOf(ab.get());
        }
        if (value instanceof AtomicInteger ai) {
            return Integer.valueOf(ai.get());
        }
        if (value instanceof AtomicLong al) {
            return Long.valueOf(al.get());
        }
        if (value instanceof Number num) {
            return validateAndNormalizeNumber(num);
        }
        if (value instanceof Map<?, ?> m) {
            return deepCopyMap(m, visited);
        }
        if (value instanceof Collection<?> c) {
            return deepCopyList(c, visited);
        }
        throw new InvalidImportJsonException("Unsupported value type in import JSON payload");
    }

    private static Map<String, Object> toUnmodifiableMap(Map<?, ?> map, Set<Object> visited) {
        if (!visited.add(map)) {
            throw new InvalidImportJsonException("Cyclic reference detected in import JSON payload");
        }
        try {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object keyObj = entry.getKey();
                if (keyObj == null) {
                    throw new InvalidImportJsonException("Import JSON map contains null key");
                }
                if (!(keyObj instanceof String key)) {
                    throw new InvalidImportJsonException("Import JSON map key must be a string");
                }
                copy.put(key, toUnmodifiableValue(entry.getValue(), visited));
            }
            return Collections.unmodifiableMap(copy);
        } finally {
            visited.remove(map);
        }
    }

    private static List<Object> toUnmodifiableList(Collection<?> col, Set<Object> visited) {
        if (!visited.add(col)) {
            throw new InvalidImportJsonException("Cyclic reference detected in import JSON payload");
        }
        try {
            List<Object> copy = new ArrayList<>(col.size());
            for (Object elem : col) {
                copy.add(toUnmodifiableValue(elem, visited));
            }
            return Collections.unmodifiableList(copy);
        } finally {
            visited.remove(col);
        }
    }

    private static Object toUnmodifiableValue(Object value, Set<Object> visited) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof AtomicBoolean ab) {
            return Boolean.valueOf(ab.get());
        }
        if (value instanceof AtomicInteger ai) {
            return Integer.valueOf(ai.get());
        }
        if (value instanceof AtomicLong al) {
            return Long.valueOf(al.get());
        }
        if (value instanceof Number num) {
            return validateAndNormalizeNumber(num);
        }
        if (value instanceof Map<?, ?> m) {
            return toUnmodifiableMap(m, visited);
        }
        if (value instanceof Collection<?> c) {
            return toUnmodifiableList(c, visited);
        }
        throw new InvalidImportJsonException("Unsupported value type in import JSON payload");
    }

    private static Number validateAndNormalizeNumber(Number num) {
        Class<?> clazz = num.getClass();
        if (clazz == Integer.class || clazz == Long.class || clazz == Double.class
                || clazz == BigDecimal.class || clazz == BigInteger.class
                || clazz == Short.class || clazz == Byte.class || clazz == Float.class) {
            return num;
        }
        throw new InvalidImportJsonException("Unsupported numeric type in import JSON payload");
    }
}
