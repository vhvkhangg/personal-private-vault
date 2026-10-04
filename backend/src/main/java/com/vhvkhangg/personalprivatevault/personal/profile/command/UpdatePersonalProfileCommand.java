package com.vhvkhangg.personalprivatevault.personal.profile.command;

import com.vhvkhangg.personalprivatevault.personal.enums.Gender;

import java.time.LocalDate;

/**
 * Command for updating an existing personal profile.
 */
public record UpdatePersonalProfileCommand(
        Long id,
        String name,
        String relationship,
        Boolean isSelf,
        Gender gender,
        LocalDate birthDate,
        String nationalityCode,
        String phone,
        String email,
        Long addressId,
        String occupation,
        String notesMarkdown
) {
}
