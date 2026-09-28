package com.vhvkhangg.personalprivatevault.authentication.internal.application.bootstrap;

import com.vhvkhangg.personalprivatevault.authentication.bootstrap.UserAlreadyBootstrappedException;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.AppUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Internal transaction-isolated helper for singleton user insertion.
 *
 * <p>Isolates user persistence in a separate transaction so that concurrent singleton or unique
 * constraint conflicts can roll back cleanly without poisoning the caller's transaction context.</p>
 */
@Component
@RequiredArgsConstructor
class UserBootstrapper {

    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AppUser persistInNewTransaction(AppUser user) {
        try {
            entityManager.persist(user);
            entityManager.flush();
            return user;
        } catch (DataIntegrityViolationException | PersistenceException ex) {
            throw new UserAlreadyBootstrappedException("Application user is already bootstrapped");
        }
    }
}
