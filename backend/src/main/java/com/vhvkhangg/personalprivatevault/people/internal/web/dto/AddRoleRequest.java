package com.vhvkhangg.personalprivatevault.people.internal.web.dto;

import com.vhvkhangg.personalprivatevault.people.enums.PersonRole;
import jakarta.validation.constraints.NotNull;

public record AddRoleRequest(
        @NotNull(message = "Role must not be null")
        PersonRole role
) {
}
