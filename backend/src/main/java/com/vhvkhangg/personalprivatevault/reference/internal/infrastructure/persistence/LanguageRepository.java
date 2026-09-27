package com.vhvkhangg.personalprivatevault.reference.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.reference.internal.domain.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LanguageRepository extends JpaRepository<Language, String> {
    List<Language> findAllByOrderByCodeAsc();
}
