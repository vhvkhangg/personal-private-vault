package com.vhvkhangg.personalprivatevault.people.internal.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entity mapping the {@code creator_group_members} join table.
 */
@Entity
@Table(name = "creator_group_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreatorGroupMember {

    @EmbeddedId
    private CreatorGroupMemberId id;

    public CreatorGroupMember(CreatorGroupMemberId id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public CreatorGroupMember(Long creatorGroupId, Long personId) {
        this(new CreatorGroupMemberId(creatorGroupId, personId));
    }
}
