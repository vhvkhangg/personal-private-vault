package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.InvalidBootstrapException;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.bootstrap.BootstrapService;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.AppUser;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BootstrapValidationTest {

    private AppUserRepository appUserRepository;
    private PasswordEncoder passwordEncoder;
    private Clock clock;
    private BootstrapOperations bootstrapOperations;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        clock = Clock.fixed(Instant.parse("2026-09-28T08:00:00Z"), ZoneOffset.UTC);
        when(appUserRepository.existsById(AppUser.SINGLETON_ID)).thenReturn(false);

        bootstrapOperations = new BootstrapService(appUserRepository, null, passwordEncoder, clock);
    }

    @Nested
    @DisplayName("Email validation tests")
    class EmailValidation {

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "plainaddress", "missingatsign.com", "@missingusername.com", "user@.com"})
        @DisplayName("Rejects blank or invalid email formats")
        void rejectsInvalidEmail(String email) {
            BootstrapCommand command = new BootstrapCommand(email, "owner", "validPassword123!", "123456");

            assertThatThrownBy(() -> bootstrapOperations.bootstrap(command))
                    .isInstanceOf(InvalidBootstrapException.class);
        }

        @Test
        @DisplayName("Rejects email exceeding 320 characters")
        void rejectsEmailTooLong() {
            String longEmail = "a".repeat(315) + "@vault.com";
            BootstrapCommand command = new BootstrapCommand(longEmail, "owner", "validPassword123!", "123456");

            assertThatThrownBy(() -> bootstrapOperations.bootstrap(command))
                    .isInstanceOf(InvalidBootstrapException.class)
                    .hasMessageContaining("exceed 320 characters");
        }
    }

    @Nested
    @DisplayName("Username validation tests")
    class UsernameValidation {

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Rejects blank username")
        void rejectsBlankUsername(String username) {
            BootstrapCommand command = new BootstrapCommand("owner@vault.local", username, "validPassword123!", "123456");

            assertThatThrownBy(() -> bootstrapOperations.bootstrap(command))
                    .isInstanceOf(InvalidBootstrapException.class)
                    .hasMessageContaining("Username must not be null or blank");
        }

        @Test
        @DisplayName("Rejects username exceeding 100 characters")
        void rejectsUsernameTooLong() {
            String longUsername = "u".repeat(101);
            BootstrapCommand command = new BootstrapCommand("owner@vault.local", longUsername, "validPassword123!", "123456");

            assertThatThrownBy(() -> bootstrapOperations.bootstrap(command))
                    .isInstanceOf(InvalidBootstrapException.class)
                    .hasMessageContaining("exceed 100 characters");
        }
    }

    @Nested
    @DisplayName("Password validation tests")
    class PasswordValidation {

        @Test
        @DisplayName("Rejects password shorter than 12 characters")
        void rejectsPasswordTooShort() {
            BootstrapCommand command = new BootstrapCommand("owner@vault.local", "owner", "short11char", "123456");

            assertThatThrownBy(() -> bootstrapOperations.bootstrap(command))
                    .isInstanceOf(InvalidBootstrapException.class)
                    .hasMessageContaining("Password must be between 12 and 128 characters");
        }

        @Test
        @DisplayName("Rejects password longer than 128 characters")
        void rejectsPasswordTooLong() {
            String longPassword = "p".repeat(129);
            BootstrapCommand command = new BootstrapCommand("owner@vault.local", "owner", longPassword, "123456");

            assertThatThrownBy(() -> bootstrapOperations.bootstrap(command))
                    .isInstanceOf(InvalidBootstrapException.class)
                    .hasMessageContaining("Password must be between 12 and 128 characters");
        }
    }

    @Nested
    @DisplayName("PIN validation tests")
    class PinValidation {

        @ParameterizedTest
        @ValueSource(strings = {"12345", "1234567", "abcdef", "12345a", "12 456", ""})
        @DisplayName("Rejects PIN that is not exactly six decimal digits")
        void rejectsInvalidPin(String pin) {
            BootstrapCommand command = new BootstrapCommand("owner@vault.local", "owner", "validPassword123!", pin);

            assertThatThrownBy(() -> bootstrapOperations.bootstrap(command))
                    .isInstanceOf(InvalidBootstrapException.class)
                    .hasMessageContaining("PIN must be exactly six decimal digits");
        }
    }
}
