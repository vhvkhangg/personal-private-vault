package com.vhvkhangg.personalprivatevault.people.internal.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCreatorGroupRequest(
        @NotBlank(message = "Group name must not be blank")
        @Size(max = 255, message = "Group name must not exceed 255 characters")
        String name,
        String description
) {
}
