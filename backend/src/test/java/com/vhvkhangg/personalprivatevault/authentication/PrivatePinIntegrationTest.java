package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.ChangePinCommand;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.InvalidPinException;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.PinVerificationException;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.PrivatePinOperations;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.UnauthenticatedAccessException;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrivatePinIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private BootstrapOperations bootstrapOperations;

    @Autowired
    private PrivatePinOperations privatePinOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM app_users");

        bootstrapOperations.bootstrap(new BootstrapCommand(
                "owner@vault.local",
                "owner",
                "SuperSecretPassword123!",
                "123456"
        ));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("1", "credentials");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Fails closed when unauthenticated user attempts PIN verification or change")
    void rejectsUnauthenticatedPinOperations() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> privatePinOperations.verifyPin("123456"))
                .isInstanceOf(UnauthenticatedAccessException.class)
                .hasMessageContaining("Authenticated user context is required");

        assertThatThrownBy(() -> privatePinOperations.changePin(new ChangePinCommand("123456", "654321")))
                .isInstanceOf(UnauthenticatedAccessException.class)
                .hasMessageContaining("Authenticated user context is required");
    }

    @Test
    @DisplayName("Verifies correct and incorrect six-digit PIN under authenticated context")
    void verifiesPinCorrectly() {
        authenticateUser();

        assertThat(privatePinOperations.verifyPin("123456")).isTrue();
        assertThat(privatePinOperations.verifyPin("654321")).isFalse();

        // Format validation
        assertThatThrownBy(() -> privatePinOperations.verifyPin("123"))
                .isInstanceOf(InvalidPinException.class)
                .hasMessageContaining("exactly six decimal digits");
    }

    @Test
    @DisplayName("Changes PIN when current PIN matches, updates pin_hash, and rejects subsequent old PIN")
    void changesPinSuccessfully() {
        authenticateUser();

        // Wrong current PIN
        assertThatThrownBy(() -> privatePinOperations.changePin(new ChangePinCommand("000000", "654321")))
                .isInstanceOf(PinVerificationException.class)
                .hasMessageContaining("Current PIN is incorrect");

        // Invalid new PIN format
        assertThatThrownBy(() -> privatePinOperations.changePin(new ChangePinCommand("123456", "abc")))
                .isInstanceOf(InvalidPinException.class)
                .hasMessageContaining("exactly six decimal digits");

        // Valid change
        privatePinOperations.changePin(new ChangePinCommand("123456", "654321"));

        // New PIN works, old PIN fails
        assertThat(privatePinOperations.verifyPin("654321")).isTrue();
        assertThat(privatePinOperations.verifyPin("123456")).isFalse();
    }
}
