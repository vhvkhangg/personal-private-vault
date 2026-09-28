package com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.people.enums.PersonRole;
import com.vhvkhangg.personalprivatevault.people.internal.domain.PersonRoleAssignment;
import com.vhvkhangg.personalprivatevault.people.internal.domain.PersonRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for {@link PersonRoleAssignment} join entities.
 */
public interface PersonRoleRepository extends JpaRepository<PersonRoleAssignment, PersonRoleId> {

    List<PersonRoleAssignment> findByIdPersonId(Long personId);

    boolean existsByIdPersonIdAndIdRole(Long personId, PersonRole role);

    @Modifying(flushAutomatically = true)
    @Query(
            value = "INSERT INTO person_roles (person_id, role) VALUES (:personId, cast(:role as person_role)) ON CONFLICT (person_id, role) DO NOTHING",
            nativeQuery = true
    )
    void insertRoleIfAbsent(@Param("personId") Long personId, @Param("role") String role);
}
