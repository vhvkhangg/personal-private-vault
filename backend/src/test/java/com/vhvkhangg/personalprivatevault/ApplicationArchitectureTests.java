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
                .contains("enums", "group", "person", "view", "search");
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
                .containsExactlyInAnyOrder("enums", "group", "person", "view", "search");

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
                .contains("enums", "fiction", "genre", "link", "view", "search");
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
                .containsExactlyInAnyOrder("enums", "fiction", "genre", "link", "view", "search");

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

    @Test
    @DisplayName("Verifies that film module defines expected named interfaces")
    void verifiesFilmModuleConfiguration() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var filmModule = modules.getModuleByName("film");
        assertThat(filmModule).isPresent();
        assertThat(filmModule.get().getNamedInterfaces().stream().map(org.springframework.modulith.core.NamedInterface::getName))
                .contains("credit", "enums", "film", "genre", "link", "view", "search");
    }

    @Test
    @DisplayName("Verifies film module named interfaces expose capability contracts and hide internal packages")
    void verifiesFilmNamedInterfacesExposureAndEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var filmModule = modules.getModuleByName("film").orElseThrow();

        // 1. Verify exact logical named interfaces: no unexpected named interface introduced
        assertThat(filmModule.getNamedInterfaces().stream()
                .filter(org.springframework.modulith.core.NamedInterface::isNamed)
                .map(org.springframework.modulith.core.NamedInterface::getName))
                .containsExactlyInAnyOrder("credit", "enums", "film", "genre", "link", "view", "search");

        // 2. Verify film named interface exposes operations, commands, and exceptions
        var filmInterface = filmModule.getNamedInterfaces().getByName("film").orElseThrow();
        var filmTypeNames = filmInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(filmTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.film.film.FilmOperations",
                "com.vhvkhangg.personalprivatevault.film.film.command.CreateFilmCommand",
                "com.vhvkhangg.personalprivatevault.film.film.command.UpdateFilmCommand",
                "com.vhvkhangg.personalprivatevault.film.film.exception.FilmNotFoundException",
                "com.vhvkhangg.personalprivatevault.film.film.exception.InvalidFilmException"
        );

        // 3. Verify genre named interface exposes operations, commands, and exceptions
        var genreInterface = filmModule.getNamedInterfaces().getByName("genre").orElseThrow();
        var genreTypeNames = genreInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(genreTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.film.genre.FilmGenreOperations",
                "com.vhvkhangg.personalprivatevault.film.genre.command.CreateFilmGenreCommand",
                "com.vhvkhangg.personalprivatevault.film.genre.command.UpdateFilmGenreCommand",
                "com.vhvkhangg.personalprivatevault.film.genre.exception.FilmGenreNameAlreadyExistsException",
                "com.vhvkhangg.personalprivatevault.film.genre.exception.FilmGenreNotFoundException",
                "com.vhvkhangg.personalprivatevault.film.genre.exception.InvalidFilmGenreException"
        );

        // 4. Verify link named interface exposes operations, commands, and exceptions
        var linkInterface = filmModule.getNamedInterfaces().getByName("link").orElseThrow();
        var linkTypeNames = linkInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(linkTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.film.link.FilmLinkOperations",
                "com.vhvkhangg.personalprivatevault.film.link.command.CreateFilmLinkCommand",
                "com.vhvkhangg.personalprivatevault.film.link.command.UpdateFilmLinkCommand",
                "com.vhvkhangg.personalprivatevault.film.link.exception.FilmLinkNotFoundException",
                "com.vhvkhangg.personalprivatevault.film.link.exception.InvalidFilmLinkException"
        );

        // 5. Verify credit named interface exposes operations, commands, and exceptions
        var creditInterface = filmModule.getNamedInterfaces().getByName("credit").orElseThrow();
        var creditTypeNames = creditInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(creditTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.film.credit.FilmCreditOperations",
                "com.vhvkhangg.personalprivatevault.film.credit.command.CreateFilmCreditCommand",
                "com.vhvkhangg.personalprivatevault.film.credit.exception.FilmCreditNotFoundException",
                "com.vhvkhangg.personalprivatevault.film.credit.exception.InvalidFilmCreditException"
        );

        // 6. Verify enums named interface exposes expected enums
        var enumsInterface = filmModule.getNamedInterfaces().getByName("enums").orElseThrow();
        var enumsTypeNames = enumsInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(enumsTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.film.enums.FilmFormat",
                "com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle",
                "com.vhvkhangg.personalprivatevault.film.enums.FilmCreditRole",
                "com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus",
                "com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus"
        );

        // 7. Verify view named interface exposes views
        var viewInterface = filmModule.getNamedInterfaces().getByName("view").orElseThrow();
        var viewTypeNames = viewInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(viewTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.film.view.FilmView",
                "com.vhvkhangg.personalprivatevault.film.view.FilmGenreView",
                "com.vhvkhangg.personalprivatevault.film.view.FilmLinkView",
                "com.vhvkhangg.personalprivatevault.film.view.FilmCreditView",
                "com.vhvkhangg.personalprivatevault.film.view.FilmClassificationsView"
        );

        // 8. Verify no internal type is exposed through any named interface of film module
        for (var namedInterface : filmModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }

    @Test
    @DisplayName("Verifies that media module defines expected named interfaces")
    void verifiesMediaModuleConfiguration() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var mediaModule = modules.getModuleByName("media");
        assertThat(mediaModule).isPresent();
        assertThat(mediaModule.get().getNamedInterfaces().stream().map(org.springframework.modulith.core.NamedInterface::getName))
                .contains("album", "image", "view", "search");
    }

    @Test
    @DisplayName("Verifies media module named interfaces expose capability contracts and hide internal packages")
    void verifiesMediaNamedInterfacesExposureAndEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var mediaModule = modules.getModuleByName("media").orElseThrow();

        // 1. Verify exact logical named interfaces: no unexpected named interface introduced
        assertThat(mediaModule.getNamedInterfaces().stream()
                .filter(org.springframework.modulith.core.NamedInterface::isNamed)
                .map(org.springframework.modulith.core.NamedInterface::getName))
                .containsExactlyInAnyOrder("album", "image", "view", "search");

        // 2. Verify album named interface exposes operations, commands, and exceptions
        var albumInterface = mediaModule.getNamedInterfaces().getByName("album").orElseThrow();
        var albumTypeNames = albumInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(albumTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.media.album.AlbumOperations",
                "com.vhvkhangg.personalprivatevault.media.album.CreateAlbumCommand",
                "com.vhvkhangg.personalprivatevault.media.album.UpdateAlbumCommand",
                "com.vhvkhangg.personalprivatevault.media.album.AlbumNotFoundException",
                "com.vhvkhangg.personalprivatevault.media.album.InvalidAlbumException"
        );

        // 3. Verify image named interface exposes operations, commands, and exceptions
        var imageInterface = mediaModule.getNamedInterfaces().getByName("image").orElseThrow();
        var imageTypeNames = imageInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(imageTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.media.image.ImageOperations",
                "com.vhvkhangg.personalprivatevault.media.image.CreateImageCommand",
                "com.vhvkhangg.personalprivatevault.media.image.UpdateImageMetadataCommand",
                "com.vhvkhangg.personalprivatevault.media.image.ImageNotFoundException",
                "com.vhvkhangg.personalprivatevault.media.image.InvalidImageException",
                "com.vhvkhangg.personalprivatevault.media.image.ImageConflictException"
        );

        // 4. Verify view named interface exposes views
        var viewInterface = mediaModule.getNamedInterfaces().getByName("view").orElseThrow();
        var viewTypeNames = viewInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(viewTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.media.view.AlbumView",
                "com.vhvkhangg.personalprivatevault.media.view.ImageView"
        );

        // 5. Verify no internal type is exposed through any named interface of media module
        for (var namedInterface : mediaModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }

    @Test
    @DisplayName("Verifies that location module defines expected named interfaces")
    void verifiesLocationModuleConfiguration() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var locationModule = modules.getModuleByName("location");
        assertThat(locationModule).isPresent();
        assertThat(locationModule.get().getNamedInterfaces().stream().map(org.springframework.modulith.core.NamedInterface::getName))
                .contains("address", "brand", "category", "enums", "hours", "location", "view", "search");
    }

    @Test
    @DisplayName("Verifies location module named interfaces expose capability contracts and hide internal packages")
    void verifiesLocationNamedInterfacesExposureAndEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var locationModule = modules.getModuleByName("location").orElseThrow();

        // 1. Verify exact logical named interfaces: no unexpected named interface introduced
        assertThat(locationModule.getNamedInterfaces().stream()
                .filter(org.springframework.modulith.core.NamedInterface::isNamed)
                .map(org.springframework.modulith.core.NamedInterface::getName))
                .containsExactlyInAnyOrder("address", "brand", "category", "enums", "hours", "location", "view", "search");

        // 2. Verify address named interface exposes operations, commands, and exceptions
        var addressInterface = locationModule.getNamedInterfaces().getByName("address").orElseThrow();
        var addressTypeNames = addressInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(addressTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.location.address.AddressOperations",
                "com.vhvkhangg.personalprivatevault.location.address.CreateAddressCommand",
                "com.vhvkhangg.personalprivatevault.location.address.UpdateAddressCommand",
                "com.vhvkhangg.personalprivatevault.location.address.AddressNotFoundException",
                "com.vhvkhangg.personalprivatevault.location.address.InvalidAddressException"
        );

        // 3. Verify brand named interface exposes operations, commands, and exceptions
        var brandInterface = locationModule.getNamedInterfaces().getByName("brand").orElseThrow();
        var brandTypeNames = brandInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(brandTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.location.brand.BrandOperations",
                "com.vhvkhangg.personalprivatevault.location.brand.CreateBrandCommand",
                "com.vhvkhangg.personalprivatevault.location.brand.UpdateBrandCommand",
                "com.vhvkhangg.personalprivatevault.location.brand.BrandNotFoundException",
                "com.vhvkhangg.personalprivatevault.location.brand.InvalidBrandException"
        );

        // 4. Verify category named interface exposes operations, commands, and exceptions
        var categoryInterface = locationModule.getNamedInterfaces().getByName("category").orElseThrow();
        var categoryTypeNames = categoryInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(categoryTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.location.category.LocationCategoryOperations",
                "com.vhvkhangg.personalprivatevault.location.category.CreateLocationCategoryCommand",
                "com.vhvkhangg.personalprivatevault.location.category.UpdateLocationCategoryCommand",
                "com.vhvkhangg.personalprivatevault.location.category.LocationCategoryNotFoundException",
                "com.vhvkhangg.personalprivatevault.location.category.LocationCategoryNameAlreadyExistsException",
                "com.vhvkhangg.personalprivatevault.location.category.InvalidLocationCategoryException"
        );

        // 5. Verify enums named interface exposes expected enums
        var enumsInterface = locationModule.getNamedInterfaces().getByName("enums").orElseThrow();
        var enumsTypeNames = enumsInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(enumsTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.location.enums.DiningServiceStyle",
                "com.vhvkhangg.personalprivatevault.location.enums.DayOfWeek"
        );

        // 6. Verify hours named interface exposes operations, commands, inputs, and exceptions
        var hoursInterface = locationModule.getNamedInterfaces().getByName("hours").orElseThrow();
        var hoursTypeNames = hoursInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(hoursTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursOperations",
                "com.vhvkhangg.personalprivatevault.location.hours.ReplaceBusinessHoursScheduleCommand",
                "com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursIntervalInput",
                "com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursNotFoundException",
                "com.vhvkhangg.personalprivatevault.location.hours.InvalidBusinessHoursException"
        );

        // 7. Verify location named interface exposes operations, commands, and exceptions
        var locInterface = locationModule.getNamedInterfaces().getByName("location").orElseThrow();
        var locTypeNames = locInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(locTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.location.location.LocationOperations",
                "com.vhvkhangg.personalprivatevault.location.location.CreateLocationCommand",
                "com.vhvkhangg.personalprivatevault.location.location.UpdateLocationCommand",
                "com.vhvkhangg.personalprivatevault.location.location.LocationNotFoundException",
                "com.vhvkhangg.personalprivatevault.location.location.InvalidLocationException"
        );

        // 8. Verify view named interface exposes views
        var viewInterface = locationModule.getNamedInterfaces().getByName("view").orElseThrow();
        var viewTypeNames = viewInterface.asJavaClasses()
                .map(com.tngtech.archunit.core.domain.JavaClass::getName)
                .toList();
        assertThat(viewTypeNames).contains(
                "com.vhvkhangg.personalprivatevault.location.view.AddressView",
                "com.vhvkhangg.personalprivatevault.location.view.BrandView",
                "com.vhvkhangg.personalprivatevault.location.view.BusinessHoursIntervalView",
                "com.vhvkhangg.personalprivatevault.location.view.BusinessHoursScheduleView",
                "com.vhvkhangg.personalprivatevault.location.view.LocationCategoryView",
                "com.vhvkhangg.personalprivatevault.location.view.LocationView"
        );

        // 9. Verify no internal type is exposed through any named interface of location module
        for (var namedInterface : locationModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }

    @Test
    @DisplayName("Verifies that feed module defines expected named interfaces")
    void verifiesFeedModuleConfiguration() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var feedModule = modules.getModuleByName("feed");
        assertThat(feedModule).isPresent();
        assertThat(feedModule.get().getNamedInterfaces().stream().map(org.springframework.modulith.core.NamedInterface::getName))
                .contains("conversion", "enums", "item", "resource", "source", "view", "search");
    }

    @Test
    @DisplayName("Verifies feed module named interfaces expose capability contracts and hide internal packages")
    void verifiesFeedNamedInterfacesExposureAndEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var feedModule = modules.getModuleByName("feed").orElseThrow();

        assertThat(feedModule.getNamedInterfaces().stream()
                .filter(org.springframework.modulith.core.NamedInterface::isNamed)
                .map(org.springframework.modulith.core.NamedInterface::getName))
                .containsExactlyInAnyOrder("conversion", "enums", "item", "resource", "source", "view", "search");

        var sourceInterface = feedModule.getNamedInterfaces().getByName("source").orElseThrow();
        assertThat(sourceInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.feed.source.FeedSourceOperations",
                        "com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand",
                        "com.vhvkhangg.personalprivatevault.feed.source.command.UpdateFeedSourceCommand",
                        "com.vhvkhangg.personalprivatevault.feed.source.exception.FeedSourceNotFoundException",
                        "com.vhvkhangg.personalprivatevault.feed.source.exception.InvalidFeedSourceException"
                );

        var itemInterface = feedModule.getNamedInterfaces().getByName("item").orElseThrow();
        assertThat(itemInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.feed.item.FeedItemOperations",
                        "com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput",
                        "com.vhvkhangg.personalprivatevault.feed.item.exception.FeedItemConflictException",
                        "com.vhvkhangg.personalprivatevault.feed.item.exception.FeedItemNotFoundException",
                        "com.vhvkhangg.personalprivatevault.feed.item.exception.InvalidFeedItemException"
                );

        var resourceInterface = feedModule.getNamedInterfaces().getByName("resource").orElseThrow();
        assertThat(resourceInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.feed.resource.SavedResourceOperations",
                        "com.vhvkhangg.personalprivatevault.feed.resource.command.CreateFeedSavedResourceCommand",
                        "com.vhvkhangg.personalprivatevault.feed.resource.command.CreateManualSavedResourceCommand",
                        "com.vhvkhangg.personalprivatevault.feed.resource.exception.SavedResourceConflictException",
                        "com.vhvkhangg.personalprivatevault.feed.resource.exception.SavedResourceNotFoundException",
                        "com.vhvkhangg.personalprivatevault.feed.resource.exception.InvalidSavedResourceException"
                );

        var conversionInterface = feedModule.getNamedInterfaces().getByName("conversion").orElseThrow();
        assertThat(conversionInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.feed.conversion.SavedResourceConversionOperations",
                        "com.vhvkhangg.personalprivatevault.feed.conversion.exception.InvalidSavedResourceConversionException"
                );

        for (var namedInterface : feedModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }

    @Test
    @DisplayName("Verifies that importdata module defines expected named interfaces")
    void verifiesImportDataModuleConfiguration() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var importDataModule = modules.getModuleByName("importdata");
        assertThat(importDataModule).isPresent();
        assertThat(importDataModule.get().getNamedInterfaces().stream().map(org.springframework.modulith.core.NamedInterface::getName))
                .contains("enums", "job", "view");
    }

    @Test
    @DisplayName("Verifies importdata module named interfaces expose capability contracts and hide internal packages")
    void verifiesImportDataNamedInterfacesExposureAndEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var importDataModule = modules.getModuleByName("importdata").orElseThrow();

        assertThat(importDataModule.getNamedInterfaces().stream()
                .filter(org.springframework.modulith.core.NamedInterface::isNamed)
                .map(org.springframework.modulith.core.NamedInterface::getName))
                .containsExactlyInAnyOrder("enums", "job", "view");

        var jobInterface = importDataModule.getNamedInterfaces().getByName("job").orElseThrow();
        assertThat(jobInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.importdata.job.ImportJobOperations",
                        "com.vhvkhangg.personalprivatevault.importdata.job.command.CreateImportJobCommand",
                        "com.vhvkhangg.personalprivatevault.importdata.job.command.ExecuteImportJobCommand",
                        "com.vhvkhangg.personalprivatevault.importdata.job.command.ImportItemDecisionInput",
                        "com.vhvkhangg.personalprivatevault.importdata.job.exception.ImportJobNotFoundException",
                        "com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportJobException",
                        "com.vhvkhangg.personalprivatevault.importdata.job.exception.InvalidImportTransitionException"
                );

        for (var namedInterface : importDataModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }

    @Test
    @DisplayName("Verifies feed and importdata modules allowed dependencies and lack of mutual cross-dependency")
    void verifiesFeedAndImportDataModuleDependencies() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var feedModule = modules.getModuleByName("feed").orElseThrow();
        var importDataModule = modules.getModuleByName("importdata").orElseThrow();

        assertThat(feedModule.getDirectDependencies(modules).stream().toList())
                .noneMatch(dep -> dep.getTargetModule().equals(importDataModule));

        assertThat(importDataModule.getDirectDependencies(modules).stream().toList())
                .noneMatch(dep -> dep.getTargetModule().equals(feedModule));
    }

    @Test
    @DisplayName("Verifies that finance module defines expected named interfaces")
    void verifiesFinanceModuleConfiguration() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var financeModule = modules.getModuleByName("finance");
        assertThat(financeModule).isPresent();
        assertThat(financeModule.get().getNamedInterfaces().stream().map(org.springframework.modulith.core.NamedInterface::getName))
                .contains("category", "enums", "recurring", "subscription", "transaction", "view", "wallet");
    }

    @Test
    @DisplayName("Verifies finance module named interfaces expose capability contracts and hide internal packages")
    void verifiesFinanceNamedInterfacesExposureAndEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var financeModule = modules.getModuleByName("finance").orElseThrow();

        assertThat(financeModule.getNamedInterfaces().stream()
                .filter(org.springframework.modulith.core.NamedInterface::isNamed)
                .map(org.springframework.modulith.core.NamedInterface::getName))
                .containsExactlyInAnyOrder("category", "enums", "recurring", "subscription", "transaction", "view", "wallet");

        var walletInterface = financeModule.getNamedInterfaces().getByName("wallet").orElseThrow();
        assertThat(walletInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.finance.wallet.WalletOperations",
                        "com.vhvkhangg.personalprivatevault.finance.wallet.command.CreateWalletCommand",
                        "com.vhvkhangg.personalprivatevault.finance.wallet.command.UpdateWalletCommand",
                        "com.vhvkhangg.personalprivatevault.finance.wallet.exception.WalletNotFoundException",
                        "com.vhvkhangg.personalprivatevault.finance.wallet.exception.InvalidWalletException",
                        "com.vhvkhangg.personalprivatevault.finance.wallet.exception.WalletConflictException"
                );

        var categoryInterface = financeModule.getNamedInterfaces().getByName("category").orElseThrow();
        assertThat(categoryInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.finance.category.TransactionCategoryOperations",
                        "com.vhvkhangg.personalprivatevault.finance.category.command.CreateTransactionCategoryCommand",
                        "com.vhvkhangg.personalprivatevault.finance.category.command.UpdateTransactionCategoryCommand",
                        "com.vhvkhangg.personalprivatevault.finance.category.exception.TransactionCategoryNotFoundException",
                        "com.vhvkhangg.personalprivatevault.finance.category.exception.InvalidTransactionCategoryException",
                        "com.vhvkhangg.personalprivatevault.finance.category.exception.TransactionCategoryConflictException"
                );

        var transactionInterface = financeModule.getNamedInterfaces().getByName("transaction").orElseThrow();
        assertThat(transactionInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.finance.transaction.FinancialTransactionOperations",
                        "com.vhvkhangg.personalprivatevault.finance.transaction.command.CreateFinancialTransactionCommand",
                        "com.vhvkhangg.personalprivatevault.finance.transaction.command.UpdateFinancialTransactionCommand",
                        "com.vhvkhangg.personalprivatevault.finance.transaction.command.FinancialTransactionEntryInput",
                        "com.vhvkhangg.personalprivatevault.finance.transaction.exception.FinancialTransactionNotFoundException",
                        "com.vhvkhangg.personalprivatevault.finance.transaction.exception.InvalidFinancialTransactionException"
                );

        var recurringInterface = financeModule.getNamedInterfaces().getByName("recurring").orElseThrow();
        assertThat(recurringInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.finance.recurring.RecurringTransactionRuleOperations",
                        "com.vhvkhangg.personalprivatevault.finance.recurring.command.CreateRecurringTransactionRuleCommand",
                        "com.vhvkhangg.personalprivatevault.finance.recurring.command.UpdateRecurringTransactionRuleCommand",
                        "com.vhvkhangg.personalprivatevault.finance.recurring.command.RecurringRuleEntryInput",
                        "com.vhvkhangg.personalprivatevault.finance.recurring.exception.RecurringTransactionRuleNotFoundException",
                        "com.vhvkhangg.personalprivatevault.finance.recurring.exception.InvalidRecurringTransactionRuleException"
                );

        var subscriptionInterface = financeModule.getNamedInterfaces().getByName("subscription").orElseThrow();
        assertThat(subscriptionInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.finance.subscription.SubscriptionOperations",
                        "com.vhvkhangg.personalprivatevault.finance.subscription.command.CreateSubscriptionCommand",
                        "com.vhvkhangg.personalprivatevault.finance.subscription.command.UpdateSubscriptionCommand",
                        "com.vhvkhangg.personalprivatevault.finance.subscription.exception.SubscriptionNotFoundException",
                        "com.vhvkhangg.personalprivatevault.finance.subscription.exception.InvalidSubscriptionException",
                        "com.vhvkhangg.personalprivatevault.finance.subscription.exception.SubscriptionConflictException"
                );

        for (var namedInterface : financeModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }

    @Test
    @DisplayName("Verifies that journal module defines expected named interfaces")
    void verifiesJournalModuleConfiguration() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var journalModule = modules.getModuleByName("journal");
        assertThat(journalModule).isPresent();
        assertThat(journalModule.get().getNamedInterfaces().stream().map(org.springframework.modulith.core.NamedInterface::getName))
                .contains("diary", "view");
    }

    @Test
    @DisplayName("Verifies journal module named interfaces expose capability contracts and hide internal packages")
    void verifiesJournalNamedInterfacesExposureAndEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var journalModule = modules.getModuleByName("journal").orElseThrow();

        assertThat(journalModule.getNamedInterfaces().stream()
                .filter(org.springframework.modulith.core.NamedInterface::isNamed)
                .map(org.springframework.modulith.core.NamedInterface::getName))
                .containsExactlyInAnyOrder("diary", "view");

        var diaryInterface = journalModule.getNamedInterfaces().getByName("diary").orElseThrow();
        assertThat(diaryInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.journal.diary.DiaryOperations",
                        "com.vhvkhangg.personalprivatevault.journal.diary.command.CreateDiaryEntryCommand",
                        "com.vhvkhangg.personalprivatevault.journal.diary.command.UpdateDiaryEntryCommand",
                        "com.vhvkhangg.personalprivatevault.journal.diary.exception.DiaryEntryNotFoundException",
                        "com.vhvkhangg.personalprivatevault.journal.diary.exception.InvalidDiaryEntryException"
                );

        for (var namedInterface : journalModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }

    @Test
    @DisplayName("Verifies that personal module defines expected named interfaces")
    void verifiesPersonalModuleConfiguration() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var personalModule = modules.getModuleByName("personal");
        assertThat(personalModule).isPresent();
        assertThat(personalModule.get().getNamedInterfaces().stream().map(org.springframework.modulith.core.NamedInterface::getName))
                .contains("enums", "profile", "view");
    }

    @Test
    @DisplayName("Verifies personal module named interfaces expose capability contracts and hide internal packages")
    void verifiesPersonalNamedInterfacesExposureAndEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var personalModule = modules.getModuleByName("personal").orElseThrow();

        assertThat(personalModule.getNamedInterfaces().stream()
                .filter(org.springframework.modulith.core.NamedInterface::isNamed)
                .map(org.springframework.modulith.core.NamedInterface::getName))
                .containsExactlyInAnyOrder("enums", "profile", "view");

        var profileInterface = personalModule.getNamedInterfaces().getByName("profile").orElseThrow();
        assertThat(profileInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.personal.profile.PersonalProfileOperations",
                        "com.vhvkhangg.personalprivatevault.personal.profile.command.CreatePersonalProfileCommand",
                        "com.vhvkhangg.personalprivatevault.personal.profile.command.UpdatePersonalProfileCommand",
                        "com.vhvkhangg.personalprivatevault.personal.profile.exception.PersonalProfileNotFoundException",
                        "com.vhvkhangg.personalprivatevault.personal.profile.exception.InvalidPersonalProfileException",
                        "com.vhvkhangg.personalprivatevault.personal.profile.exception.PersonalProfileConflictException"
                );

        for (var namedInterface : personalModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }

    @Test
    @DisplayName("Verifies Phase 11 module dependencies and isolation")
    void verifiesPhase11ModuleDependenciesAndIsolation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var financeModule = modules.getModuleByName("finance").orElseThrow();
        var journalModule = modules.getModuleByName("journal").orElseThrow();
        var personalModule = modules.getModuleByName("personal").orElseThrow();
        var referenceModule = modules.getModuleByName("reference").orElseThrow();
        var locationModule = modules.getModuleByName("location").orElseThrow();
        var vaultModule = modules.getModuleByName("vault").orElseThrow();

        // Finance -> reference only
        assertThat(financeModule.getDirectDependencies(modules).stream().toList())
                .allMatch(dep -> dep.getTargetModule().equals(referenceModule));
        assertThat(financeModule.getDirectDependencies(modules).stream().toList())
                .noneMatch(dep -> dep.getTargetModule().equals(vaultModule)
                        || dep.getTargetModule().equals(journalModule)
                        || dep.getTargetModule().equals(personalModule));

        // Journal -> none
        assertThat(journalModule.getDirectDependencies(modules).stream().toList()).isEmpty();

        // Personal -> reference, location only
        assertThat(personalModule.getDirectDependencies(modules).stream().toList())
                .allMatch(dep -> dep.getTargetModule().equals(referenceModule)
                        || dep.getTargetModule().equals(locationModule));
        assertThat(personalModule.getDirectDependencies(modules).stream().toList())
                .noneMatch(dep -> dep.getTargetModule().equals(vaultModule)
                        || dep.getTargetModule().equals(financeModule)
                        || dep.getTargetModule().equals(journalModule));
    }

    @Test
    @DisplayName("Verifies that search module defines expected named interfaces")
    void verifiesSearchModuleConfiguration() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var searchModule = modules.getModuleByName("search");
        assertThat(searchModule).isPresent();
        assertThat(searchModule.get().getNamedInterfaces().stream().map(org.springframework.modulith.core.NamedInterface::getName))
                .contains("enums", "query", "view");
    }

    @Test
    @DisplayName("Verifies search module named interfaces expose capability contracts and hide internal packages")
    void verifiesSearchNamedInterfacesExposureAndEncapsulation() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var searchModule = modules.getModuleByName("search").orElseThrow();

        assertThat(searchModule.getNamedInterfaces().stream()
                .filter(org.springframework.modulith.core.NamedInterface::isNamed)
                .map(org.springframework.modulith.core.NamedInterface::getName))
                .containsExactlyInAnyOrder("enums", "query", "view");

        var queryInterface = searchModule.getNamedInterfaces().getByName("query").orElseThrow();
        assertThat(queryInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.search.query.GlobalSearchOperations",
                        "com.vhvkhangg.personalprivatevault.search.query.GlobalSearchQuery"
                );

        var viewInterface = searchModule.getNamedInterfaces().getByName("view").orElseThrow();
        assertThat(viewInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.search.view.GlobalSearchResult",
                        "com.vhvkhangg.personalprivatevault.search.view.GlobalSearchPage"
                );

        var enumsInterface = searchModule.getNamedInterfaces().getByName("enums").orElseThrow();
        assertThat(enumsInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName).toList())
                .contains(
                        "com.vhvkhangg.personalprivatevault.search.enums.SearchDomain",
                        "com.vhvkhangg.personalprivatevault.search.enums.SearchMatchKind"
                );

        for (var namedInterface : searchModule.getNamedInterfaces()) {
            assertThat(namedInterface.asJavaClasses().map(com.tngtech.archunit.core.domain.JavaClass::getName))
                    .noneMatch(name -> name.contains(".internal."));
        }
    }

    @Test
    @DisplayName("Verifies search module is a leaf orchestration module with no owned entities or repositories")
    void verifiesSearchModuleHasNoOwnedEntitiesOrRepositories() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var searchModule = modules.getModuleByName("search").orElseThrow();

        var allClasses = new com.tngtech.archunit.core.importer.ClassFileImporter()
                .importPackages("com.vhvkhangg.personalprivatevault.search");
        assertThat(allClasses.stream().filter(c -> c.isAnnotatedWith(jakarta.persistence.Entity.class)).toList()).isEmpty();
        assertThat(allClasses.stream().filter(c -> c.isAnnotatedWith(org.springframework.stereotype.Repository.class)).toList()).isEmpty();
    }

    @Test
    @DisplayName("Verifies search module dependencies and leaf invariant (no module depends on search)")
    void verifiesSearchModuleDependenciesAndLeafProperty() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var searchModule = modules.getModuleByName("search").orElseThrow();

        for (var module : modules) {
            if (!module.equals(searchModule)) {
                assertThat(module.getDirectDependencies(modules).stream().toList())
                        .noneMatch(dep -> dep.getTargetModule().equals(searchModule));
            }
        }
    }

    @Test
    @DisplayName("Verifies portability module configuration, zero dependencies, and leaf invariant")
    void verifiesPortabilityModuleConfigurationAndLeafProperty() {
        ApplicationModules modules = ApplicationModules.of(PersonalPrivateVaultApplication.class);
        var portabilityModuleOpt = modules.getModuleByName("portability");
        assertThat(portabilityModuleOpt).isPresent();
        var portabilityModule = portabilityModuleOpt.get();

        // 1. Zero outbound dependencies to other application modules
        assertThat(portabilityModule.getDirectDependencies(modules).stream().toList()).isEmpty();

        // 2. Leaf invariant: no application module depends on portability
        for (var module : modules) {
            if (!module.equals(portabilityModule)) {
                assertThat(module.getDirectDependencies(modules).stream().toList())
                        .noneMatch(dep -> dep.getTargetModule().equals(portabilityModule));
            }
        }
    }

    @Test
    @DisplayName("Verifies portability module owns zero JPA entities and zero JPA repositories")
    void verifiesPortabilityOwnsNoJpaEntitiesOrRepositories() {
        var allClasses = new com.tngtech.archunit.core.importer.ClassFileImporter()
                .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.vhvkhangg.personalprivatevault.portability");

        assertThat(allClasses.stream().filter(c -> c.isAnnotatedWith(jakarta.persistence.Entity.class)).toList()).isEmpty();
        assertThat(allClasses.stream().filter(c -> c.isAnnotatedWith(jakarta.persistence.Table.class)).toList()).isEmpty();
        assertThat(allClasses.stream().filter(c -> c.isAnnotatedWith(org.springframework.stereotype.Repository.class)).toList()).isEmpty();
        assertThat(allClasses.stream().filter(c -> c.isAssignableTo(org.springframework.data.repository.Repository.class)).toList()).isEmpty();
    }

    @Test
    @DisplayName("Verifies cross-table JDBC access in portability is strictly confined to PortabilitySnapshotAdapter")
    void verifiesPortabilityJdbcAccessIsRestrictedToSnapshotAdapter() {
        var allClasses = new com.tngtech.archunit.core.importer.ClassFileImporter()
                .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.vhvkhangg.personalprivatevault.portability");

        var nonAdapterClasses = allClasses.stream()
                .filter(c -> !c.getName().equals("com.vhvkhangg.personalprivatevault.portability.internal.infrastructure.snapshot.PortabilitySnapshotAdapter"))
                .toList();

        for (var javaClass : nonAdapterClasses) {
            for (var dep : javaClass.getDirectDependenciesFromSelf()) {
                String target = dep.getTargetClass().getName();
                assertThat(target.startsWith("java.sql.") || target.startsWith("javax.sql.") || target.startsWith("org.springframework.jdbc."))
                        .as("Class %s outside snapshot adapter illegally accesses JDBC: %s", javaClass.getName(), target)
                        .isFalse();
            }
        }
    }

    @Test
    @DisplayName("Verifies AWS/S3 provider types do not leak outside Media infrastructure storage package")
    void verifiesMediaStorageProviderTypesStayInInfrastructure() {
        var allClasses = new com.tngtech.archunit.core.importer.ClassFileImporter()
                .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.vhvkhangg.personalprivatevault");

        var nonStorageInfraClasses = allClasses.stream()
                .filter(c -> !c.getPackageName().startsWith("com.vhvkhangg.personalprivatevault.media.internal.infrastructure.storage"))
                .toList();

        for (var javaClass : nonStorageInfraClasses) {
            for (var dep : javaClass.getDirectDependenciesFromSelf()) {
                assertThat(dep.getTargetClass().getName())
                        .as("Class %s outside Media storage infrastructure leaks AWS/S3 type: %s", javaClass.getName(), dep.getTargetClass().getName())
                        .doesNotContain("software.amazon.awssdk");
            }
        }
    }

    @Test
    @DisplayName("Verifies root package classes strictly adhere to ADR-0016 allowlist")
    void verifiesRootPackageStrictAllowlist() {
        var allClasses = new com.tngtech.archunit.core.importer.ClassFileImporter()
                .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.vhvkhangg.personalprivatevault");

        var rootClasses = allClasses.stream()
                .filter(c -> c.getPackageName().equals("com.vhvkhangg.personalprivatevault"))
                .map(com.tngtech.archunit.core.domain.JavaClass::getSimpleName)
                .filter(name -> !name.isEmpty() && !name.contains("$"))
                .toList();

        java.util.Set<String> allowedSimpleNames = java.util.Set.of(
                "PersonalPrivateVaultApplication",
                "ApiResponse",
                "ApiError",
                "ApiFieldError",
                "ApiMeta",
                "ApiPageMeta",
                "ApiResponses",
                "ApiExceptionHandler",
                "OpenApiConfiguration",
                "package-info"
        );

        assertThat(rootClasses).isSubsetOf(allowedSimpleNames);
    }

    @Test
    @DisplayName("Verifies all @RestController classes reside strictly under internal.web.controller packages")
    void verifiesAllControllersResideInInternalWebControllerPackages() {
        var allClasses = new com.tngtech.archunit.core.importer.ClassFileImporter()
                .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.vhvkhangg.personalprivatevault");

        var controllers = allClasses.stream()
                .filter(c -> c.isAnnotatedWith(org.springframework.web.bind.annotation.RestController.class))
                .toList();

        assertThat(controllers).isNotEmpty();
        for (var controller : controllers) {
            assertThat(controller.getPackageName())
                    .matches("com\\.vhvkhangg\\.personalprivatevault\\.[a-z]+\\.internal\\.web\\.controller");
        }
    }

    @Test
    @DisplayName("Verifies no @RestController exposes JPA entities in public method signatures")
    void verifiesNoControllersExposeJpaEntities() {
        var allClasses = new com.tngtech.archunit.core.importer.ClassFileImporter()
                .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.vhvkhangg.personalprivatevault");

        var controllers = allClasses.stream()
                .filter(c -> c.isAnnotatedWith(org.springframework.web.bind.annotation.RestController.class))
                .toList();

        for (var controller : controllers) {
            for (var method : controller.getMethods()) {
                if (method.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC)) {
                    assertNoEntityInType(method.getReturnType(), "Controller " + controller.getName() + " method " + method.getName() + " return type");

                    for (var param : method.getParameters()) {
                        assertNoEntityInType(param.getType(), "Controller " + controller.getName() + " method " + method.getName() + " parameter index " + param.getIndex());
                    }
                }
            }
        }
    }

    private void assertNoEntityInType(com.tngtech.archunit.core.domain.JavaType type, String locationDescription) {
        if (type == null) {
            return;
        }
        var rawType = type.toErasure();
        assertThat(rawType.isAnnotatedWith(jakarta.persistence.Entity.class))
                .as("%s contains entity type %s", locationDescription, rawType.getName())
                .isFalse();

        if (rawType.isArray()) {
            assertNoEntityInType(rawType.getComponentType(), locationDescription);
        }

        if (type instanceof com.tngtech.archunit.core.domain.JavaParameterizedType parameterizedType) {
            for (var arg : parameterizedType.getActualTypeArguments()) {
                assertNoEntityInType(arg, locationDescription);
            }
        } else if (type instanceof com.tngtech.archunit.core.domain.JavaWildcardType wildcardType) {
            for (var bound : wildcardType.getUpperBounds()) {
                assertNoEntityInType(bound, locationDescription);
            }
            for (var bound : wildcardType.getLowerBounds()) {
                assertNoEntityInType(bound, locationDescription);
            }
        }
    }

    @Test
    @DisplayName("Verifies nested modules in Knowledge and Collection do not expose external web controllers")
    void verifiesNestedModulesHaveNoExternalControllers() {
        var allClasses = new com.tngtech.archunit.core.importer.ClassFileImporter()
                .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.vhvkhangg.personalprivatevault");

        var forbiddenPrefixes = java.util.List.of(
                "com.vhvkhangg.personalprivatevault.knowledge.study",
                "com.vhvkhangg.personalprivatevault.knowledge.information",
                "com.vhvkhangg.personalprivatevault.knowledge.vocabulary",
                "com.vhvkhangg.personalprivatevault.knowledge.note",
                "com.vhvkhangg.personalprivatevault.collection.music",
                "com.vhvkhangg.personalprivatevault.collection.shopping",
                "com.vhvkhangg.personalprivatevault.collection.software"
        );

        var nestedControllers = allClasses.stream()
                .filter(c -> c.isAnnotatedWith(org.springframework.web.bind.annotation.RestController.class))
                .filter(c -> forbiddenPrefixes.stream().anyMatch(prefix -> c.getPackageName().startsWith(prefix)))
                .toList();

        assertThat(nestedControllers).isEmpty();
    }
}
