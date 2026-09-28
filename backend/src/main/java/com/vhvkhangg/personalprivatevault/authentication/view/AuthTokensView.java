package com.vhvkhangg.personalprivatevault.authentication.view;

/**
 * Public immutable view representing issued authentication tokens.
 */
public record AuthTokensView(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
    @Override
    public String toString() {
        return "AuthTokensView[accessToken=[REDACTED], refreshToken=[REDACTED], tokenType="
                + tokenType + ", expiresIn=" + expiresIn + "]";
    }
}
