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

    @Test
    @DisplayName("Verifies people module named interfaces expose reorganized contracts and hide internal packages")
    void verifiesPeopleNamedInterfacesExposureAndEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var peopleModule = modules.getModuleByName("people").orElseThrow();

        // 1. Verify exact logical named interfaces: no extra named interface introduced
        assertThat(peopleModule.getNamedInterfaces().stream()
                .filter(org.springframework.modulith.core.NamedInterface::isNamed)
                .map(org.springframework.modulith.core.NamedInterface::getName))
                .containsExactlyInAnyOrder("enums", "group", "person", "view");

        // 2. Verify person named interface exposes operations, commands, and exceptions across new child packages
        var personInterface = peopleModule.getNamedInterfaces().getByName("person").orElseThrow();
        var personTypeNames = personInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();

        assertThat(personTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.people.person.PersonOperations",
                "com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand",
                "com.vhvkhangg.personalprivatevault.people.person.command.UpdatePersonCommand",
                "com.vhvkhangg.personalprivatevault.people.person.exception.InvalidPersonException",
                "com.vhvkhangg.personalprivatevault.people.person.exception.PersonNotFoundException"
        );

        // 3. Verify group named interface exposes operations, commands, and exceptions across new child packages
        var groupInterface = peopleModule.getNamedInterfaces().getByName("group").orElseThrow();
        var groupTypeNames = groupInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();

        assertThat(groupTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.people.group.CreatorGroupOperations",
                "com.vhvkhangg.personalprivatevault.people.group.command.CreateCreatorGroupCommand",
                "com.vhvkhangg.personalprivatevault.people.group.command.UpdateCreatorGroupCommand",
                "com.vhvkhangg.personalprivatevault.people.group.exception.CreatorGroupNameAlreadyExistsException",
                "com.vhvkhangg.personalprivatevault.people.group.exception.CreatorGroupNotFoundException",
                "com.vhvkhangg.personalprivatevault.people.group.exception.InvalidCreatorGroupException"
        );

        // 4. Verify no internal type is exposed through any named interface of people module
        for (var namedInterface : peopleModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }
}
