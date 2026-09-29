package com.vhvkhangg.personalprivatevault.people;

import com.vhvkhangg.personalprivatevault.people.group.command.CreateCreatorGroupCommand;
import com.vhvkhangg.personalprivatevault.people.group.command.UpdateCreatorGroupCommand;
import com.vhvkhangg.personalprivatevault.people.group.exception.CreatorGroupNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.people.group.exception.CreatorGroupNotFoundException;
import com.vhvkhangg.personalprivatevault.people.group.exception.InvalidCreatorGroupException;
import com.vhvkhangg.personalprivatevault.people.internal.application.CreatorGroupService;
import com.vhvkhangg.personalprivatevault.people.internal.domain.CreatorGroup;
import com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence.CreatorGroupMemberRepository;
import com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence.CreatorGroupRepository;
import com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence.PersonRepository;
import com.vhvkhangg.personalprivatevault.people.person.exception.PersonNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreatorGroupValidationTest {

    private CreatorGroupRepository creatorGroupRepository;
    private CreatorGroupMemberRepository creatorGroupMemberRepository;
    private PersonRepository personRepository;
    private CreatorGroupService creatorGroupService;

    @BeforeEach
    void setUp() {
        creatorGroupRepository = mock(CreatorGroupRepository.class);
        creatorGroupMemberRepository = mock(CreatorGroupMemberRepository.class);
        personRepository = mock(PersonRepository.class);
        creatorGroupService = new CreatorGroupService(
                creatorGroupRepository,
                creatorGroupMemberRepository,
                personRepository
        );
    }

    @Nested
    @DisplayName("Creator Group Creation Validation")
    class CreateGroupValidation {

        @Test
        @DisplayName("Rejects null create command")
        void rejectsNullCreateCommand() {
            assertThatThrownBy(() -> creatorGroupService.create(null))
                    .isInstanceOf(InvalidCreatorGroupException.class)
                    .hasMessageContaining("must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Rejects blank group name")
        void rejectsBlankGroupName(String blankName) {
            CreateCreatorGroupCommand command = new CreateCreatorGroupCommand(blankName, "Description");
            assertThatThrownBy(() -> creatorGroupService.create(command))
                    .isInstanceOf(InvalidCreatorGroupException.class)
                    .hasMessageContaining("Creator group name must not be blank");
        }

        @Test
        @DisplayName("Rejects group name exceeding 255 characters")
        void rejectsOverlyLongGroupName() {
            String longName = "G".repeat(256);
            CreateCreatorGroupCommand command = new CreateCreatorGroupCommand(longName, "Description");
            assertThatThrownBy(() -> creatorGroupService.create(command))
                    .isInstanceOf(InvalidCreatorGroupException.class)
                    .hasMessageContaining("exceed 255 characters");
        }

        @Test
        @DisplayName("Throws CreatorGroupNameAlreadyExistsException on exact duplicate name pre-check")
        void throwsOnDuplicateGroupName() {
            when(creatorGroupRepository.existsByName("The Beatles")).thenReturn(true);
            CreateCreatorGroupCommand command = new CreateCreatorGroupCommand("The Beatles", "Rock band");
            assertThatThrownBy(() -> creatorGroupService.create(command))
                    .isInstanceOf(CreatorGroupNameAlreadyExistsException.class)
                    .hasMessageContaining("The Beatles");
        }
    }

    @Nested
    @DisplayName("Creator Group Update Validation")
    class UpdateGroupValidation {

        @Test
        @DisplayName("Rejects null update command")
        void rejectsNullUpdateCommand() {
            assertThatThrownBy(() -> creatorGroupService.update(null))
                    .isInstanceOf(InvalidCreatorGroupException.class)
                    .hasMessageContaining("must not be null");
        }

        @Test
        @DisplayName("Rejects update command with null id")
        void rejectsNullIdInUpdate() {
            UpdateCreatorGroupCommand command = new UpdateCreatorGroupCommand(null, "New Name", "Desc");
            assertThatThrownBy(() -> creatorGroupService.update(command))
                    .isInstanceOf(InvalidCreatorGroupException.class)
                    .hasMessageContaining("Creator group id must not be null");
        }

        @Test
        @DisplayName("Throws CreatorGroupNotFoundException when updating non-existent group")
        void throwsNotFoundOnNonExistentGroup() {
            when(creatorGroupRepository.findById(999L)).thenReturn(Optional.empty());
            UpdateCreatorGroupCommand command = new UpdateCreatorGroupCommand(999L, "New Name", "Desc");
            assertThatThrownBy(() -> creatorGroupService.update(command))
                    .isInstanceOf(CreatorGroupNotFoundException.class)
                    .hasMessageContaining("Creator group not found with id: 999");
        }

        @Test
        @DisplayName("Throws CreatorGroupNameAlreadyExistsException when updating name to one used by another group")
        void throwsWhenUpdatingNameToExistingOtherGroup() {
            CreatorGroup group = mock(CreatorGroup.class);
            when(group.getId()).thenReturn(1L);
            when(creatorGroupRepository.findById(1L)).thenReturn(Optional.of(group));
            CreatorGroup other = mock(CreatorGroup.class);
            when(other.getId()).thenReturn(2L);
            when(creatorGroupRepository.findByName("Taken Name")).thenReturn(Optional.of(other));
            UpdateCreatorGroupCommand command = new UpdateCreatorGroupCommand(1L, "Taken Name", "Desc");
            assertThatThrownBy(() -> creatorGroupService.update(command))
                    .isInstanceOf(CreatorGroupNameAlreadyExistsException.class)
                    .hasMessageContaining("Taken Name");
        }
    }

    @Nested
    @DisplayName("Creator Group Membership Validation")
    class MembershipValidation {

        @Test
        @DisplayName("Rejects addMember with null groupId")
        void rejectsNullGroupIdInAddMember() {
            assertThatThrownBy(() -> creatorGroupService.addMember(null, 1L))
                    .isInstanceOf(InvalidCreatorGroupException.class)
                    .hasMessageContaining("groupId must not be null");
        }

        @Test
        @DisplayName("Rejects addMember with null personId")
        void rejectsNullPersonIdInAddMember() {
            assertThatThrownBy(() -> creatorGroupService.addMember(1L, null))
                    .isInstanceOf(InvalidCreatorGroupException.class)
                    .hasMessageContaining("personId must not be null");
        }

        @Test
        @DisplayName("Throws CreatorGroupNotFoundException when adding member to non-existent group")
        void throwsNotFoundWhenGroupMissingInAddMember() {
            when(creatorGroupRepository.existsById(999L)).thenReturn(false);
            assertThatThrownBy(() -> creatorGroupService.addMember(999L, 1L))
                    .isInstanceOf(CreatorGroupNotFoundException.class)
                    .hasMessageContaining("Creator group not found with id: 999");
        }

        @Test
        @DisplayName("Throws PersonNotFoundException when adding non-existent person to group")
        void throwsNotFoundWhenPersonMissingInAddMember() {
            when(creatorGroupRepository.existsById(1L)).thenReturn(true);
            when(personRepository.existsById(888L)).thenReturn(false);
            assertThatThrownBy(() -> creatorGroupService.addMember(1L, 888L))
                    .isInstanceOf(PersonNotFoundException.class)
                    .hasMessageContaining("Person not found with id: 888");
        }

        @Test
        @DisplayName("Rejects getMembers with null groupId")
        void rejectsNullGroupIdInGetMembers() {
            assertThatThrownBy(() -> creatorGroupService.getMembers(null))
                    .isInstanceOf(InvalidCreatorGroupException.class)
                    .hasMessageContaining("groupId must not be null");
        }

        @Test
        @DisplayName("Throws CreatorGroupNotFoundException when getting members of non-existent group")
        void throwsNotFoundWhenGroupMissingInGetMembers() {
            when(creatorGroupRepository.existsById(999L)).thenReturn(false);
            assertThatThrownBy(() -> creatorGroupService.getMembers(999L))
                    .isInstanceOf(CreatorGroupNotFoundException.class)
                    .hasMessageContaining("Creator group not found with id: 999");
        }
    }
}
