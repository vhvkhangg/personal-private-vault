package com.vhvkhangg.personalprivatevault.reference;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReferenceWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("GET /api/v1/reference/** endpoints require authentication and return reference catalogs")
    void referenceEndpointsReturnCatalogs() throws Exception {
        // Unauthenticated -> 401
        mockMvc.perform(get("/api/v1/reference/countries"))
                .andExpect(status().isUnauthorized());

        // Authenticated -> 200
        mockMvc.perform(get("/api/v1/reference/countries")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/reference/currencies")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/reference/languages")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/reference/platforms")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/reference/story-archetypes")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/reference/world-settings")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("B1-13: Six selected Reference GETs preserve DTO, ascending ordering, detail success and missing 404 for both profiles")
    void selectedReferenceGetsPreserveContractForBothProfiles() throws Exception {
        String tokenConfigured = publicBearerToken();
        String tokenDowngraded = publicBearerTokenViaDowngrade();
        String tokenPrivate = bearerToken();

        for (String token : java.util.List.of(tokenConfigured, tokenDowngraded, tokenPrivate)) {
            String authHeader = "Bearer " + token;

            // 1. Countries list -> 200, ordered by code ascending
            mockMvc.perform(get("/api/v1/reference/countries").header("Authorization", authHeader))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].code").value("US"))
                    .andExpect(jsonPath("$.data[1].code").value("VN"));

            // 2. Country detail -> 200
            mockMvc.perform(get("/api/v1/reference/countries/VN").header("Authorization", authHeader))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.code").value("VN"))
                    .andExpect(jsonPath("$.data.nameEn").value("Vietnam"))
                    .andExpect(jsonPath("$.data.nameVi").value("Việt Nam"));

            // 3. Country missing -> 404
            mockMvc.perform(get("/api/v1/reference/countries/ZZ").header("Authorization", authHeader))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("COUNTRY_NOT_FOUND"))
                    .andExpect(jsonPath("$.error.message").value("Country not found"));

            // 4. Languages list -> 200, ordered by code ascending
            mockMvc.perform(get("/api/v1/reference/languages").header("Authorization", authHeader))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].code").value("en"));

            // 5. Language detail -> 200
            mockMvc.perform(get("/api/v1/reference/languages/en").header("Authorization", authHeader))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.code").value("en"))
                    .andExpect(jsonPath("$.data.nameEn").value("English"))
                    .andExpect(jsonPath("$.data.nameVi").value("Tiếng Anh"));

            // 6. Language missing -> 404
            mockMvc.perform(get("/api/v1/reference/languages/xx").header("Authorization", authHeader))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("LANGUAGE_NOT_FOUND"))
                    .andExpect(jsonPath("$.error.message").value("Language not found"));

            // 7. Currencies list -> 200, ordered by code ascending
            mockMvc.perform(get("/api/v1/reference/currencies").header("Authorization", authHeader))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].code").value("USD"))
                    .andExpect(jsonPath("$.data[1].code").value("VND"));

            // 8. Currency detail -> 200
            mockMvc.perform(get("/api/v1/reference/currencies/USD").header("Authorization", authHeader))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.code").value("USD"))
                    .andExpect(jsonPath("$.data.name").value("US Dollar"))
                    .andExpect(jsonPath("$.data.symbol").value("$"));

            // 9. Currency missing -> 404
            mockMvc.perform(get("/api/v1/reference/currencies/ZZZ").header("Authorization", authHeader))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("CURRENCY_NOT_FOUND"))
                    .andExpect(jsonPath("$.error.message").value("Currency not found"));
        }

        // Anonymous verification for all 6 routes -> 401
        for (String path : java.util.List.of(
                "/api/v1/reference/countries", "/api/v1/reference/countries/VN",
                "/api/v1/reference/languages", "/api/v1/reference/languages/en",
                "/api/v1/reference/currencies", "/api/v1/reference/currencies/USD")) {
            mockMvc.perform(get(path))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
        }
    }

    @Test
    @DisplayName("B1-14: Six unselected Reference GETs deny PUBLIC tokens with 403 ACCESS_DENIED even with populated data, while PRIVATE succeeds")
    void unselectedReferenceGetsDeniedToPublicTokens() throws Exception {
        String tokenConfigured = publicBearerToken();
        String tokenDowngraded = publicBearerTokenViaDowngrade();

        var unselectedPaths = java.util.List.of(
                "/api/v1/reference/platforms",
                "/api/v1/reference/platforms/1",
                "/api/v1/reference/story-archetypes",
                "/api/v1/reference/story-archetypes/1",
                "/api/v1/reference/world-settings",
                "/api/v1/reference/world-settings/1"
        );

        for (String path : unselectedPaths) {
            // Configured PUBLIC token -> 403
            mockMvc.perform(get(path).header("Authorization", "Bearer " + tokenConfigured))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));

            // Downgraded PUBLIC token -> 403
            mockMvc.perform(get(path).header("Authorization", "Bearer " + tokenDowngraded))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"))
                    .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));

            // Anonymous -> 401
            mockMvc.perform(get(path))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
        }

        // PRIVATE token -> lists succeed with 200
        mockMvc.perform(get("/api/v1/reference/platforms").header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
        mockMvc.perform(get("/api/v1/reference/story-archetypes").header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
        mockMvc.perform(get("/api/v1/reference/world-settings").header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
