package com.vhvkhangg.personalprivatevault.people.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.people.internal.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Person} entities.
 */
public interface PersonRepository extends JpaRepository<Person, Long> {
}
