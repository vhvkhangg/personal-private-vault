package com.vhvkhangg.personalprivatevault.drive.internal.oauth;

import java.time.Instant;

public record DriveIdTokenClaims(
        String sub,
        String email,
        Instant expiresAt,
        String nonce
) {
    @Override public String toString() { return "DriveIdTokenClaims[REDACTED]"; }
}
