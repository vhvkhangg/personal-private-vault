package com.vhvkhangg.personalprivatevault.people.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite primary key for {@link CreatorGroupMember}.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class CreatorGroupMemberId implements Serializable {

    @Column(name = "creator_group_id", nullable = false)
    private Long creatorGroupId;

    @Column(name = "person_id", nullable = false)
    private Long personId;
}
