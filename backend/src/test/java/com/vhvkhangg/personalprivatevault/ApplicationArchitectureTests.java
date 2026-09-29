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

    @Test
    @DisplayName("Verifies that fiction module defines expected named interfaces")
    void verifiesFictionModuleConfiguration() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var fictionModule = modules.getModuleByName("fiction");
        assertThat(fictionModule).isPresent();
        assertThat(fictionModule.get().getNamedInterfaces().stream().map(org.springframework.modulith.core.NamedInterface::getName))
                .contains("enums", "fiction", "genre", "link", "view");
    }

    @Test
    @DisplayName("Verifies fiction module named interfaces expose capability contracts and hide internal packages")
    void verifiesFictionNamedInterfacesExposureAndEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var fictionModule = modules.getModuleByName("fiction").orElseThrow();

        // 1. Verify exact logical named interfaces: no unexpected named interface introduced
        assertThat(fictionModule.getNamedInterfaces().stream()
                .filter(org.springframework.modulith.core.NamedInterface::isNamed)
                .map(org.springframework.modulith.core.NamedInterface::getName))
                .containsExactlyInAnyOrder("enums", "fiction", "genre", "link", "view");

        // 2. Verify genre named interface exposes operations, commands, and exceptions
        var genreInterface = fictionModule.getNamedInterfaces().getByName("genre").orElseThrow();
        var genreTypeNames = genreInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(genreTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.fiction.genre.FictionGenreOperations",
                "com.vhvkhangg.personalprivatevault.fiction.genre.command.CreateFictionGenreCommand",
                "com.vhvkhangg.personalprivatevault.fiction.genre.command.UpdateFictionGenreCommand",
                "com.vhvkhangg.personalprivatevault.fiction.genre.exception.FictionGenreNameAlreadyExistsException",
                "com.vhvkhangg.personalprivatevault.fiction.genre.exception.FictionGenreNotFoundException",
                "com.vhvkhangg.personalprivatevault.fiction.genre.exception.InvalidFictionGenreException"
        );

        // 3. Verify fiction named interface exposes operations, commands, and exceptions
        var fictionInterface = fictionModule.getNamedInterfaces().getByName("fiction").orElseThrow();
        var fictionTypeNames = fictionInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(fictionTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.fiction.fiction.FictionOperations",
                "com.vhvkhangg.personalprivatevault.fiction.fiction.command.CreateFictionCommand",
                "com.vhvkhangg.personalprivatevault.fiction.fiction.command.UpdateFictionCommand",
                "com.vhvkhangg.personalprivatevault.fiction.fiction.exception.FictionNotFoundException",
                "com.vhvkhangg.personalprivatevault.fiction.fiction.exception.InvalidFictionException"
        );

        // 4. Verify link named interface exposes operations, commands, and exceptions
        var linkInterface = fictionModule.getNamedInterfaces().getByName("link").orElseThrow();
        var linkTypeNames = linkInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(linkTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.fiction.link.FictionLinkOperations",
                "com.vhvkhangg.personalprivatevault.fiction.link.command.CreateFictionLinkCommand",
                "com.vhvkhangg.personalprivatevault.fiction.link.command.UpdateFictionLinkCommand",
                "com.vhvkhangg.personalprivatevault.fiction.link.exception.FictionLinkNotFoundException",
                "com.vhvkhangg.personalprivatevault.fiction.link.exception.InvalidFictionLinkException"
        );

        // 5. Verify enums named interface exposes expected enums
        var enumsInterface = fictionModule.getNamedInterfaces().getByName("enums").orElseThrow();
        var enumsTypeNames = enumsInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(enumsTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat",
                "com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus",
                "com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus"
        );

        // 6. Verify view named interface exposes views
        var viewInterface = fictionModule.getNamedInterfaces().getByName("view").orElseThrow();
        var viewTypeNames = viewInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(viewTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.fiction.view.FictionGenreView",
                "com.vhvkhangg.personalprivatevault.fiction.view.FictionLinkView",
                "com.vhvkhangg.personalprivatevault.fiction.view.FictionClassificationsView",
                "com.vhvkhangg.personalprivatevault.fiction.view.FictionView"
        );

        // 7. Verify no internal type is exposed through any named interface of fiction module
        for (var namedInterface : fictionModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }
}
