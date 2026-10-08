package com.vhvkhangg.personalprivatevault.audit;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BA15-3 regression tests:
 * Verifies standard full-input encoding for new passwords up to 128 characters,
 * no silent 72-byte truncation, HTTP boundaries (12, 128, multibyte > 72 bytes),
 * rejection of outside-range passwords (<12 and >128), and backward compatibility
 * with legacy bcrypt hashes via HTTP login.
 */
class PasswordRangeIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("BA15-3: Newly encoded passwords use PBKDF2 standard full-input encoding")
    void newlyEncodedPasswordsUsePbkdf2() {
        String encoded = passwordEncoder.encode("ValidPassword123!");
        assertThat(encoded).startsWith("{pbkdf2}");
        assertThat(passwordEncoder.matches("ValidPassword123!", encoded)).isTrue();
        assertThat(passwordEncoder.matches("WrongPassword123!", encoded)).isFalse();
    }

    @Test
    @DisplayName("BA15-3: Validates ASCII passwords at length boundaries (12, 72, 73, 128)")
    void supportsFullRangeOfAsciiLengths() {
        // 12 chars
        String len12 = "A".repeat(11) + "!";
        String enc12 = passwordEncoder.encode(len12);
        assertThat(passwordEncoder.matches(len12, enc12)).isTrue();

        // 72 chars
        String len72 = "A".repeat(71) + "!";
        String enc72 = passwordEncoder.encode(len72);
        assertThat(passwordEncoder.matches(len72, enc72)).isTrue();

        // 73 chars
        String len73 = "A".repeat(72) + "!";
        String enc73 = passwordEncoder.encode(len73);
        assertThat(passwordEncoder.matches(len73, enc73)).isTrue();

        // 128 chars
        String len128 = "A".repeat(127) + "!";
        String enc128 = passwordEncoder.encode(len128);
        assertThat(passwordEncoder.matches(len128, enc128)).isTrue();
    }

    @Test
    @DisplayName("BA15-3: Full-input distinction without 72-byte truncation")
    void fullInputDistinctionBeyond72Bytes() {
        // 73-character passwords differing only at index 72 (the 73rd character)
        String passA = "A".repeat(72) + "X";
        String passB = "A".repeat(72) + "Y";

        String encodedA = passwordEncoder.encode(passA);
        assertThat(passwordEncoder.matches(passA, encodedA)).isTrue();
        // Crucial test: in bcrypt, characters past byte 72 are silently ignored.
        // In our full-input PBKDF2 encoder, passB MUST NOT match encodedA!
        assertThat(passwordEncoder.matches(passB, encodedA)).isFalse();
    }

    @Test
    @DisplayName("BA15-3: Multibyte passwords exceeding 72 bytes are correctly handled without truncation")
    void multibytePasswordsBeyond72Bytes() {
        // Japanese character "あ" is 3 bytes in UTF-8. 30 chars = 90 bytes (> 72 bytes).
        String multiByteA = "あ".repeat(30) + "1";
        String multiByteB = "あ".repeat(30) + "2";

        String encoded = passwordEncoder.encode(multiByteA);
        assertThat(passwordEncoder.matches(multiByteA, encoded)).isTrue();
        assertThat(passwordEncoder.matches(multiByteB, encoded)).isFalse();
    }

    @Test
    @DisplayName("BA15-3: Legacy bcrypt hashes remain fully verifiable")
    void legacyBcryptHashesAreVerifiable() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        String rawPassword = "LegacyPassword123!";
        String legacyHash = bcrypt.encode(rawPassword);

        // Verification through DelegatingPasswordEncoder matches bcrypt hashes
        assertThat(passwordEncoder.matches(rawPassword, legacyHash)).isTrue();
        assertThat(passwordEncoder.matches("WrongPassword", legacyHash)).isFalse();

        // Prefixed bcrypt hash
        String prefixedBcrypt = "{bcrypt}" + legacyHash;
        assertThat(passwordEncoder.matches(rawPassword, prefixedBcrypt)).isTrue();
    }

    @Test
    @DisplayName("BA15-3: HTTP bootstrap and login at password boundaries (12, 128 chars, multibyte > 72 bytes)")
    void httpBootstrapAndLoginAtPasswordBoundaries() throws Exception {
        // 12-character password
        String pass12 = "A".repeat(11) + "!";
        String payload12 = """
                {
                  "username": "user12",
                  "email": "user12@example.com",
                  "password": "%s",
                  "pin": "123456"
                }
                """.formatted(pass12);
        mockMvc.perform(post("/api/v1/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload12))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("user12"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "user12",
                                  "password": "%s"
                                }
                                """.formatted(pass12)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString());

        cleanUpDatabase();

        // 128-character password
        String pass128 = "B".repeat(127) + "!";
        String payload128 = """
                {
                  "username": "user128",
                  "email": "user128@example.com",
                  "password": "%s",
                  "pin": "123456"
                }
                """.formatted(pass128);
        mockMvc.perform(post("/api/v1/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload128))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("user128"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "user128",
                                  "password": "%s"
                                }
                                """.formatted(pass128)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString());

        cleanUpDatabase();

        // Multibyte > 72 bytes (30 chars of "あ" = 90 bytes)
        String passMulti = "あ".repeat(30) + "1";
        String payloadMulti = """
                {
                  "username": "usermulti",
                  "email": "usermulti@example.com",
                  "password": "%s",
                  "pin": "123456"
                }
                """.formatted(passMulti);
        mockMvc.perform(post("/api/v1/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadMulti))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("usermulti"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "usermulti",
                                  "password": "%s"
                                }
                                """.formatted(passMulti)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString());
    }

    @Test
    @DisplayName("BA15-3: HTTP rejects passwords outside 12-128 range (<12 and >128)")
    void httpRejectsPasswordsOutside12To128Range() throws Exception {
        // 11 characters (< 12) -> domain validation triggers AUTH_INVALID_BOOTSTRAP 422
        String pass11 = "A".repeat(10) + "!";
        String payload11 = """
                {
                  "username": "user11",
                  "email": "user11@example.com",
                  "password": "%s",
                  "pin": "123456"
                }
                """.formatted(pass11);
        mockMvc.perform(post("/api/v1/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload11))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_BOOTSTRAP"));

        // 129 characters (> 128) -> DTO validation triggers VALIDATION_ERROR 400
        String pass129 = "A".repeat(128) + "!";
        String payload129 = """
                {
                  "username": "user129",
                  "email": "user129@example.com",
                  "password": "%s",
                  "pin": "123456"
                }
                """.formatted(pass129);
        mockMvc.perform(post("/api/v1/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload129))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("BA15-3: HTTP legacy bcrypt password login succeeds for existing user")
    void httpLegacyBcryptLoginSucceeds() throws Exception {
        String bootstrapPayload = """
                {
                  "username": "legacyuser",
                  "email": "legacy@example.com",
                  "password": "initial-pass-123456",
                  "pin": "123456"
                }
                """;
        mockMvc.perform(post("/api/v1/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bootstrapPayload))
                .andExpect(status().isCreated());

        // Update database user password directly to a legacy bcrypt hash (simulating pre-existing account)
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        String legacyPassword = "LegacyBcryptPassword123!";
        String legacyHash = bcrypt.encode(legacyPassword);
        jdbcTemplate.update("UPDATE app_users SET password_hash = ? WHERE username = 'legacyuser'", legacyHash);

        // Login using the raw password against legacy bcrypt hash succeeds over HTTP
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "legacyuser",
                                  "password": "%s"
                                }
                                """.formatted(legacyPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString());
    }
}
