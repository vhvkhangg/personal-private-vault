package com.vhvkhangg.personalprivatevault.rag.internal.application.qa;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagQaProtocolTest {
    @Test void workerErrorsRetainOnlyAllowlistedCategories() {
        assertThat(supervisor.workerFailureCategory("{\"detail\":{\"category\":\"HEADROOM_UNAVAILABLE\"}}".getBytes(StandardCharsets.UTF_8)))
                .isEqualTo("HEADROOM_UNAVAILABLE");
        for (String body : new String[]{"{\"detail\":\"PRIVATE_ERROR_SENTINEL\"}",
                "{\"detail\":{\"category\":\"PRIVATE_ERROR_SENTINEL\"}}", "malformed"}) {
            assertThat(supervisor.workerFailureCategory(body.getBytes(StandardCharsets.UTF_8)))
                    .isEqualTo("UNCLASSIFIED_FAILURE");
        }
    }
    private final RagQaSupervisor supervisor = new RagQaSupervisor(
            false, false, false, "", "", "", "model.gguf", 4, 4096, 768, false, "", null, null);
    @Test void cudaCannotEnableAnOperationalOrWrongCandidateRuntime() {
        for (boolean testScope : new boolean[]{false, true}) {
            assertThatThrownBy(() -> new RagQaSupervisor(false, false, testScope,
                    "", "", "", "model.gguf", 4, 4096, 768, true, "cuda/python.exe", null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> new RagQaSupervisor(false, false, false, "", "", "",
                "qwen2.5-7b-instruct-q4_k_m-00001-of-00002.gguf", 4, 4096, 768,
                true, "cuda/python.exe", null, null)).isInstanceOf(IllegalArgumentException.class);
    }
    private static final String VALID = """
            {"contract_version":"1.0","reader_id":"reader","process_id":42,"endpoint_nonce":"nonce",
             "scope_digest":"scope","profile":"profile","prompt_profile_hash":"hash",
             "outcome":"ANSWERED","model_version":"model","artifact_hash":"artifact",
             "claims":[{"id":"a","text":"Fact","supports":[{"citation_key":"[1]","quote":"Fact"}]}],
             "unanswered_parts":[],"conflict_pairs":[]}
            """;
    @Test void completeTypedCandidatePassesStructuralBoundary() throws Exception {
        assertThat(supervisor.parseGenerationResponse(VALID.getBytes(StandardCharsets.UTF_8))
                .path("process_id").longValue()).isEqualTo(42);
    }
    @Test void unknownMissingNestedAndCoercedTypesAreRejected() {
        for (String malformed : new String[]{
                VALID.replace("\"contract_version\":", "\"extra\":true,\"contract_version\":"),
                VALID.replace("\"text\":\"Fact\"", "\"extra\":true,\"text\":\"Fact\""),
                VALID.replace("\"quote\":\"Fact\"", "\"quote\":\"Fact\",\"extra\":true"),
                VALID.replace("\"text\":\"Fact\"", "\"text\":true"),
                VALID.replace("\"process_id\":42", "\"process_id\":\"42\""),
                VALID.replace("\"process_id\":42", "\"process_id\":42.0"),
                VALID.replace("\"process_id\":42", "\"process_id\":null"),
                VALID.replace("\"unanswered_parts\":[]", "\"unanswered_parts\":[123]"),
                VALID.replace("\"artifact_hash\":\"artifact\",", ""),
                VALID.replace("\"supports\":[{\"citation_key\":\"[1]\",\"quote\":\"Fact\"}]", "\"supports\":{}"),
                VALID.replace("\"conflict_pairs\":[]", "\"conflict_pairs\":[{\"claim_id_a\":\"a\",\"claim_id_b\":\"b\",\"description\":\"x\",\"extra\":true}]")
        }) {
            assertThatThrownBy(() -> supervisor.parseGenerationResponse(
                    malformed.getBytes(StandardCharsets.UTF_8))).isInstanceOf(RuntimeException.class);
        }
    }
    @Test void duplicateKeysAndTrailingDocumentsAreRejected() {
        for (String malformed : new String[]{
                VALID.replace("\"outcome\":", "\"outcome\":\"ABSTAIN\",\"outcome\":"),
                VALID.replace("\"text\":\"Fact\"", "\"text\":\"Other\",\"text\":\"Fact\""),
                VALID + "{}"
        }) {
            assertThatThrownBy(() -> supervisor.parseGenerationResponse(
                    malformed.getBytes(StandardCharsets.UTF_8))).isInstanceOf(Exception.class);
        }
    }
}

