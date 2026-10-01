package com.vhvkhangg.personalprivatevault.importdata.internal.parsing;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemStatus;
import com.vhvkhangg.personalprivatevault.importdata.enums.ImportTargetType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeInformationType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyStatus;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeStudyType;
import com.vhvkhangg.personalprivatevault.knowledge.api.KnowledgeVocabularyLearningStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Validates structural requirements and canonicalizes parsed raw maps into target Knowledge command payload maps.
 */
public final class TargetPayloadCanonicalizer {

    private TargetPayloadCanonicalizer() {}

    public static ImportParsedItem canonicalize(ImportTargetType targetType, int itemIndex, Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Empty item payload");
        }

        return switch (targetType) {
            case STUDY -> canonicalizeStudy(itemIndex, raw);
            case INFORMATION -> canonicalizeInformation(itemIndex, raw);
            case VOCABULARY -> canonicalizeVocabulary(itemIndex, raw);
            case NOTE -> canonicalizeNote(itemIndex, raw);
        };
    }

    private static ImportParsedItem canonicalizeStudy(int itemIndex, Map<String, Object> raw) {
        Set<String> recognized = Set.of(
                "title", "posterUrl", "poster_url", "type", "siteDomain", "site_domain",
                "youtubeChannelAccountId", "youtube_channel_account_id",
                "authorPersonId", "author_person_id", "authorGroupId", "author_group_id",
                "publishedDate", "published_date", "priceAmount", "price_amount",
                "currencyCode", "currency_code", "description", "url", "review",
                "learningStatus", "learning_status", "progressPercent", "progress_percent",
                "currentProgressText", "current_progress_text"
        );

        for (String key : raw.keySet()) {
            if (!recognized.contains(key)) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Unknown field encountered in item payload");
            }
        }

        try {
            String title = getString(raw, "title");
            if (title == null || title.isBlank()) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Missing required field: title");
            }

            Object rawType = raw.get("type");
            if (rawType == null || (rawType instanceof String s && s.isBlank())) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Missing required field: type");
            }
            KnowledgeStudyType type = parseEnum(KnowledgeStudyType.class, rawType);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("title", title.trim());
            payload.put("posterUrl", getString(raw, "posterUrl", "poster_url"));
            payload.put("type", type.name());
            payload.put("siteDomain", getString(raw, "siteDomain", "site_domain"));

            payload.put("youtubeChannelAccountId", getLong(raw, "youtubeChannelAccountId", "youtube_channel_account_id"));
            payload.put("authorPersonId", getLong(raw, "authorPersonId", "author_person_id"));
            payload.put("authorGroupId", getLong(raw, "authorGroupId", "author_group_id"));
            payload.put("publishedDate", getLocalDate(raw, "publishedDate", "published_date"));
            payload.put("priceAmount", getBigDecimal(raw, "priceAmount", "price_amount"));
            payload.put("currencyCode", getString(raw, "currencyCode", "currency_code"));
            payload.put("description", getString(raw, "description"));
            payload.put("url", getString(raw, "url"));
            payload.put("review", getString(raw, "review"));

            Object rawLearningStatus = getFirst(raw, "learningStatus", "learning_status");
            if (rawLearningStatus != null && !(rawLearningStatus instanceof String s && s.isBlank())) {
                payload.put("learningStatus", parseEnum(KnowledgeStudyStatus.class, rawLearningStatus).name());
            } else {
                payload.put("learningStatus", null);
            }

            payload.put("progressPercent", getBigDecimal(raw, "progressPercent", "progress_percent"));
            payload.put("currentProgressText", getString(raw, "currentProgressText", "current_progress_text"));

            return new ImportParsedItem(itemIndex, payload, ImportItemStatus.VALID, null);
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Invalid field format in study item");
        }
    }

    private static ImportParsedItem canonicalizeInformation(int itemIndex, Map<String, Object> raw) {
        Set<String> recognized = Set.of(
                "title", "type", "description", "contentMarkdown", "content_markdown",
                "example", "sourceName", "source_name", "sourceUrl", "source_url"
        );

        for (String key : raw.keySet()) {
            if (!recognized.contains(key)) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Unknown field encountered in item payload");
            }
        }

        try {
            String title = getString(raw, "title");
            if (title == null || title.isBlank()) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Missing required field: title");
            }

            Object rawType = raw.get("type");
            if (rawType == null || (rawType instanceof String s && s.isBlank())) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Missing required field: type");
            }
            KnowledgeInformationType type = parseEnum(KnowledgeInformationType.class, rawType);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("title", title.trim());
            payload.put("type", type.name());
            payload.put("description", getString(raw, "description"));
            payload.put("contentMarkdown", getString(raw, "contentMarkdown", "content_markdown"));
            payload.put("example", getString(raw, "example"));
            payload.put("sourceName", getString(raw, "sourceName", "source_name"));
            payload.put("sourceUrl", getString(raw, "sourceUrl", "source_url"));

            return new ImportParsedItem(itemIndex, payload, ImportItemStatus.VALID, null);
        } catch (IllegalArgumentException ex) {
            return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Invalid field format in information item");
        }
    }

    private static ImportParsedItem canonicalizeVocabulary(int itemIndex, Map<String, Object> raw) {
        Set<String> recognized = Set.of(
                "word", "languageCode", "language_code", "meaning", "example",
                "pronunciation", "ipa", "partOfSpeech", "part_of_speech",
                "sourceName", "source_name", "sourceUrl", "source_url",
                "learningStatus", "learning_status", "nextReviewAt", "next_review_at",
                "intervalDays", "interval_days", "easeFactor", "ease_factor",
                "repetitionCount", "repetition_count", "lapseCount", "lapse_count"
        );

        for (String key : raw.keySet()) {
            if (!recognized.contains(key)) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Unknown field encountered in item payload");
            }
        }

        try {
            String word = getString(raw, "word");
            if (word == null || word.isBlank()) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Missing required field: word");
            }
            String languageCode = getString(raw, "languageCode", "language_code");
            if (languageCode == null || languageCode.isBlank()) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Missing required field: languageCode");
            }
            String meaning = getString(raw, "meaning");
            if (meaning == null || meaning.isBlank()) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Missing required field: meaning");
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("word", word.trim());
            payload.put("languageCode", languageCode.trim());
            payload.put("meaning", meaning.trim());
            payload.put("example", getString(raw, "example"));
            payload.put("pronunciation", getString(raw, "pronunciation"));
            payload.put("ipa", getString(raw, "ipa"));
            payload.put("partOfSpeech", getString(raw, "partOfSpeech", "part_of_speech"));
            payload.put("sourceName", getString(raw, "sourceName", "source_name"));
            payload.put("sourceUrl", getString(raw, "sourceUrl", "source_url"));

            Object rawLearningStatus = getFirst(raw, "learningStatus", "learning_status");
            if (rawLearningStatus != null && !(rawLearningStatus instanceof String s && s.isBlank())) {
                payload.put("learningStatus", parseEnum(KnowledgeVocabularyLearningStatus.class, rawLearningStatus).name());
            } else {
                payload.put("learningStatus", null);
            }

            payload.put("nextReviewAt", getInstant(raw, "nextReviewAt", "next_review_at"));
            payload.put("intervalDays", getInteger(raw, "intervalDays", "interval_days"));
            payload.put("easeFactor", getBigDecimal(raw, "easeFactor", "ease_factor"));
            payload.put("repetitionCount", getInteger(raw, "repetitionCount", "repetition_count"));
            payload.put("lapseCount", getInteger(raw, "lapseCount", "lapse_count"));

            return new ImportParsedItem(itemIndex, payload, ImportItemStatus.VALID, null);
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Invalid field format in vocabulary item");
        }
    }

    private static ImportParsedItem canonicalizeNote(int itemIndex, Map<String, Object> raw) {
        Set<String> recognized = Set.of(
                "title", "contentMarkdown", "content_markdown", "summary",
                "sourceName", "source_name", "sourceUrl", "source_url",
                "importedFileName", "imported_file_name", "importedFileHash", "imported_file_hash",
                "frontmatter"
        );

        for (String key : raw.keySet()) {
            if (!recognized.contains(key)) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Unknown field encountered in item payload");
            }
        }

        try {
            String title = getString(raw, "title");
            if (title == null || title.isBlank()) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Missing required field: title");
            }
            String contentMarkdown = getString(raw, "contentMarkdown", "content_markdown");
            if (contentMarkdown == null || contentMarkdown.isBlank()) {
                return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Missing required field: contentMarkdown");
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("title", title.trim());
            payload.put("contentMarkdown", contentMarkdown);
            payload.put("summary", getString(raw, "summary"));
            payload.put("sourceName", getString(raw, "sourceName", "source_name"));
            payload.put("sourceUrl", getString(raw, "sourceUrl", "source_url"));
            payload.put("importedFileName", getString(raw, "importedFileName", "imported_file_name"));
            payload.put("importedFileHash", getString(raw, "importedFileHash", "imported_file_hash"));

            if (raw.containsKey("frontmatter")) {
                Object rawFrontmatter = raw.get("frontmatter");
                if (rawFrontmatter == null) {
                    payload.put("frontmatter", Collections.emptyMap());
                } else if (rawFrontmatter instanceof Map<?, ?> map) {
                    if (!isValidFrontmatter(map)) {
                        return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Malformed frontmatter in note item");
                    }
                    Map<String, Object> fm = new LinkedHashMap<>();
                    for (Map.Entry<?, ?> e : map.entrySet()) {
                        fm.put((String) e.getKey(), e.getValue());
                    }
                    payload.put("frontmatter", fm);
                } else {
                    return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Malformed frontmatter in note item");
                }
            } else {
                payload.put("frontmatter", Collections.emptyMap());
            }

            return new ImportParsedItem(itemIndex, payload, ImportItemStatus.VALID, null);
        } catch (IllegalArgumentException ex) {
            return new ImportParsedItem(itemIndex, null, ImportItemStatus.INVALID, "Invalid field format in note item");
        }
    }

    static boolean isValidFrontmatter(Object obj) {
        if (obj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() == null || !(entry.getKey() instanceof String k) || k.isBlank()) {
                    return false;
                }
                if (!isValidFrontmatter(entry.getValue())) {
                    return false;
                }
            }
        } else if (obj instanceof java.util.Collection<?> coll) {
            for (Object item : coll) {
                if (!isValidFrontmatter(item)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Object getFirst(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            if (map.containsKey(k)) {
                return map.get(k);
            }
        }
        return null;
    }

    private static String getString(Map<String, Object> map, String... keys) {
        Object val = getFirst(map, keys);
        if (val == null) {
            return null;
        }
        if (val instanceof Map || val instanceof java.util.Collection || (val != null && val.getClass().isArray())) {
            throw new IllegalArgumentException("Complex structure not allowed for scalar string field");
        }
        String s = val.toString();
        return s.isBlank() ? null : s;
    }

    private static Long getLong(Map<String, Object> map, String... keys) {
        Object val = getFirst(map, keys);
        if (val == null) {
            return null;
        }
        if (val instanceof Map || val instanceof java.util.Collection) {
            throw new IllegalArgumentException("Complex structure not allowed for integer field");
        }
        if (val instanceof Long l) {
            return l;
        }
        if (val instanceof Integer i) {
            return i.longValue();
        }
        if (val instanceof Short s) {
            return s.longValue();
        }
        if (val instanceof Byte b) {
            return b.longValue();
        }
        if (val instanceof java.math.BigInteger bi) {
            if (bi.compareTo(java.math.BigInteger.valueOf(Long.MIN_VALUE)) < 0 || bi.compareTo(java.math.BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                throw new IllegalArgumentException("Integer overflow for long field");
            }
            return bi.longValue();
        }
        if (val instanceof BigDecimal bd) {
            try {
                return bd.longValueExact();
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Fractional value not allowed for integer field");
            }
        }
        if (val instanceof Number n) {
            double d = n.doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d) || Math.floor(d) != d) {
                throw new IllegalArgumentException("Fractional value not allowed for integer field");
            }
            if (d < Long.MIN_VALUE || d > Long.MAX_VALUE) {
                throw new IllegalArgumentException("Integer overflow for long field");
            }
            return (long) d;
        }
        String s = val.toString().trim();
        if (s.isEmpty()) {
            return null;
        }
        return Long.parseLong(s);
    }

    private static Integer getInteger(Map<String, Object> map, String... keys) {
        Long l = getLong(map, keys);
        if (l == null) {
            return null;
        }
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Integer overflow for int field");
        }
        return l.intValue();
    }

    private static BigDecimal getBigDecimal(Map<String, Object> map, String... keys) {
        Object val = getFirst(map, keys);
        if (val == null) {
            return null;
        }
        if (val instanceof Map || val instanceof java.util.Collection) {
            throw new IllegalArgumentException("Complex structure not allowed for decimal field");
        }
        if (val instanceof BigDecimal bd) {
            return bd;
        }
        if (val instanceof Long || val instanceof Integer || val instanceof Short || val instanceof Byte) {
            return BigDecimal.valueOf(((Number) val).longValue());
        }
        if (val instanceof java.math.BigInteger bi) {
            return new BigDecimal(bi);
        }
        if (val instanceof CharSequence cs) {
            String s = cs.toString().trim();
            if (s.isEmpty()) {
                return null;
            }
            return new BigDecimal(s);
        }
        if (val instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        throw new IllegalArgumentException("Invalid type for decimal field");
    }

    private static String getLocalDate(Map<String, Object> map, String... keys) {
        Object val = getFirst(map, keys);
        if (val == null) {
            return null;
        }
        if (val instanceof Map || val instanceof java.util.Collection) {
            throw new IllegalArgumentException("Complex structure not allowed for date field");
        }
        if (val instanceof LocalDate ld) {
            return ld.toString();
        }
        String s = val.toString().trim();
        if (s.isEmpty()) {
            return null;
        }
        return LocalDate.parse(s).toString();
    }

    private static String getInstant(Map<String, Object> map, String... keys) {
        Object val = getFirst(map, keys);
        if (val == null) {
            return null;
        }
        if (val instanceof Map || val instanceof java.util.Collection) {
            throw new IllegalArgumentException("Complex structure not allowed for instant field");
        }
        if (val instanceof Instant i) {
            return i.toString();
        }
        String s = val.toString().trim();
        if (s.isEmpty()) {
            return null;
        }
        return Instant.parse(s).toString();
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E parseEnum(Class<E> enumClass, Object val) {
        if (enumClass.isInstance(val)) {
            return (E) val;
        }
        String s = val.toString().trim();
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(s)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Unknown enum: " + s);
    }
}
