package com.vhvkhangg.personalprivatevault;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Architecture verification test for the Personal Private Vault modular monolith.
 *
 * <p>Verifies module boundaries, cyclicity, and explicit inter-module dependencies without
 * starting a full Spring application context or requiring database connectivity.
 */
class ApplicationArchitectureTests {

    @Test
    @DisplayName("Verifies that module structure satisfies Spring Modulith constraints")
    void verifiesModularStructure() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        assertThat(modules).isNotNull();
        modules.verify();
    }

    @Test
    @DisplayName("Verifies that people module defines expected named interfaces")
    void verifiesPeopleModuleConfiguration() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var peopleModule = modules.getModuleByName("people");
        assertThat(peopleModule).isPresent();
        assertThat(peopleModule.get().getNamedInterfaces().stream().map(org.springframework.modulith.core.NamedInterface::getName))
                .contains("enums", "group", "person", "view");
    }
}
