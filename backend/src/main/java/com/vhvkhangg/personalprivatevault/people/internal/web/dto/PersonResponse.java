package com.vhvkhangg.personalprivatevault.people.internal.web.dto;

import com.vhvkhangg.personalprivatevault.people.enums.Gender;
import com.vhvkhangg.personalprivatevault.people.enums.PersonRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record PersonResponse(
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
}
