package com.vhvkhangg.personalprivatevault.personal.internal.web.dto;

import com.vhvkhangg.personalprivatevault.personal.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePersonalProfileRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 100) String relationship,
        Boolean isSelf,
        Gender gender,
        LocalDate birthDate,
        @Size(max = 2) String nationalityCode,
        @Size(max = 64) String phone,
        @Size(max = 320) String email,
        Long addressId,
        @Size(max = 255) String occupation,
        String notesMarkdown
) {
    public CreatePersonalProfileRequest {
        nationalityCode = (nationalityCode != null && !nationalityCode.isBlank()) ? nationalityCode.trim() : null;
    }
}
