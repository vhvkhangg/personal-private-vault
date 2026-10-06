package com.vhvkhangg.personalprivatevault.personal.internal.web.dto;

import com.vhvkhangg.personalprivatevault.personal.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdatePersonalProfileRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 100) String relationship,
        Boolean isSelf,
        Gender gender,
        LocalDate birthDate,
        @Size(min = 2, max = 2) String nationalityCode,
        @Size(max = 50) String phone,
        @Email @Size(max = 255) String email,
        Long addressId,
        @Size(max = 255) String occupation,
        String notesMarkdown
) {}
