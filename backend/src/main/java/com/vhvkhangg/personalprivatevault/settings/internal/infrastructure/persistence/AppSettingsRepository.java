package com.vhvkhangg.personalprivatevault.settings.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.settings.internal.domain.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppSettingsRepository extends JpaRepository<AppSettings, Short> {
}
