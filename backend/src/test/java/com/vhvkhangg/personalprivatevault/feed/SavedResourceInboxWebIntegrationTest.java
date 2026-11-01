package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
class SavedResourceInboxWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("GET /api/v1/saved-resources/inbox without bearer token returns 401 UNAUTHORIZED")
    void unauthenticatedInboxReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/saved-resources/inbox"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/saved-resources/inbox with invalid bearer token returns 401 UNAUTHORIZED")
    void invalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .header("Authorization", "Bearer invalid-token-value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authenticated GET /api/v1/saved-resources/inbox returns 200 with standard ApiResponse envelope")
    void authenticatedInboxReturns200WithEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.meta").value(nullValue()));
    }

    @Test
    @DisplayName("Limit validation errors return 400 VALIDATION_ERROR for limit 0, negative, or > 100")
    void limitValidationErrors() throws Exception {
        // limit = 0
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("limit", "0")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // limit = -5
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("limit", "-5")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // limit = 101
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("limit", "101")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Malformed request parameter returns 400 MALFORMED_REQUEST for non-numeric limit or invalid kind")
    void malformedParameterErrors() throws Exception {
        // Non-numeric limit
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("limit", "not-a-number")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));

        // Invalid enum kind
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "UNKNOWN_NONEXISTENT_KIND")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));
    }

    @Test
    @DisplayName("Invalid or blank cursor returns 400 INVALID_ARGUMENT")
    void invalidCursorErrors() throws Exception {
        // Blank cursor
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("cursor", "   ")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.error.message").value("Invalid argument provided"));

        // Malformed base64 cursor
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("cursor", "%%%invalid-encoding%%%")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));

        // Oversized cursor
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("cursor", "x".repeat(513))
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));

        // Filter mismatch: cursor encoded with ARTICLE used with kind=REPOSITORY
        String articlePayload = "v1|ARTICLE|" + Instant.now() + "|1";
        String articleCursor = Base64.getUrlEncoder().withoutPadding().encodeToString(articlePayload.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "REPOSITORY")
                        .param("cursor", articleCursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));

        // Out of range timestamp (year > 9999)
        String futurePayload = "v1|ARTICLE|+10000-01-01T00:00:00Z|1";
        String futureCursor = Base64.getUrlEncoder().withoutPadding().encodeToString(futurePayload.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "ARTICLE")
                        .param("cursor", futureCursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));

        // Out of range timestamp (year < 0001)
        String pastPayload = "v1|ARTICLE|0000-01-01T00:00:00Z|1";
        String pastCursor = Base64.getUrlEncoder().withoutPadding().encodeToString(pastPayload.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "ARTICLE")
                        .param("cursor", pastCursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));

        // Overflow ID
        String overflowPayload = "v1|ARTICLE|2026-11-01T10:00:00Z|999999999999999999999999";
        String overflowCursor = Base64.getUrlEncoder().withoutPadding().encodeToString(overflowPayload.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "ARTICLE")
                        .param("cursor", overflowCursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));

        // Non-positive ID
        String zeroIdPayload = "v1|ARTICLE|2026-11-01T10:00:00Z|0";
        String zeroIdCursor = Base64.getUrlEncoder().withoutPadding().encodeToString(zeroIdPayload.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "ARTICLE")
                        .param("cursor", zeroIdCursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));

        // Unsupported version
        String v2Payload = "v2|ARTICLE|2026-11-01T10:00:00Z|1";
        String v2Cursor = Base64.getUrlEncoder().withoutPadding().encodeToString(v2Payload.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "ARTICLE")
                        .param("cursor", v2Cursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));
    }

    @Test
    @DisplayName("Valid edited tuple seek position returns 200 OK")
    void validEditedTupleSeekPositionReturns200() throws Exception {
        String editedPayload = "v1|ARTICLE|2026-11-01T10:00:00Z|99999";
        String editedCursor = Base64.getUrlEncoder().withoutPadding().encodeToString(editedPayload.getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "ARTICLE")
                        .param("cursor", editedCursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("HTTP keyset pagination flow returns non-null cursor then terminal null cursor")
    void httpKeysetPaginationAndTerminalNullCursor() throws Exception {
        for (int i = 0; i < 3; i++) {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "kind", "ARTICLE",
                    "title", "Page Item " + i,
                    "resourceUrl", "https://example.com/page-item-" + i + "-" + System.currentTimeMillis()
            ));
            mockMvc.perform(post("/api/v1/saved-resources/manual")
                            .header("Authorization", bearerHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated());
            Thread.sleep(10);
        }

        // Page 1 with limit 2: returns 2 items and a non-null nextCursor
        String firstPageResponse = mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "ARTICLE")
                        .param("limit", "2")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.nextCursor", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        String nextCursor = objectMapper.readTree(firstPageResponse).path("data").path("nextCursor").asText();
        assertThat(nextCursor).isNotBlank();

        // Page 2 with cursor: returns remaining 1 item and terminal null cursor
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "ARTICLE")
                        .param("limit", "2")
                        .param("cursor", nextCursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()));
    }

    @Test
    @DisplayName("Empty inbox query returns empty array and null nextCursor")
    void emptyInboxPageReturnsNullCursorAndEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "REPOSITORY")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", empty()))
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()));
    }

    @Test
    @DisplayName("Valid boundary limits 1, 50, and 100 return 200 OK")
    void boundaryLimitsAccepted() throws Exception {
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("limit", "1")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("limit", "50")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("limit", "100")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Untrusted string data round-trips safely as pure JSON data without execution or leaking credentials")
    void untrustedStringRoundTripsSafely() throws Exception {
        String xssTitle = "<script>alert('XSS')</script> & \"quotes\" 'single'";
        String xssUrl = "https://example.com/safe-path?param=test&tag=1#frag";
        String payload = objectMapper.writeValueAsString(Map.of(
                "kind", "ARTICLE",
                "title", xssTitle,
                "resourceUrl", xssUrl
        ));

        mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value(xssTitle))
                .andExpect(jsonPath("$.data.resourceUrl").value(xssUrl));

        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "ARTICLE")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].title").value(xssTitle))
                .andExpect(jsonPath("$.data.items[0].resourceUrl").value(xssUrl));
    }

    @Test
    @DisplayName("Nested untrusted metadata round-trips safely without execution or data leakage")
    void untrustedStringWithNestedMetadataRoundTripsSafely() throws Exception {
        Map<String, Object> nestedMetadata = Map.of(
                "nestedScript", "<script>alert('nested')</script>",
                "sampleKey", "sampleValue"
        );
        String payload = objectMapper.writeValueAsString(Map.of(
                "kind", "ARTICLE",
                "title", "Nested Metadata Resource",
                "resourceUrl", "https://example.com/nested-" + System.currentTimeMillis(),
                "rawMetadata", nestedMetadata
        ));

        mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "ARTICLE")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].rawMetadata.nestedScript").value("<script>alert('nested')</script>"));
    }

    @Test
    @DisplayName("Error messages and logs do not leak credentials, secret markers, or SQL")
    void errorMessagesDoNotLeakCredentialsOrSql(CapturedOutput output) throws Exception {
        String secretToken = "SECRET_TOKEN_XYZ_98765";
        String invalidCursorPayload = "v1|ARTICLE|2026-11-01T12:00:00Z|" + secretToken;
        String invalidCursor = Base64.getUrlEncoder().withoutPadding().encodeToString(invalidCursorPayload.getBytes(StandardCharsets.UTF_8));

        String responseBody = mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "ARTICLE")
                        .param("cursor", invalidCursor)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Invalid argument provided"))
                .andReturn().getResponse().getContentAsString();

        assertThat(responseBody).doesNotContain(secretToken);
        assertThat(responseBody).doesNotContain(invalidCursor);
        assertThat(output.getAll()).doesNotContain(secretToken);
        assertThat(output.getAll()).doesNotContain(invalidCursor);
        assertThat(output.getAll()).doesNotContain("SELECT ");
    }

    @Test
    @DisplayName("Existing manual URL acceptance handles non-HTTP scheme without fetching or redirecting")
    void nonHttpSchemeManualUrlAcceptedSafely() throws Exception {
        String nonHttpUrl = "gopher://gopher.floodgap.com/1/world-" + System.currentTimeMillis();
        String payload = """
                {
                  "kind": "OTHER",
                  "title": "Gopher Resource",
                  "resourceUrl": "%s"
                }
                """.formatted(nonHttpUrl);

        mockMvc.perform(post("/api/v1/saved-resources/manual")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.resourceUrl").value(nonHttpUrl));

        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .param("kind", "OTHER")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].resourceUrl").value(nonHttpUrl));
    }

    @Test
    @DisplayName("Literal /inbox route does not collide with /{id} path variable routing")
    void literalInboxRoutingTakesPrecedence() throws Exception {
        mockMvc.perform(get("/api/v1/saved-resources/inbox")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()));
    }
}
