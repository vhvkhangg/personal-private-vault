package com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.reference.internal.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, String> {
    List<Country> findAllByOrderByCodeAsc();
}
