package com.vhvkhangg.personalprivatevault.authentication.internal.application.bootstrap;

import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.InvalidBootstrapException;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.UserAlreadyBootstrappedException;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.AppUser;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.AppUserRepository;
import com.vhvkhangg.personalprivatevault.authentication.view.AppUserView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BootstrapService implements BootstrapOperations {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern PIN_PATTERN = Pattern.compile("^[0-9]{6}$");

    private final AppUserRepository appUserRepository;
    private final UserBootstrapper userBootstrapper;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public boolean isBootstrapped() {
        return appUserRepository.existsById(AppUser.SINGLETON_ID);
    }

    @Override
    @Transactional
    public AppUserView bootstrap(BootstrapCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        if (isBootstrapped()) {
            throw new UserAlreadyBootstrappedException("Application user is already bootstrapped");
        }

        String email = validateAndTrimEmail(command.email());
        String username = validateAndTrimUsername(command.username());
        String password = validatePassword(command.password());
        String pin = validatePin(command.pin());

        Instant now = clock.instant();
        String passwordHash = passwordEncoder.encode(password);
        String pinHash = passwordEncoder.encode(pin);

        AppUser user = new AppUser(email, username, passwordHash, pinHash, now);
        AppUser persisted = userBootstrapper.persistInNewTransaction(user);

        return new AppUserView(
                persisted.getId(),
                persisted.getEmail(),
                persisted.getUsername(),
                persisted.getCreatedAt(),
                persisted.getUpdatedAt()
        );
    }

    private String validateAndTrimEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidBootstrapException("Email must not be null or blank");
        }
        String trimmed = email.trim();
        if (trimmed.length() > 320) {
            throw new InvalidBootstrapException("Email must not exceed 320 characters");
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidBootstrapException("Invalid email format: " + trimmed);
        }
        return trimmed;
    }

    private String validateAndTrimUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidBootstrapException("Username must not be null or blank");
        }
        String trimmed = username.trim();
        if (trimmed.length() > 100) {
            throw new InvalidBootstrapException("Username must not exceed 100 characters");
        }
        return trimmed;
    }

    private String validatePassword(String password) {
        if (password == null || password.length() < 12 || password.length() > 128) {
            throw new InvalidBootstrapException("Password must be between 12 and 128 characters");
        }
        return password;
    }

    private String validatePin(String pin) {
        if (pin == null || !PIN_PATTERN.matcher(pin).matches()) {
            throw new InvalidBootstrapException("PIN must be exactly six decimal digits");
        }
        return pin;
    }
}
