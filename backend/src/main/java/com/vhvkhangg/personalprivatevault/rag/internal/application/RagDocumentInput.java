package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.*;

/** Exact v3 input encoding shared by issuance and transfer; v2 is unaffected. */
public final class RagDocumentInput {
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    private RagDocumentInput() {}

    static Map<String, Object> approvedRoot(String key, RagSourceAuthority.Prepared prepared) {
        try {
            var proof = prepared.proofs().get(prepared.rootId());
            Map<String, Object> root = new TreeMap<>();
            root.put("root_key", key);
            root.put("source_id", Long.toString(proof.ownerId()));
            root.put("source_version", proof.version());
            root.put("format", "DOMAIN");
            root.put("content_text", null);
            root.put("content_b64", null);
            root.put("ocr_languages", List.of("en"));
            root.put("approved_fields", proof.fields().stream().sorted().toList());
            root.put("transclusions", Map.of());
            var fields = new ArrayList<Map<String, Object>>();
            for (var node : JSON.readTree(prepared.payload()).path("sources")) {
                long id = node.path("source_id").asLong();
                var childProof = prepared.proofs().get(id);
                var values = node.path("fields");
                for (String field : childProof.fields().stream().sorted().toList()) {
                    var value = values.path(field);
                    if (value.isNull() || value.isMissingNode()) continue;
                    fields.add(Map.of("source_key", Long.toString(id), "source_version", childProof.version(),
                            "approved_field", field, "text", value.isTextual() ? value.asText() : value.toString()));
                }
            }
            root.put("prepared_fields", fields);
            return root;
        } catch (com.fasterxml.jackson.core.JsonProcessingException invalid) {
            throw new IllegalStateException("Invalid prepared document input", invalid);
        }
    }

    public static String encode(List<Map<String, Object>> roots) {
        try {
            return JSON.writeValueAsString(roots);
        } catch (com.fasterxml.jackson.core.JsonProcessingException invalid) {
            throw new IllegalStateException("Invalid document input encoding", invalid);
        }
    }
}
