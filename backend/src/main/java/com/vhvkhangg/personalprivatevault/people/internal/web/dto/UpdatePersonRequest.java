package com.vhvkhangg.personalprivatevault.people.internal.web.dto;

import com.vhvkhangg.personalprivatevault.people.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdatePersonRequest(
        @NotBlank(message = "Name must not be blank")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,
        @Size(max = 2048, message = "Avatar URL must not exceed 2048 characters")
        String avatarUrl,
        Gender gender,
        LocalDate birthDate,
        BigDecimal heightCm,
        BigDecimal weightKg,
        @Size(max = 3, message = "Nationality code must not exceed 3 characters")
        String nationalityCode,
        String notes
) {
}
