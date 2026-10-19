package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/** Bounded immutable issued scope for safe cleanup and reservation release. */
final class RagManifestScope {
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private RagManifestScope() {}

    record Scope(SortedSet<Long> vaultOwners, SortedSet<UUID> externalFiles) {}

    static JsonNode parse(String manifest) {
        try {
            if (manifest == null || manifest.length() > 1_048_576) throw new IllegalArgumentException();
            var value = JSON.readTree(manifest);
            if (!value.isObject()) throw new IllegalArgumentException();
            String contract = value.path("contract_version").asText();
            if ("2.0".equals(contract)) {
                if (value.size() != 4 || !value.path("nodes").isArray() || value.path("nodes").isEmpty()
                        || value.path("nodes").size() > 100 || !value.path("edges").isArray()) {
                    throw new IllegalArgumentException();
                }
                return value;
            } else if ("3.0".equals(contract)) {
                if (!value.path("roots").isArray() || value.path("roots").isEmpty()
                        || value.path("roots").size() > 100) {
                    throw new IllegalArgumentException();
                }
                return value;
            }
            throw new IllegalArgumentException();
        } catch (Exception invalid) { throw new IllegalStateException("Issued source scope is unavailable"); }
    }

    static Scope scope(String manifest) {
        var node = parse(manifest);
        String contract = node.path("contract_version").asText();
        var vaultOwners = new TreeSet<Long>();
        var externalFiles = new TreeSet<UUID>();

        if ("2.0".equals(contract)) {
            for (var item : node.path("nodes")) {
                if (!item.path("source_type").asText().matches("FICTION|FILM")) throw new IllegalStateException("Unsupported issued source");
                long id = Long.parseLong(item.path("domain_id").asText());
                if (id <= 0 || !vaultOwners.add(id)) throw new IllegalStateException("Invalid issued source scope");
            }
        } else if ("3.0".equals(contract)) {
            for (var root : node.path("roots")) {
                if (root.path("manifest").isObject()) {
                    vaultOwners.addAll(scope(root.path("manifest").toString()).vaultOwners());
                    externalFiles.addAll(scope(root.path("manifest").toString()).externalFiles());
                    continue;
                }
                String type = root.path("source_type").asText();
                if ("EXTERNAL_FILE".equals(type)) {
                    try {
                        externalFiles.add(UUID.fromString(root.path("source_id").asText()));
                    } catch (Exception ignored) {}
                } else if ("FICTION".equals(type) || "FILM".equals(type)) {
                    long id = Long.parseLong(root.path("source_id").asText());
                    if (id <= 0 || !vaultOwners.add(id)) throw new IllegalStateException("Invalid issued source scope");
                }
            }
        }
        return new Scope(vaultOwners, externalFiles);
    }

    static SortedSet<Long> owners(String manifest) {
        return scope(manifest).vaultOwners();
    }

    static boolean references(String manifest, long source) {
        var node = parse(manifest);
        String contract = node.path("contract_version").asText();
        if ("2.0".equals(contract)) {
            for (var item : node.path("nodes")) {
                if (item.path("source_id").asLong(-1) == source) return true;
            }
        } else if ("3.0".equals(contract)) {
            for (var root : node.path("roots")) {
                if (root.path("rag_source_id").asLong(-1) == source) return true;
                if (root.path("manifest").isObject() && references(root.path("manifest").toString(), source)) return true;
            }
        }
        return false;
    }
}
