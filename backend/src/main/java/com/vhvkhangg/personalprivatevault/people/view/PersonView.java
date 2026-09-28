package com.vhvkhangg.personalprivatevault.people.view;

import com.vhvkhangg.personalprivatevault.people.enums.Gender;
import com.vhvkhangg.personalprivatevault.people.enums.PersonRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Immutable view of a person profile and their assigned roles.
 */
public record PersonView(
        Long id,
        String name,
        String avatarUrl,
        Gender gender,
        LocalDate birthDate,
        BigDecimal heightCm,
        BigDecimal weightKg,
        String nationalityCode,
        String notes,
        Set<PersonRole> roles
) {
    public PersonView {
        roles = roles != null ? Set.copyOf(roles) : Set.of();
    }
}
