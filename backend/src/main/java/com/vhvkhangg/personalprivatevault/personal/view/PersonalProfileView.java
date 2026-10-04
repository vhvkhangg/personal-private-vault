package com.vhvkhangg.personalprivatevault.personal.view;

import com.vhvkhangg.personalprivatevault.personal.enums.Gender;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Read model representing an immutable personal profile.
 */
public record PersonalProfileView(
        Long id,
        String name,
        String relationship,
        boolean isSelf,
        Gender gender,
        LocalDate birthDate,
        String nationalityCode,
        String phone,
        String email,
        Long addressId,
        String occupation,
        String notesMarkdown,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
