package com.vhvkhangg.personalprivatevault.people.internal.application;

import com.vhvkhangg.personalprivatevault.people.group.CreatorGroupOperations;
import com.vhvkhangg.personalprivatevault.people.group.command.CreateCreatorGroupCommand;
import com.vhvkhangg.personalprivatevault.people.group.command.UpdateCreatorGroupCommand;
import com.vhvkhangg.personalprivatevault.people.group.exception.CreatorGroupNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.people.group.exception.CreatorGroupNotFoundException;
import com.vhvkhangg.personalprivatevault.people.group.exception.InvalidCreatorGroupException;
import com.vhvkhangg.personalprivatevault.people.internal.domain.CreatorGroup;
import com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence.CreatorGroupMemberRepository;
import com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence.CreatorGroupRepository;
import com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence.PersonRepository;
import com.vhvkhangg.personalprivatevault.people.person.exception.PersonNotFoundException;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupMemberView;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service implementing {@link CreatorGroupOperations}.
 */
@Service
public class CreatorGroupService implements CreatorGroupOperations {

    private final CreatorGroupRepository creatorGroupRepository;
    private final CreatorGroupMemberRepository creatorGroupMemberRepository;
    private final PersonRepository personRepository;
    private final Clock clock;

    @Autowired
    public CreatorGroupService(
            CreatorGroupRepository creatorGroupRepository,
            CreatorGroupMemberRepository creatorGroupMemberRepository,
            PersonRepository personRepository,
            @Autowired(required = false) Clock clock) {
        this.creatorGroupRepository = Objects.requireNonNull(creatorGroupRepository, "creatorGroupRepository must not be null");
        this.creatorGroupMemberRepository = Objects.requireNonNull(creatorGroupMemberRepository, "creatorGroupMemberRepository must not be null");
        this.personRepository = Objects.requireNonNull(personRepository, "personRepository must not be null");
        this.clock = clock != null ? clock : Clock.systemUTC();
    }

    public CreatorGroupService(
            CreatorGroupRepository creatorGroupRepository,
            CreatorGroupMemberRepository creatorGroupMemberRepository,
            PersonRepository personRepository) {
        this(creatorGroupRepository, creatorGroupMemberRepository, personRepository, Clock.systemUTC());
    }

    @Override
    @Transactional
    public CreatorGroupView create(CreateCreatorGroupCommand command) {
        if (command == null) {
            throw new InvalidCreatorGroupException("CreateCreatorGroupCommand must not be null");
        }
        String name = validateName(command.name());
        String description = validateDescription(command.description());

        if (creatorGroupRepository.existsByName(name)) {
            throw new CreatorGroupNameAlreadyExistsException(name);
        }

        try {
            CreatorGroup created = creatorGroupRepository.saveAndFlush(
                    new CreatorGroup(name, description, clock.instant())
            );
            return toView(created);
        } catch (DataIntegrityViolationException ex) {
            throw new CreatorGroupNameAlreadyExistsException(name, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CreatorGroupView> find(Long groupId) {
        if (groupId == null) {
            return Optional.empty();
        }
        return creatorGroupRepository.findById(groupId).map(this::toView);
    }

    @Override
    @Transactional
    public CreatorGroupView update(UpdateCreatorGroupCommand command) {
        if (command == null) {
            throw new InvalidCreatorGroupException("UpdateCreatorGroupCommand must not be null");
        }
        if (command.id() == null) {
            throw new InvalidCreatorGroupException("Creator group id must not be null");
        }
        String name = validateName(command.name());
        String description = validateDescription(command.description());

        CreatorGroup group = creatorGroupRepository.findById(command.id())
                .orElseThrow(() -> new CreatorGroupNotFoundException(command.id()));

        creatorGroupRepository.findByName(name).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), command.id())) {
                throw new CreatorGroupNameAlreadyExistsException(name);
            }
        });

        group.update(name, description, clock.instant());
        try {
            CreatorGroup saved = creatorGroupRepository.saveAndFlush(group);
            return toView(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new CreatorGroupNameAlreadyExistsException(name, ex);
        }
    }

    @Override
    @Transactional
    public void addMember(Long groupId, Long personId) {
        if (groupId == null) {
            throw new InvalidCreatorGroupException("groupId must not be null");
        }
        if (personId == null) {
            throw new InvalidCreatorGroupException("personId must not be null");
        }
        if (!creatorGroupRepository.existsById(groupId)) {
            throw new CreatorGroupNotFoundException(groupId);
        }
        if (!personRepository.existsById(personId)) {
            throw new PersonNotFoundException(personId);
        }

        creatorGroupMemberRepository.insertMemberIfAbsent(groupId, personId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreatorGroupMemberView> getMembers(Long groupId) {
        if (groupId == null) {
            throw new InvalidCreatorGroupException("groupId must not be null");
        }
        if (!creatorGroupRepository.existsById(groupId)) {
            throw new CreatorGroupNotFoundException(groupId);
        }
        return List.copyOf(creatorGroupMemberRepository.findMembersByGroupId(groupId));
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidCreatorGroupException("Creator group name must not be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 255) {
            throw new InvalidCreatorGroupException("Creator group name must not exceed 255 characters");
        }
        return trimmed;
    }

    private String validateDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private CreatorGroupView toView(CreatorGroup group) {
        return new CreatorGroupView(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}
