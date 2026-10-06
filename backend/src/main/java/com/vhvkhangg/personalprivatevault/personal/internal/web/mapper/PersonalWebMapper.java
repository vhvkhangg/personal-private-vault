package com.vhvkhangg.personalprivatevault.personal.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.personal.internal.web.dto.CreatePersonalProfileRequest;
import com.vhvkhangg.personalprivatevault.personal.internal.web.dto.PersonalProfileResponse;
import com.vhvkhangg.personalprivatevault.personal.internal.web.dto.UpdatePersonalProfileRequest;
import com.vhvkhangg.personalprivatevault.personal.profile.command.CreatePersonalProfileCommand;
import com.vhvkhangg.personalprivatevault.personal.profile.command.UpdatePersonalProfileCommand;
import com.vhvkhangg.personalprivatevault.personal.view.PersonalProfileView;

public final class PersonalWebMapper {

    private PersonalWebMapper() {}

    public static CreatePersonalProfileCommand toCommand(CreatePersonalProfileRequest request) {
        return new CreatePersonalProfileCommand(
                request.name(),
                request.relationship(),
                request.isSelf(),
                request.gender(),
                request.birthDate(),
                request.nationalityCode(),
                request.phone(),
                request.email(),
                request.addressId(),
                request.occupation(),
                request.notesMarkdown()
        );
    }

    public static UpdatePersonalProfileCommand toCommand(Long id, UpdatePersonalProfileRequest request) {
        return new UpdatePersonalProfileCommand(
                id,
                request.name(),
                request.relationship(),
                request.isSelf(),
                request.gender(),
                request.birthDate(),
                request.nationalityCode(),
                request.phone(),
                request.email(),
                request.addressId(),
                request.occupation(),
                request.notesMarkdown()
        );
    }

    public static PersonalProfileResponse toResponse(PersonalProfileView view) {
        return new PersonalProfileResponse(
                view.id(),
                view.name(),
                view.relationship(),
                view.isSelf(),
                view.gender(),
                view.birthDate(),
                view.nationalityCode(),
                view.phone(),
                view.email(),
                view.addressId(),
                view.occupation(),
                view.notesMarkdown(),
                view.createdAt(),
                view.updatedAt(),
                view.deletedAt()
        );
    }
}
