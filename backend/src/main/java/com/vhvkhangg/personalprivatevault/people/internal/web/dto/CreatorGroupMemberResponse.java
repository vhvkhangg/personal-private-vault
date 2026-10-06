package com.vhvkhangg.personalprivatevault.people.internal.web.dto;

public record CreatorGroupMemberResponse(
        Long groupId,
        Long personId,
        String personName
) {
}
