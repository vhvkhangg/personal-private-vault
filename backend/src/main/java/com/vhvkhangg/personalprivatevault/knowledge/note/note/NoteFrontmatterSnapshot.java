package com.vhvkhangg.personalprivatevault.knowledge.note.note;

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
 * Centralized policy and utility for isolating and validating note frontmatter JSON structures.
 *
 * <p>Enforces deep defensive isolation across commands, entities, nested views, and parent facade views,
 * preventing dirty-checking mutations from caller or view references. Validates keys, leaves, and cycle
 * prevention consistently, throwing {@link InvalidNoteException} without leaking sensitive payloads.</p>
 */
public final class NoteFrontmatterSnapshot {

    private NoteFrontmatterSnapshot() {}

    /**
     * Creates a detached, fully mutable deep copy of the source frontmatter map suitable for JPA entity storage.
     *
     * @param source the input frontmatter map (may be null)
     * @return a detached mutable copy, or null if source was null
     * @throws InvalidNoteException if the frontmatter contains invalid keys, cycles, or unsupported value types
     */
    public static Map<String, Object> deepCopy(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        return deepCopyMap(source, visited);
    }

    /**
     * Creates a recursively unmodifiable snapshot of the source frontmatter map for safe exposure in public views
     * and command records. Supports JSON null values without NPE.
     *
     * @param source the input frontmatter map (may be null)
     * @return an unmodifiable snapshot, or null if source was null
     * @throws InvalidNoteException if the frontmatter contains invalid keys, cycles, or unsupported value types
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
            throw new InvalidNoteException("Cyclic reference detected in note frontmatter");
        }
        try {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object keyObj = entry.getKey();
                if (keyObj == null) {
                    throw new InvalidNoteException("Frontmatter map contains null key");
                }
                if (!(keyObj instanceof String key)) {
                    throw new InvalidNoteException("Frontmatter map key must be a string");
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
            throw new InvalidNoteException("Cyclic reference detected in note frontmatter");
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
        throw new InvalidNoteException("Unsupported value type in note frontmatter");
    }

    private static Map<String, Object> toUnmodifiableMap(Map<?, ?> map, Set<Object> visited) {
        if (!visited.add(map)) {
            throw new InvalidNoteException("Cyclic reference detected in note frontmatter");
        }
        try {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object keyObj = entry.getKey();
                if (keyObj == null) {
                    throw new InvalidNoteException("Frontmatter map contains null key");
                }
                if (!(keyObj instanceof String key)) {
                    throw new InvalidNoteException("Frontmatter map key must be a string");
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
            throw new InvalidNoteException("Cyclic reference detected in note frontmatter");
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
        throw new InvalidNoteException("Unsupported value type in note frontmatter");
    }

    private static Number validateAndNormalizeNumber(Number num) {
        Class<?> clazz = num.getClass();
        if (clazz == Integer.class || clazz == Long.class || clazz == Double.class
                || clazz == BigDecimal.class || clazz == BigInteger.class
                || clazz == Short.class || clazz == Byte.class || clazz == Float.class) {
            return num;
        }
        throw new InvalidNoteException("Unsupported numeric type in note frontmatter");
    }
}
