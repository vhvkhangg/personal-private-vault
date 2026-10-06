package com.vhvkhangg.personalprivatevault.people.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.people.group.command.CreateCreatorGroupCommand;
import com.vhvkhangg.personalprivatevault.people.group.command.UpdateCreatorGroupCommand;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.CreateCreatorGroupRequest;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.CreatePersonRequest;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.CreatorGroupMemberResponse;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.CreatorGroupResponse;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.PersonResponse;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.UpdateCreatorGroupRequest;
import com.vhvkhangg.personalprivatevault.people.internal.web.dto.UpdatePersonRequest;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.person.command.UpdatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupMemberView;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupView;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;

public final class PeopleWebMapper {

    private PeopleWebMapper() {}

    public static PersonResponse toResponse(PersonView view) {
        if (view == null) return null;
        return new PersonResponse(
                view.id(),
                view.name(),
                view.avatarUrl(),
                view.gender(),
                view.birthDate(),
                view.heightCm(),
                view.weightKg(),
                view.nationalityCode(),
                view.notes(),
                view.roles()
        );
    }

    public static CreatePersonCommand toCommand(CreatePersonRequest request) {
        if (request == null) return null;
        return new CreatePersonCommand(
                request.name(),
                request.avatarUrl(),
                request.gender(),
                request.birthDate(),
                request.heightCm(),
                request.weightKg(),
                request.nationalityCode(),
                request.notes()
        );
    }

    public static UpdatePersonCommand toCommand(Long id, UpdatePersonRequest request) {
        if (request == null) return null;
        return new UpdatePersonCommand(
                id,
                request.name(),
                request.avatarUrl(),
                request.gender(),
                request.birthDate(),
                request.heightCm(),
                request.weightKg(),
                request.nationalityCode(),
                request.notes()
        );
    }

    public static CreatorGroupResponse toResponse(CreatorGroupView view) {
        if (view == null) return null;
        return new CreatorGroupResponse(
                view.id(),
                view.name(),
                view.description(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    public static CreatorGroupMemberResponse toResponse(CreatorGroupMemberView view) {
        if (view == null) return null;
        return new CreatorGroupMemberResponse(
                view.groupId(),
                view.personId(),
                view.personName()
        );
    }

    public static CreateCreatorGroupCommand toCommand(CreateCreatorGroupRequest request) {
        if (request == null) return null;
        return new CreateCreatorGroupCommand(
                request.name(),
                request.description()
        );
    }

    public static UpdateCreatorGroupCommand toCommand(Long id, UpdateCreatorGroupRequest request) {
        if (request == null) return null;
        return new UpdateCreatorGroupCommand(
                id,
                request.name(),
                request.description()
        );
    }
}
