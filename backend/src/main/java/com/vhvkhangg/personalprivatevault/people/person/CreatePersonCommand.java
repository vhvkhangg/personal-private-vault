package com.vhvkhangg.personalprivatevault.people.person;

import com.vhvkhangg.personalprivatevault.people.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command to create a new person profile.
 */
public record CreatePersonCommand(
        String name,
        String avatarUrl,
        Gender gender,
        LocalDate birthDate,
        BigDecimal heightCm,
        BigDecimal weightKg,
        String nationalityCode,
        String notes
) {}
