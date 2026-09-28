package com.vhvkhangg.personalprivatevault.authentication.bootstrap;

/**
 * Command for bootstrapping the singleton application user.
 */
public record BootstrapCommand(
        String email,
        String username,
        String password,
        String pin
) {
    @Override
    public String toString() {
        return "BootstrapCommand[email=" + email + ", username=" + username
                + ", password=[REDACTED], pin=[REDACTED]]";
    }
}
