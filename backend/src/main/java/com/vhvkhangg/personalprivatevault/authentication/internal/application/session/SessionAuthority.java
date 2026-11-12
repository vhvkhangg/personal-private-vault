package com.vhvkhangg.personalprivatevault.authentication.internal.application.session;

import com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record SessionAuthority(
        UUID sessionId,
        short ownerId,
        LoginProfile activeProfile,
        long generation,
        Long publicCredentialEpoch,
        Instant expiresAt,
        Instant revokedAt,
        long currentCredentialEpoch
) {
    public boolean isValid(Instant now, LoginProfile expectedProfile, long expectedGeneration) {
        if (revokedAt != null) {
            return false;
        }
        if (!now.isBefore(expiresAt)) {
            return false;
        }
        if (ownerId != 1) {
            return false;
        }
        if (activeProfile != expectedProfile) {
            return false;
        }
        if (generation != expectedGeneration) {
            return false;
        }
        if (publicCredentialEpoch != null && !Objects.equals(publicCredentialEpoch, currentCredentialEpoch)) {
            return false;
        }
        return true;
    }
}
