package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.*;

/** Exact v3 input encoding shared by issuance and transfer; v2 is unaffected. */
public final class RagDocumentInput {
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    private RagDocumentInput() {}

    static List<String> workerPlan(String manifest) {
        try {
            var keys=new ArrayList<String>();
            for(var root:JSON.readTree(manifest).path("roots"))if(!root.has("no_change_generation"))keys.add(root.path("root_key").asText());
            if(keys.isEmpty())throw new IllegalStateException("Nonpublication operation has no worker plan");
            return List.copyOf(keys);
        } catch(java.io.IOException invalid) {throw new IllegalStateException("Invalid admitted roster",invalid);}
    }


    static void validateClosure(RagSourceAuthority.Prepared prepared) {
        boolean external = "EXTERNAL_FILE".equals(prepared.proofs().get(prepared.rootId()).type());
        if (prepared.proofs().values().stream().anyMatch(proof ->
                external != "EXTERNAL_FILE".equals(proof.type())))
            throw new IllegalArgumentException("Document dependencies require the same processing contract as their root");
    }

    static Map<String, Object> approvedRoot(String key, RagSourceAuthority.Prepared prepared) {
        validateClosure(prepared);
        try {
            var proof = prepared.proofs().get(prepared.rootId());
            Map<String, Object> root = new TreeMap<>();
            root.put("root_key", key);
            root.put("source_id", proof.sourceIdentity());
            root.put("source_version", proof.version());
            root.put("ocr_languages", List.of("en"));
            root.put("approved_fields", proof.fields().stream().sorted().toList());

            if ("EXTERNAL_FILE".equals(proof.type())) {
                root.put("format", "MD");
                root.put("snapshot", proof.snapshot());
                String rootText = "";
                Map<String, Map<String, Object>> childTransclusions = new TreeMap<>();
                for (var node : JSON.readTree(prepared.payload()).path("sources")) {
                    long id = node.path("source_id").asLong();
                    if (id == prepared.rootId()) {
                        rootText = node.path("text").asText("");
                    } else {
                        var childProof = prepared.proofs().get(id);
                        String childKey = node.path("id").asText();
                        Map<String, Object> childField = new TreeMap<>();
                        childField.put("source_key", childKey);
                        childField.put("source_version", childProof.version());
                        childField.put("approved_field", "content");
                        childField.put("text", node.path("text").asText(""));
                        if (!"EXTERNAL_FILE".equals(childProof.type()))
                            throw new IllegalArgumentException("Markdown dependencies require independently approved snapshots");
                        childField.put("snapshot", childProof.snapshot());
                        // Transfer each approved S/R once. The accepted D17 source
                        // context resolves authored names/properties in the adapter;
                        // basename collisions must not discard folder-qualified S/R.
                        childTransclusions.put(childKey, childField);
                    }
                }
                root.put("content_text", rootText);
                root.put("content_b64", null);
                root.put("transclusions", childTransclusions);
                root.put("prepared_fields", List.of());
            } else {
                root.put("format", "DOMAIN");
                root.put("content_text", null);
                root.put("content_b64", null);
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
            }
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
