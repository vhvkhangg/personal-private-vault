package com.vhvkhangg.personalprivatevault.collection.software.software;

import com.vhvkhangg.personalprivatevault.collection.software.view.SoftwareItemView;
import com.vhvkhangg.personalprivatevault.collection.software.view.SoftwarePlatformView;

import java.util.List;
import java.util.Optional;

/**
 * Public capability interface for the {@code collection.software} nested module.
 */
public interface SoftwareOperations {

    SoftwareItemView create(CreateSoftwareItemCommand command);

    SoftwareItemView update(Long id, UpdateSoftwareItemCommand command);

    Optional<SoftwareItemView> findById(Long id);

    void addPlatform(Long softwareId, Long platformId);

    List<SoftwarePlatformView> findPlatforms(Long softwareId, int limit);
}
