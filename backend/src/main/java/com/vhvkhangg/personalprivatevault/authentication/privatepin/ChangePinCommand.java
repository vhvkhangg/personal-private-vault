package com.vhvkhangg.personalprivatevault.authentication.privatepin;

/**
 * Command for changing the private-mode PIN.
 */
public record ChangePinCommand(
        String currentPin,
        String newPin
) {
    @Override
    public String toString() {
        return "ChangePinCommand[currentPin=[REDACTED], newPin=[REDACTED]]";
    }
}
