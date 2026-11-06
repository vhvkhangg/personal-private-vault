package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "ppv.drive.enabled=false"
})
class DriveOAuthSecurityTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("When Drive is disabled (default), anonymous request to callback returns 401 and authenticated calls return 404")
    void disabledDriveSecurityBehavior() throws Exception {
        // Anonymous callback returns 401 (not permitted in filter chain)
        mockMvc.perform(get("/api/v1/drive/oauth/callback"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        // Authenticated request to drive routes returns 404 (handlers absent)
        mockMvc.perform(get("/api/v1/drive/connections")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/drive/connections/1/authorize")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Anonymous access to Drive endpoints is rejected with 401")
    void anonymousDriveEndpointsRejectWith401() throws Exception {
        mockMvc.perform(post("/api/v1/drive/connections/1/authorize"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/api/v1/drive/connections"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(delete("/api/v1/drive/connections/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }
}
