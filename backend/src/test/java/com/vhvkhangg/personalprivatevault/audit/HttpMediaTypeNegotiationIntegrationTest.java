package com.vhvkhangg.personalprivatevault.audit;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BA15-5 regression tests:
 * Verifies that unsupported Content-Type and Accept headers return canonical 415 and 406
 * ApiResponses with accurate schema and error codes.
 */
class HttpMediaTypeNegotiationIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("BA15-5: Unsupported Content-Type returns canonical 415 UNSUPPORTED_MEDIA_TYPE with JSON body")
    void unsupportedContentTypeReturns415() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("user:pass"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string(HttpHeaders.CONTENT_TYPE, org.hamcrest.Matchers.containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.error.message").value("Content-Type is not supported"));
    }

    @Test
    @DisplayName("BA15-5: Unsupported Accept header returns canonical 406 NOT_ACCEPTABLE with JSON body")
    void unsupportedAcceptReturns406() throws Exception {
        mockMvc.perform(get("/api/v1/reference/countries")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE))
                .andExpect(status().isNotAcceptable())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string(HttpHeaders.CONTENT_TYPE, org.hamcrest.Matchers.containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("NOT_ACCEPTABLE"))
                .andExpect(jsonPath("$.error.message").value("Acceptable representation cannot be produced"));
    }

    @Test
    @DisplayName("BA15-5: Supported application/json returns successful 200 OK")
    void supportedApplicationJsonReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/reference/countries")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader())
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("BA15-5: Generated OpenAPI documents 415 on request-body operations and 406 on endpoints")
    void openApiDocuments415And406() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.responses['415']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reference/countries'].get.responses['406']").exists());
    }
}
