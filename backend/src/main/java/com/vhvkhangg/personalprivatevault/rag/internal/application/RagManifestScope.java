package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.SortedSet;
import java.util.TreeSet;

/** Bounded immutable issued scope for safe cleanup and reservation release. */
final class RagManifestScope {
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private RagManifestScope() {}
    static JsonNode parse(String manifest) {
        try {
            if (manifest == null || manifest.length() > 1_048_576) throw new IllegalArgumentException();
            var value = JSON.readTree(manifest);
            if (!value.isObject() || value.size() != 4 || !"2.0".equals(value.path("contract_version").asText())
                    || !value.path("nodes").isArray() || value.path("nodes").isEmpty()
                    || value.path("nodes").size() > 100 || !value.path("edges").isArray()) throw new IllegalArgumentException();
            return value;
        } catch (Exception invalid) { throw new IllegalStateException("Issued source scope is unavailable"); }
    }
    static SortedSet<Long> owners(String manifest) {
        var ids = new TreeSet<Long>();
        for (var node : parse(manifest).path("nodes")) {
            if (!node.path("source_type").asText().matches("FICTION|FILM")) throw new IllegalStateException("Unsupported issued source");
            long id = Long.parseLong(node.path("domain_id").asText());
            if (id <= 0 || !ids.add(id)) throw new IllegalStateException("Invalid issued source scope");
        }
        return ids;
    }
    static boolean references(String manifest, long source) {
        for (var node : parse(manifest).path("nodes")) if (node.path("source_id").asLong(-1) == source) return true;
        return false;
    }
}
