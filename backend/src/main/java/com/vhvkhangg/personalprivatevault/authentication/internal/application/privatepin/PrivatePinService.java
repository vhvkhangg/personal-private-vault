package com.vhvkhangg.personalprivatevault.authentication.internal.application.privatepin;

import com.vhvkhangg.personalprivatevault.authentication.internal.domain.AppUser;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.AppUserRepository;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.ChangePinCommand;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.InvalidPinException;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.PinVerificationException;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.PrivatePinOperations;
import com.vhvkhangg.personalprivatevault.authentication.privatepin.UnauthenticatedAccessException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class PrivatePinService implements PrivatePinOperations {

    private static final Pattern PIN_PATTERN = Pattern.compile("^[0-9]{6}$");

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final EntityManager entityManager;

    @Autowired
    public PrivatePinService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager,
            @Autowired(required = false) Clock clock) {
        this.appUserRepository = Objects.requireNonNull(appUserRepository, "appUserRepository must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder must not be null");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null");
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    public PrivatePinService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            EntityManager entityManager) {
        this(appUserRepository, passwordEncoder, entityManager, Clock.systemUTC());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyPin(String pin) {
        verifyAuthenticatedContext();
        validatePinFormat(pin);

        AppUser user = getSingletonUser();
        return passwordEncoder.matches(pin, user.getPinHash());
    }

    @Override
    @Transactional
    public void changePin(ChangePinCommand command) {
        verifyAuthenticatedContext();
        Objects.requireNonNull(command, "command must not be null");

        validatePinFormat(command.currentPin());
        validatePinFormat(command.newPin());

        AppUser user = lockAndRefreshUser();
        if (!passwordEncoder.matches(command.currentPin(), user.getPinHash())) {
            throw new PinVerificationException("Current PIN is incorrect");
        }

        String newPinHash = passwordEncoder.encode(command.newPin());
        user.updatePinHash(newPinHash, clock.instant());
        appUserRepository.saveAndFlush(user);
    }

    private AppUser lockAndRefreshUser() {
        AppUser user = appUserRepository.findByIdForUpdate(AppUser.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Singleton user is not bootstrapped"));
        entityManager.refresh(user, LockModeType.PESSIMISTIC_WRITE);
        return user;
    }

    private void verifyAuthenticatedContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new UnauthenticatedAccessException("Authenticated user context is required for private PIN operations");
        }
    }

    private void validatePinFormat(String pin) {
        if (pin == null || !PIN_PATTERN.matcher(pin).matches()) {
            throw new InvalidPinException("PIN must be exactly six decimal digits");
        }
    }

    private AppUser getSingletonUser() {
        return appUserRepository.findById(AppUser.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Singleton user is not bootstrapped"));
    }
}
