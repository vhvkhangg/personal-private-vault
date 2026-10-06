package com.vhvkhangg.personalprivatevault.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
@Import(ApiResponseEnvelopeAndErrorIntegrationTest.TestDiagnosticController.class)
class ApiResponseEnvelopeAndErrorIntegrationTest extends AbstractWebIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.security.web.access.AccessDeniedHandler accessDeniedHandler;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.security.web.FilterChainProxy filterChainProxy;

    @RestController
    @RequestMapping("/api/v1/test-diagnostic")
    static class TestDiagnosticController {
        @GetMapping("/unexpected")
        public ResponseEntity<?> throwUnexpected() {
            throw new RuntimeException("SYNTHETIC_PRIVATE_EXCEPTION_MESSAGE_12345");
        }

        @GetMapping("/access-denied")
        public ResponseEntity<?> throwAccessDenied() {
            throw new AccessDeniedException("SYNTHETIC_PRIVATE_DENIED_REASON");
        }
    }

    @Test
    @DisplayName("200 OK responses contain explicit data and present nullable error and meta keys")
    void getReturns200WithStandardEnvelope() throws Exception {
        String response = mockMvc.perform(get("/api/v1/reference/countries")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isArray()).isTrue();
        assertThat(root.has("error")).isTrue();
        assertThat(root.get("error").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
    }

    @Test
    @DisplayName("201 Created responses contain explicit data and present nullable error and meta keys")
    void postReturns201WithStandardEnvelope() throws Exception {
        String uniqueTag = "env-test-tag-" + System.currentTimeMillis();
        String response = mockMvc.perform(post("/api/v1/vault/tags")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + uniqueTag + "\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isObject()).isTrue();
        assertThat(root.path("data").path("name").asText()).isEqualTo(uniqueTag);
        assertThat(root.has("error")).isTrue();
        assertThat(root.get("error").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
    }

    @Test
    @DisplayName("Void actions return 200 OK (never 204) with present null data, error, and meta keys")
    void voidActionReturns200WithNullDataEnvelope() throws Exception {
        // Create person
        String personResp = mockMvc.perform(post("/api/v1/people")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Void Action Test Person\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number personId = objectMapper.readTree(personResp).path("data").path("id").numberValue();

        // Assign role returns 200 OK (not 204)
        String assignResp = mockMvc.perform(put("/api/v1/people/{id}/roles", personId)
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"AUTHOR\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(assignResp);
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isNull()).isTrue();
        assertThat(root.has("error")).isTrue();
        assertThat(root.get("error").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
    }

    @Test
    @DisplayName("400 Bad Request on unknown request JSON property (fail-on-unknown-properties)")
    void unknownPropertiesReturn400MalformedRequestBody() throws Exception {
        String response = mockMvc.perform(post("/api/v1/vault/tags")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"valid\", \"unexpectedExtraProperty\": \"fail\"}"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
        assertThat(root.path("error").path("code").asText()).isEqualTo("MALFORMED_REQUEST");
        assertThat(root.path("error").path("fieldErrors").isEmpty()).isTrue();
    }

    @Test
    @DisplayName("400 Bad Request on malformed JSON payload")
    void malformedJsonReturns400MalformedRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/vault/tags")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.error.fieldErrors", empty()));
    }

    @Test
    @DisplayName("400 Bad Request on bean validation failure with fieldErrors populated")
    void validationFailureReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/vault/tags")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.fieldErrors", not(empty())))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.error.fieldErrors[0].message").isNotEmpty());
    }

    @Test
    @DisplayName("401 Unauthorized wire contract when missing bearer token")
    void missingBearerReturnsStructuredJson401() throws Exception {
        String response = mockMvc.perform(get("/api/v1/reference/countries"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
        assertThat(root.path("error").path("code").asText()).isEqualTo("AUTHENTICATION_REQUIRED");
        assertThat(root.path("error").path("message").asText()).isEqualTo("Full authentication is required to access this resource");
        assertThat(root.path("error").path("fieldErrors").isEmpty()).isTrue();
    }

    @Test
    @DisplayName("403 Forbidden wire contract on access denied")
    void accessDeniedReturnsStructuredJson403() throws Exception {
        String response = mockMvc.perform(get("/api/v1/test-diagnostic/access-denied")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
        assertThat(root.path("error").path("code").asText()).isEqualTo("ACCESS_DENIED");
        assertThat(root.path("error").path("message").asText()).isEqualTo("Access is denied");
        assertThat(root.path("error").path("fieldErrors").isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Security filter AccessDeniedHandler writes canonical 403 JSON envelope")
    void securityFilterAccessDeniedHandler_writesCanonical403Envelope() throws Exception {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();

        accessDeniedHandler.handle(request, response, new org.springframework.security.access.AccessDeniedException("Security filter denied"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);

        JsonNode root = objectMapper.readTree(response.getContentAsString());
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
        assertThat(root.path("error").path("code").asText()).isEqualTo("ACCESS_DENIED");
        assertThat(root.path("error").path("message").asText()).isEqualTo("Access is denied");
        assertThat(root.path("error").path("fieldErrors").isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Security filter chain traversal through ExceptionTranslationFilter invokes AccessDeniedHandler writing canonical 403 envelope")
    void securityFilterChainTraversal_invokesAccessDeniedHandlerWritingCanonical403Envelope() throws Exception {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/settings");
        request.addHeader("Authorization", bearerHeader());
        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();

        filterChainProxy.doFilter(request, response, (req, res) -> {
            throw new org.springframework.security.access.AccessDeniedException("Security filter denied");
        });

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);

        JsonNode root = objectMapper.readTree(response.getContentAsString());
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
        assertThat(root.path("error").path("code").asText()).isEqualTo("ACCESS_DENIED");
        assertThat(root.path("error").path("message").asText()).isEqualTo("Access is denied");
        assertThat(root.path("error").path("fieldErrors").isEmpty()).isTrue();
    }

    @Test
    @DisplayName("404 Not Found wire contract when resource is not found without ID concatenation")
    void notFoundReturnsStructuredJson404() throws Exception {
        String response = mockMvc.perform(get("/api/v1/vault/entries/999999999")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
        assertThat(root.path("error").path("code").asText()).isEqualTo("VAULT_ENTRY_NOT_FOUND");
        assertThat(root.path("error").path("message").asText()).isEqualTo("Vault entry not found");
        assertThat(root.path("error").path("message").asText()).doesNotContain("999999999");
        assertThat(root.path("error").path("fieldErrors").isEmpty()).isTrue();
    }

    @Test
    @DisplayName("409 Conflict wire contract on duplicate business identity with privacy-safe message")
    void duplicateConflictReturnsStructuredJson409() throws Exception {
        String groupName = "conflict-group-" + System.currentTimeMillis();
        // First create succeeds
        mockMvc.perform(post("/api/v1/people/creator-groups")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + groupName + "\"}"))
                .andExpect(status().isCreated());

        // Second create with same name fails with 409 and safe static message
        String response = mockMvc.perform(post("/api/v1/people/creator-groups")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + groupName + "\"}"))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
        assertThat(root.path("error").path("code").asText()).isEqualTo("CREATOR_GROUP_NAME_EXISTS");
        assertThat(root.path("error").path("message").asText()).isEqualTo("Creator group name already exists");
        assertThat(root.path("error").path("message").asText()).doesNotContain(groupName);
        assertThat(root.path("error").path("fieldErrors").isEmpty()).isTrue();
    }

    @Test
    @DisplayName("422 Unprocessable Content wire contract on business domain rule violation")
    void businessRuleViolationReturnsStructuredJson422() throws Exception {
        // Change PIN with invalid PIN format (e.g. non-digit "abcdef") returns 422
        String response = mockMvc.perform(put("/api/v1/auth/private-pin")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPin\": \"123456\", \"newPin\": \"abcdef\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
        assertThat(root.path("error").path("code").asText()).isEqualTo("AUTH_INVALID_PIN_FORMAT");
        assertThat(root.path("error").path("message").asText()).isEqualTo("Invalid PIN format");
        assertThat(root.path("error").path("fieldErrors").isEmpty()).isTrue();
    }

    @Test
    @DisplayName("500 Internal Server Error returns generic safe message and logs only structural info without private exception text")
    void unexpectedFailureReturns500WithSafeStructuralLogging(CapturedOutput output) throws Exception {
        String response = mockMvc.perform(get("/api/v1/test-diagnostic/unexpected")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        assertThat(root.has("data")).isTrue();
        assertThat(root.get("data").isNull()).isTrue();
        assertThat(root.has("meta")).isTrue();
        assertThat(root.get("meta").isNull()).isTrue();
        assertThat(root.path("error").path("code").asText()).isEqualTo("INTERNAL_ERROR");
        assertThat(root.path("error").path("message").asText()).isEqualTo("An unexpected error occurred");
        assertThat(root.path("error").path("fieldErrors").isEmpty()).isTrue();

        // Verify private exception message never leaks to response
        assertThat(response).doesNotContain("SYNTHETIC_PRIVATE_EXCEPTION_MESSAGE_12345");

        // Verify private exception message never leaks to logs, and structural logging exists
        assertThat(output.getOut()).doesNotContain("SYNTHETIC_PRIVATE_EXCEPTION_MESSAGE_12345");
        assertThat(output.getOut()).contains("Unexpected server failure: exception=java.lang.RuntimeException");
    }
}
