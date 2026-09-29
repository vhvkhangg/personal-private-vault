package com.vhvkhangg.personalprivatevault.people.internal.application;

import com.vhvkhangg.personalprivatevault.people.enums.PersonRole;
import com.vhvkhangg.personalprivatevault.people.internal.domain.Person;
import com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence.PersonRepository;
import com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence.PersonRoleRepository;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.person.command.UpdatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.person.exception.InvalidPersonException;
import com.vhvkhangg.personalprivatevault.people.person.exception.PersonNotFoundException;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Application service implementing {@link PersonOperations}.
 */
@Service
public class PersonService implements PersonOperations {

    private final PersonRepository personRepository;
    private final PersonRoleRepository personRoleRepository;
    private final VaultEntryOperations vaultEntryOperations;
    private final ReferenceCatalog referenceCatalog;

    @Autowired
    public PersonService(
            PersonRepository personRepository,
            PersonRoleRepository personRoleRepository,
            VaultEntryOperations vaultEntryOperations,
            ReferenceCatalog referenceCatalog) {
        this.personRepository = Objects.requireNonNull(personRepository, "personRepository must not be null");
        this.personRoleRepository = Objects.requireNonNull(personRoleRepository, "personRoleRepository must not be null");
        this.vaultEntryOperations = Objects.requireNonNull(vaultEntryOperations, "vaultEntryOperations must not be null");
        this.referenceCatalog = Objects.requireNonNull(referenceCatalog, "referenceCatalog must not be null");
    }

    @Override
    @Transactional
    public PersonView create(CreatePersonCommand command) {
        if (command == null) {
            throw new InvalidPersonException("CreatePersonCommand must not be null");
        }
        ValidatedProfile profile = validateProfile(
                command.name(),
                command.avatarUrl(),
                command.heightCm(),
                command.weightKg(),
                command.nationalityCode(),
                command.notes()
        );

        VaultEntryView vaultEntry = vaultEntryOperations.create(VaultEntryType.PERSON);
        Person person = new Person(
                vaultEntry.id(),
                profile.name(),
                profile.avatarUrl(),
                command.gender(),
                command.birthDate(),
                profile.heightCm(),
                profile.weightKg(),
                profile.nationalityCode(),
                profile.notes()
        );
        Person saved = personRepository.save(person);
        return toView(saved, Collections.emptySet());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonView> find(Long personId) {
        if (personId == null) {
            return Optional.empty();
        }
        return personRepository.findById(personId).map(person -> {
            Set<PersonRole> roles = loadRoles(personId);
            return toView(person, roles);
        });
    }

    @Override
    @Transactional
    public PersonView update(UpdatePersonCommand command) {
        if (command == null) {
            throw new InvalidPersonException("UpdatePersonCommand must not be null");
        }
        if (command.id() == null) {
            throw new InvalidPersonException("Person id must not be null");
        }
        ValidatedProfile profile = validateProfile(
                command.name(),
                command.avatarUrl(),
                command.heightCm(),
                command.weightKg(),
                command.nationalityCode(),
                command.notes()
        );

        Person person = personRepository.findById(command.id())
                .orElseThrow(() -> new PersonNotFoundException(command.id()));

        person.update(
                profile.name(),
                profile.avatarUrl(),
                command.gender(),
                command.birthDate(),
                profile.heightCm(),
                profile.weightKg(),
                profile.nationalityCode(),
                profile.notes()
        );
        Person saved = personRepository.save(person);
        Set<PersonRole> roles = loadRoles(command.id());
        return toView(saved, roles);
    }

    @Override
    @Transactional
    public void addRole(Long personId, PersonRole role) {
        if (personId == null) {
            throw new InvalidPersonException("personId must not be null");
        }
        if (role == null) {
            throw new InvalidPersonException("role must not be null");
        }
        if (!personRepository.existsById(personId)) {
            throw new PersonNotFoundException(personId);
        }
        personRoleRepository.insertRoleIfAbsent(personId, role.name());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PersonRole> getRoles(Long personId) {
        if (personId == null) {
            throw new InvalidPersonException("personId must not be null");
        }
        if (!personRepository.existsById(personId)) {
            throw new PersonNotFoundException(personId);
        }
        return loadRoles(personId);
    }

    private Set<PersonRole> loadRoles(Long personId) {
        return personRoleRepository.findByIdPersonId(personId).stream()
                .map(assignment -> assignment.getId().getRole())
                .collect(Collectors.toUnmodifiableSet());
    }

    private record ValidatedProfile(
            String name,
            String avatarUrl,
            BigDecimal heightCm,
            BigDecimal weightKg,
            String nationalityCode,
            String notes
    ) {}

    private ValidatedProfile validateProfile(
            String rawName,
            String rawAvatarUrl,
            BigDecimal rawHeightCm,
            BigDecimal rawWeightKg,
            String rawNationalityCode,
            String rawNotes) {
        return new ValidatedProfile(
                validateName(rawName),
                validateAvatarUrl(rawAvatarUrl),
                validatePositiveDecimal(rawHeightCm, "Height"),
                validatePositiveDecimal(rawWeightKg, "Weight"),
                validateNationality(rawNationalityCode),
                validateNotes(rawNotes)
        );
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidPersonException("Person name must not be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 255) {
            throw new InvalidPersonException("Person name must not exceed 255 characters");
        }
        return trimmed;
    }

    private String validateAvatarUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return null;
        }
        String trimmed = avatarUrl.trim();
        if (trimmed.length() > 2048) {
            throw new InvalidPersonException("Avatar URL must not exceed 2048 characters");
        }
        return trimmed;
    }

    private BigDecimal validatePositiveDecimal(BigDecimal value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPersonException(fieldName + " must be positive when present");
        }
        BigDecimal normalized = value.stripTrailingZeros();
        int scale = Math.max(0, normalized.scale());
        int integerDigits = normalized.precision() - normalized.scale();
        if (scale > 2 || integerDigits > 4) {
            throw new InvalidPersonException(fieldName + " must fit numeric(6,2)");
        }
        return value;
    }

    private String validateNationality(String nationalityCode) {
        if (nationalityCode == null || nationalityCode.isBlank()) {
            return null;
        }
        String trimmed = nationalityCode.trim();
        if (trimmed.length() != 2) {
            throw new InvalidPersonException("Nationality code must be a 2-character ISO country code: " + nationalityCode);
        }
        String normalized = trimmed.toUpperCase(Locale.ROOT);
        if (referenceCatalog.country(normalized).isEmpty()) {
            throw new InvalidPersonException("Nationality code '" + normalized + "' does not exist in reference catalog");
        }
        return normalized;
    }

    private String validateNotes(String notes) {
        if (notes == null || notes.isBlank()) {
            return null;
        }
        return notes.trim();
    }

    private PersonView toView(Person person, Set<PersonRole> roles) {
        return new PersonView(
                person.getId(),
                person.getName(),
                person.getAvatarUrl(),
                person.getGender(),
                person.getBirthDate(),
                person.getHeightCm(),
                person.getWeightKg(),
                person.getNationalityCode(),
                person.getNotes(),
                roles
        );
    }
}
