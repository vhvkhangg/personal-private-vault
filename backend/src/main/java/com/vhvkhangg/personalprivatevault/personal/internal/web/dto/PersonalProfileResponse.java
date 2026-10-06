package com.vhvkhangg.personalprivatevault.personal.internal.web.dto;

import com.vhvkhangg.personalprivatevault.personal.enums.Gender;

import java.time.Instant;
import java.time.LocalDate;

public record PersonalProfileResponse(
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
) {}
