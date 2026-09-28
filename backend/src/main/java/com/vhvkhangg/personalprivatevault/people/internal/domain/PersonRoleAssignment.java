package com.vhvkhangg.personalprivatevault.people.internal.domain;

import com.vhvkhangg.personalprivatevault.people.enums.PersonRole;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Entity mapping the {@code person_roles} join table.
 */
@Entity
@Table(name = "person_roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonRoleAssignment {

    @EmbeddedId
    private PersonRoleId id;

    public PersonRoleAssignment(PersonRoleId id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public PersonRoleAssignment(Long personId, PersonRole role) {
        this(new PersonRoleId(personId, role));
    }
}
