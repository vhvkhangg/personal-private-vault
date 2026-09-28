package com.vhvkhangg.personalprivatevault.authentication.session;

/**
 * Command for user login by username or email.
 */
public record LoginCommand(
        String identifier,
        String password
) {
    @Override
    public String toString() {
        return "LoginCommand[identifier=" + identifier + ", password=[REDACTED]]";
    }
}
