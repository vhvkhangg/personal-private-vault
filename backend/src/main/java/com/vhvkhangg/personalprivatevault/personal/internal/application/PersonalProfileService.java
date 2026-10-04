package com.vhvkhangg.personalprivatevault.personal.internal.application;

import com.vhvkhangg.personalprivatevault.location.address.AddressNotFoundException;
import com.vhvkhangg.personalprivatevault.location.address.AddressOperations;
import com.vhvkhangg.personalprivatevault.location.address.InvalidAddressException;
import com.vhvkhangg.personalprivatevault.personal.enums.Gender;
import com.vhvkhangg.personalprivatevault.personal.internal.domain.PersonalProfile;
import com.vhvkhangg.personalprivatevault.personal.internal.infrastructure.persistence.PersonalProfileRepository;
import com.vhvkhangg.personalprivatevault.personal.profile.PersonalProfileOperations;
import com.vhvkhangg.personalprivatevault.personal.profile.command.CreatePersonalProfileCommand;
import com.vhvkhangg.personalprivatevault.personal.profile.command.UpdatePersonalProfileCommand;
import com.vhvkhangg.personalprivatevault.personal.profile.exception.InvalidPersonalProfileException;
import com.vhvkhangg.personalprivatevault.personal.profile.exception.PersonalProfileConflictException;
import com.vhvkhangg.personalprivatevault.personal.profile.exception.PersonalProfileNotFoundException;
import com.vhvkhangg.personalprivatevault.personal.view.PersonalProfileView;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Application service implementing {@link PersonalProfileOperations}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalProfileService implements PersonalProfileOperations {

    private final PersonalProfileRepository personalProfileRepository;
    private final ReferenceCatalog referenceCatalog;
    private final AddressOperations addressOperations;

    @Override
    @Transactional
    public PersonalProfileView createProfile(CreatePersonalProfileCommand command) {
        if (command == null) {
            throw new InvalidPersonalProfileException("Command must not be null");
        }
        boolean isSelf = Boolean.TRUE.equals(command.isSelf());
        validateProfile(
                command.name(),
                command.relationship(),
                command.nationalityCode(),
                command.phone(),
                command.email(),
                command.addressId(),
                command.occupation()
        );

        if (isSelf && personalProfileRepository.existsByIsSelfTrueAndDeletedAtIsNull()) {
            throw new PersonalProfileConflictException("An active self profile already exists");
        }

        String phone = normalizeText(command.phone());
        String email = normalizeText(command.email());
        String occupation = normalizeText(command.occupation());
        String nationalityCode = normalizeText(command.nationalityCode());

        PersonalProfile profile = new PersonalProfile(
                command.name().trim(),
                command.relationship().trim(),
                isSelf,
                command.gender(),
                command.birthDate(),
                nationalityCode,
                phone,
                email,
                command.addressId(),
                occupation,
                command.notesMarkdown()
        );

        try {
            PersonalProfile saved = personalProfileRepository.saveAndFlush(profile);
            return toView(saved);
        } catch (DataIntegrityViolationException ex) {
            if (isActiveSelfUniqueViolation(ex)) {
                throw new PersonalProfileConflictException("An active self profile already exists");
            }
            throw ex;
        }
    }

    @Override
    @Transactional
    public PersonalProfileView updateProfile(UpdatePersonalProfileCommand command) {
        if (command == null) {
            throw new InvalidPersonalProfileException("Command must not be null");
        }
        if (command.id() == null) {
            throw new InvalidPersonalProfileException("Profile id must not be null");
        }
        boolean isSelf = Boolean.TRUE.equals(command.isSelf());
        validateProfile(
                command.name(),
                command.relationship(),
                command.nationalityCode(),
                command.phone(),
                command.email(),
                command.addressId(),
                command.occupation()
        );

        PersonalProfile profile = personalProfileRepository.findByIdAndDeletedAtIsNull(command.id())
                .orElseThrow(() -> new PersonalProfileNotFoundException(command.id()));

        if (isSelf && personalProfileRepository.existsByIsSelfTrueAndDeletedAtIsNullAndIdNot(profile.getId())) {
            throw new PersonalProfileConflictException("An active self profile already exists");
        }

        String phone = normalizeText(command.phone());
        String email = normalizeText(command.email());
        String occupation = normalizeText(command.occupation());
        String nationalityCode = normalizeText(command.nationalityCode());

        profile.update(
                command.name().trim(),
                command.relationship().trim(),
                isSelf,
                command.gender(),
                command.birthDate(),
                nationalityCode,
                phone,
                email,
                command.addressId(),
                occupation,
                command.notesMarkdown()
        );

        try {
            personalProfileRepository.flush();
            return toView(profile);
        } catch (DataIntegrityViolationException ex) {
            if (isActiveSelfUniqueViolation(ex)) {
                throw new PersonalProfileConflictException("An active self profile already exists");
            }
            throw ex;
        }
    }

    @Override
    public PersonalProfileView findProfileById(Long id) {
        if (id == null) {
            throw new InvalidPersonalProfileException("Profile id must not be null");
        }
        PersonalProfile profile = personalProfileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new PersonalProfileNotFoundException(id));
        return toView(profile);
    }

    @Override
    public Optional<PersonalProfileView> findSelfProfile() {
        return personalProfileRepository.findByIsSelfTrueAndDeletedAtIsNull()
                .map(this::toView);
    }

    @Override
    public List<PersonalProfileView> findProfiles(int limit) {
        if (limit <= 0) {
            throw new InvalidPersonalProfileException("Limit must be positive");
        }
        return personalProfileRepository.findByDeletedAtIsNullOrderByNameAscIdAsc(PageRequest.of(0, limit))
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional
    public PersonalProfileView softDeleteProfile(Long id) {
        if (id == null) {
            throw new InvalidPersonalProfileException("Profile id must not be null");
        }
        PersonalProfile profile = personalProfileRepository.findById(id)
                .orElseThrow(() -> new PersonalProfileNotFoundException(id));
        profile.softDelete();
        return toView(profile);
    }

    @Override
    @Transactional
    public PersonalProfileView restoreProfile(Long id) {
        if (id == null) {
            throw new InvalidPersonalProfileException("Profile id must not be null");
        }
        PersonalProfile profile = personalProfileRepository.findById(id)
                .orElseThrow(() -> new PersonalProfileNotFoundException(id));

        if (profile.getDeletedAt() == null) {
            return toView(profile);
        }

        if (profile.isSelf() && personalProfileRepository.existsByIsSelfTrueAndDeletedAtIsNullAndIdNot(profile.getId())) {
            throw new PersonalProfileConflictException("An active self profile already exists");
        }

        profile.restore();

        try {
            personalProfileRepository.flush();
            return toView(profile);
        } catch (DataIntegrityViolationException ex) {
            if (isActiveSelfUniqueViolation(ex)) {
                throw new PersonalProfileConflictException("An active self profile already exists");
            }
            throw ex;
        }
    }

    private void validateProfile(
            String name,
            String relationship,
            String nationalityCode,
            String phone,
            String email,
            Long addressId,
            String occupation
    ) {
        if (name == null || name.isBlank()) {
            throw new InvalidPersonalProfileException("name must not be blank");
        }
        if (name.length() > 255) {
            throw new InvalidPersonalProfileException("name must not exceed 255 characters");
        }
        if (relationship == null || relationship.isBlank()) {
            throw new InvalidPersonalProfileException("relationship must not be blank");
        }
        if (relationship.length() > 100) {
            throw new InvalidPersonalProfileException("relationship must not exceed 100 characters");
        }
        if (phone != null && phone.length() > 64) {
            throw new InvalidPersonalProfileException("phone must not exceed 64 characters");
        }
        if (email != null && email.length() > 320) {
            throw new InvalidPersonalProfileException("email must not exceed 320 characters");
        }
        if (occupation != null && occupation.length() > 255) {
            throw new InvalidPersonalProfileException("occupation must not exceed 255 characters");
        }
        String resolvedNationality = normalizeText(nationalityCode);
        if (resolvedNationality != null) {
            if (referenceCatalog.country(resolvedNationality).isEmpty()) {
                throw new InvalidPersonalProfileException("Unknown nationality code: " + resolvedNationality);
            }
        }
        if (addressId != null) {
            try {
                addressOperations.findById(addressId);
            } catch (AddressNotFoundException | InvalidAddressException e) {
                throw new InvalidPersonalProfileException("Address with id " + addressId + " does not exist", e);
            }
        }
    }

    private boolean isActiveSelfUniqueViolation(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException cve && cve.getConstraintName() != null) {
                String cName = cve.getConstraintName().toLowerCase(Locale.ROOT);
                if (cName.contains("uq_personal_profiles_one_active_self") || cName.contains("personal_profiles_one_active_self")) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        Throwable root = ex.getRootCause();
        String rootMsg = root != null && root.getMessage() != null ? root.getMessage().toLowerCase(Locale.ROOT) : "";
        if (rootMsg.contains("uq_personal_profiles_one_active_self") || rootMsg.contains("personal_profiles_one_active_self")) {
            return true;
        }
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase(Locale.ROOT) : "";
        return msg.contains("uq_personal_profiles_one_active_self") || msg.contains("personal_profiles_one_active_self");
    }

    private String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.trim();
    }

    private PersonalProfileView toView(PersonalProfile p) {
        return new PersonalProfileView(
                p.getId(),
                p.getName(),
                p.getRelationship(),
                p.isSelf(),
                p.getGender(),
                p.getBirthDate(),
                p.getNationalityCode(),
                p.getPhone(),
                p.getEmail(),
                p.getAddressId(),
                p.getOccupation(),
                p.getNotesMarkdown(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getDeletedAt()
        );
    }
}
