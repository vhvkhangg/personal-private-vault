package com.vhvkhangg.personalprivatevault.people;

import com.vhvkhangg.personalprivatevault.people.enums.Gender;
import com.vhvkhangg.personalprivatevault.people.enums.PersonRole;
import com.vhvkhangg.personalprivatevault.people.internal.application.PersonService;
import com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence.PersonRepository;
import com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence.PersonRoleRepository;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.person.command.UpdatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.person.exception.InvalidPersonException;
import com.vhvkhangg.personalprivatevault.people.person.exception.PersonNotFoundException;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PersonValidationTest {

    private PersonRepository personRepository;
    private PersonRoleRepository personRoleRepository;
    private VaultEntryOperations vaultEntryOperations;
    private ReferenceCatalog referenceCatalog;
    private PersonService personService;

    @BeforeEach
    void setUp() {
        personRepository = mock(PersonRepository.class);
        personRoleRepository = mock(PersonRoleRepository.class);
        vaultEntryOperations = mock(VaultEntryOperations.class);
        referenceCatalog = mock(ReferenceCatalog.class);
        personService = new PersonService(
                personRepository,
                personRoleRepository,
                vaultEntryOperations,
                referenceCatalog
        );
    }

    @Nested
    @DisplayName("Person Creation Validation")
    class CreatePersonValidation {

        @Test
        @DisplayName("Rejects null command")
        void rejectsNullCommand() {
            assertThatThrownBy(() -> personService.create(null))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Rejects blank person name")
        void rejectsBlankName(String blankName) {
            CreatePersonCommand command = new CreatePersonCommand(
                    blankName, null, Gender.MALE, LocalDate.of(1990, 1, 1),
                    null, null, null, null
            );
            assertThatThrownBy(() -> personService.create(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Person name must not be blank");
        }

        @Test
        @DisplayName("Rejects person name exceeding 255 characters")
        void rejectsOverlyLongName() {
            String longName = "A".repeat(256);
            CreatePersonCommand command = new CreatePersonCommand(
                    longName, null, Gender.FEMALE, null, null, null, null, null
            );
            assertThatThrownBy(() -> personService.create(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("exceed 255 characters");
        }

        @Test
        @DisplayName("Rejects avatar URL exceeding 2048 characters")
        void rejectsOverlyLongAvatarUrl() {
            String longUrl = "https://example.com/" + "x".repeat(2030);
            CreatePersonCommand command = new CreatePersonCommand(
                    "Valid Name", longUrl, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> personService.create(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Avatar URL must not exceed 2048 characters");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-0.01", "-175.50", "-1E+2147483647"})
        @DisplayName("Rejects non-positive height")
        void rejectsNonPositiveHeight(String height) {
            CreatePersonCommand command = new CreatePersonCommand(
                    "Valid Name", null, null, null,
                    new BigDecimal(height), null, null, null
            );
            assertThatThrownBy(() -> personService.create(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Height must be positive");
        }

        @ParameterizedTest
        @ValueSource(strings = {"180.123", "10000", "10000.00", "99999.9", "1E+2147483647", "100E+2147483647", "1000E+2147483647", "1E-2147483647"})
        @DisplayName("Rejects height exceeding numeric(6,2)")
        void rejectsHeightExceedingPrecision(String height) {
            CreatePersonCommand command = new CreatePersonCommand(
                    "Valid Name", null, null, null,
                    new BigDecimal(height), null, null, null
            );
            assertThatThrownBy(() -> personService.create(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Height must fit numeric(6,2)");
            org.mockito.Mockito.verifyNoInteractions(vaultEntryOperations);
            org.mockito.Mockito.verify(personRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1.00", "-70", "-1E+2147483647"})
        @DisplayName("Rejects non-positive weight")
        void rejectsNonPositiveWeight(String weight) {
            CreatePersonCommand command = new CreatePersonCommand(
                    "Valid Name", null, null, null,
                    null, new BigDecimal(weight), null, null
            );
            assertThatThrownBy(() -> personService.create(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Weight must be positive");
            org.mockito.Mockito.verifyNoInteractions(vaultEntryOperations);
            org.mockito.Mockito.verify(personRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"70.123", "10000", "10000.00", "99999", "1E+2147483647", "100E+2147483647", "1000E+2147483647", "1E-2147483647"})
        @DisplayName("Rejects weight exceeding numeric(6,2)")
        void rejectsWeightExceedingPrecision(String weight) {
            CreatePersonCommand command = new CreatePersonCommand(
                    "Valid Name", null, null, null,
                    null, new BigDecimal(weight), null, null
            );
            assertThatThrownBy(() -> personService.create(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Weight must fit numeric(6,2)");
            org.mockito.Mockito.verifyNoInteractions(vaultEntryOperations);
            org.mockito.Mockito.verify(personRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"9999.99", "1E+3", "175.500"})
        @DisplayName("Accepts valid boundary and scientific notation height/weight")
        void acceptsValidBoundaryDecimals(String validNumeric) {
            when(personRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));
            when(vaultEntryOperations.create(org.mockito.ArgumentMatchers.any())).thenReturn(
                    new com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView(
                            1L, com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType.PERSON,
                            java.time.Instant.now(), java.time.Instant.now(), null
                    )
            );

            BigDecimal val = new BigDecimal(validNumeric);
            CreatePersonCommand command = new CreatePersonCommand(
                    "Valid Name", null, null, null,
                    val, val, null, null
            );
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> personService.create(command));
        }

        @ParameterizedTest
        @ValueSource(strings = {"U", "USA", "123"})
        @DisplayName("Rejects nationality codes that are not exactly 2 characters")
        void rejectsInvalidNationalityLength(String code) {
            CreatePersonCommand command = new CreatePersonCommand(
                    "Valid Name", null, null, null,
                    null, null, code, null
            );
            assertThatThrownBy(() -> personService.create(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Nationality code must be a 2-character ISO country code");
        }

        @Test
        @DisplayName("Rejects nationality code not found in reference catalog")
        void rejectsMissingReferenceCountry() {
            when(referenceCatalog.country("ZZ")).thenReturn(Optional.empty());
            CreatePersonCommand command = new CreatePersonCommand(
                    "Valid Name", null, null, null,
                    null, null, "ZZ", null
            );
            assertThatThrownBy(() -> personService.create(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Nationality code 'ZZ' does not exist in reference catalog");
        }
    }

    @Nested
    @DisplayName("Person Update Validation")
    class UpdatePersonValidation {

        @Test
        @DisplayName("Rejects null update command")
        void rejectsNullUpdateCommand() {
            assertThatThrownBy(() -> personService.update(null))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("must not be null");
        }

        @Test
        @DisplayName("Rejects update command with null id")
        void rejectsNullIdInUpdate() {
            UpdatePersonCommand command = new UpdatePersonCommand(
                    null, "Updated Name", null, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> personService.update(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Person id must not be null");
        }

        @Test
        @DisplayName("Throws PersonNotFoundException when updating non-existent person")
        void throwsNotFoundOnNonExistentPerson() {
            when(personRepository.findById(999L)).thenReturn(Optional.empty());
            UpdatePersonCommand command = new UpdatePersonCommand(
                    999L, "Updated Name", null, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> personService.update(command))
                    .isInstanceOf(PersonNotFoundException.class)
                    .hasMessageContaining("Person not found with id: 999");
        }

        @ParameterizedTest
        @ValueSource(strings = {"180.123", "10000", "10000.00", "99999.9", "1E+2147483647", "100E+2147483647", "1000E+2147483647", "1E-2147483647"})
        @DisplayName("Rejects height exceeding numeric(6,2) on update")
        void rejectsHeightExceedingPrecisionOnUpdate(String height) {
            com.vhvkhangg.personalprivatevault.people.internal.domain.Person existing =
                    new com.vhvkhangg.personalprivatevault.people.internal.domain.Person(
                            1L, "Existing", null, null, null, null, null, null, null
                    );
            when(personRepository.findById(1L)).thenReturn(Optional.of(existing));

            UpdatePersonCommand command = new UpdatePersonCommand(
                    1L, "Existing", null, null, null,
                    new BigDecimal(height), null, null, null
            );
            assertThatThrownBy(() -> personService.update(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Height must fit numeric(6,2)");
            org.mockito.Mockito.verify(personRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1.00", "-1E+2147483647"})
        @DisplayName("Rejects non-positive height on update")
        void rejectsNonPositiveHeightOnUpdate(String height) {
            com.vhvkhangg.personalprivatevault.people.internal.domain.Person existing =
                    new com.vhvkhangg.personalprivatevault.people.internal.domain.Person(
                            1L, "Existing", null, null, null, null, null, null, null
                    );
            when(personRepository.findById(1L)).thenReturn(Optional.of(existing));

            UpdatePersonCommand command = new UpdatePersonCommand(
                    1L, "Existing", null, null, null,
                    new BigDecimal(height), null, null, null
            );
            assertThatThrownBy(() -> personService.update(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Height must be positive");
            org.mockito.Mockito.verify(personRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"70.123", "10000", "10000.00", "99999", "1E+2147483647", "100E+2147483647", "1000E+2147483647", "1E-2147483647"})
        @DisplayName("Rejects weight exceeding numeric(6,2) on update")
        void rejectsWeightExceedingPrecisionOnUpdate(String weight) {
            com.vhvkhangg.personalprivatevault.people.internal.domain.Person existing =
                    new com.vhvkhangg.personalprivatevault.people.internal.domain.Person(
                            1L, "Existing", null, null, null, null, null, null, null
                    );
            when(personRepository.findById(1L)).thenReturn(Optional.of(existing));

            UpdatePersonCommand command = new UpdatePersonCommand(
                    1L, "Existing", null, null, null,
                    null, new BigDecimal(weight), null, null
            );
            assertThatThrownBy(() -> personService.update(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Weight must fit numeric(6,2)");
            org.mockito.Mockito.verify(personRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1.00", "-1E+2147483647"})
        @DisplayName("Rejects non-positive weight on update")
        void rejectsNonPositiveWeightOnUpdate(String weight) {
            com.vhvkhangg.personalprivatevault.people.internal.domain.Person existing =
                    new com.vhvkhangg.personalprivatevault.people.internal.domain.Person(
                            1L, "Existing", null, null, null, null, null, null, null
                    );
            when(personRepository.findById(1L)).thenReturn(Optional.of(existing));

            UpdatePersonCommand command = new UpdatePersonCommand(
                    1L, "Existing", null, null, null,
                    null, new BigDecimal(weight), null, null
            );
            assertThatThrownBy(() -> personService.update(command))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("Weight must be positive");
        }

        @ParameterizedTest
        @ValueSource(strings = {"9999.99", "1E+3", "175.500"})
        @DisplayName("Accepts valid boundary and scientific notation on update")
        void acceptsValidBoundaryOnUpdate(String validNumeric) {
            com.vhvkhangg.personalprivatevault.people.internal.domain.Person existing =
                    new com.vhvkhangg.personalprivatevault.people.internal.domain.Person(
                            1L, "Existing", null, null, null, null, null, null, null
                    );
            when(personRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(personRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

            BigDecimal val = new BigDecimal(validNumeric);
            UpdatePersonCommand command = new UpdatePersonCommand(
                    1L, "Existing", null, null, null,
                    val, val, null, null
            );
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> personService.update(command));
        }
    }

    @Nested
    @DisplayName("Person Role Validation")
    class RoleValidation {

        @Test
        @DisplayName("Rejects addRole with null personId")
        void rejectsNullPersonIdInAddRole() {
            assertThatThrownBy(() -> personService.addRole(null, PersonRole.ACTOR))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("personId must not be null");
        }

        @Test
        @DisplayName("Rejects addRole with null role")
        void rejectsNullRoleInAddRole() {
            assertThatThrownBy(() -> personService.addRole(1L, null))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("role must not be null");
        }

        @Test
        @DisplayName("Throws PersonNotFoundException when adding role to non-existent person")
        void throwsNotFoundWhenAddingRoleToMissingPerson() {
            when(personRepository.existsById(999L)).thenReturn(false);
            assertThatThrownBy(() -> personService.addRole(999L, PersonRole.SINGER))
                    .isInstanceOf(PersonNotFoundException.class)
                    .hasMessageContaining("Person not found with id: 999");
        }

        @Test
        @DisplayName("Rejects getRoles with null personId")
        void rejectsNullPersonIdInGetRoles() {
            assertThatThrownBy(() -> personService.getRoles(null))
                    .isInstanceOf(InvalidPersonException.class)
                    .hasMessageContaining("personId must not be null");
        }

        @Test
        @DisplayName("Throws PersonNotFoundException when getting roles of non-existent person")
        void throwsNotFoundWhenGettingRolesOfMissingPerson() {
            when(personRepository.existsById(999L)).thenReturn(false);
            assertThatThrownBy(() -> personService.getRoles(999L))
                    .isInstanceOf(PersonNotFoundException.class)
                    .hasMessageContaining("Person not found with id: 999");
        }
    }
}
