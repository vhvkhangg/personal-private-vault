package com.vhvkhangg.personalprivatevault.location.internal.application;

import com.vhvkhangg.personalprivatevault.location.category.CreateLocationCategoryCommand;
import com.vhvkhangg.personalprivatevault.location.category.InvalidLocationCategoryException;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryNotFoundException;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryOperations;
import com.vhvkhangg.personalprivatevault.location.category.UpdateLocationCategoryCommand;
import com.vhvkhangg.personalprivatevault.location.internal.domain.LocationCategory;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationCategoryAssignmentRepository;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationCategoryRepository;
import com.vhvkhangg.personalprivatevault.location.view.LocationCategoryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class LocationCategoryService implements LocationCategoryOperations {

    private final LocationCategoryRepository categoryRepository;
    private final LocationCategoryAssignmentRepository assignmentRepository;

    @Autowired
    public LocationCategoryService(
            LocationCategoryRepository categoryRepository,
            LocationCategoryAssignmentRepository assignmentRepository
    ) {
        this.categoryRepository = Objects.requireNonNull(categoryRepository, "categoryRepository must not be null");
        this.assignmentRepository = Objects.requireNonNull(assignmentRepository, "assignmentRepository must not be null");
    }

    @Override
    @Transactional
    public LocationCategoryView create(CreateLocationCategoryCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        String name = validateAndTrimName(command.name());
        String description = trimIfPresent(command.description());

        if (categoryRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new LocationCategoryNameAlreadyExistsException("Location category name already exists: " + name);
        }

        try {
            LocationCategory category = new LocationCategory(name, description);
            LocationCategory saved = categoryRepository.saveAndFlush(category);
            return toView(saved);
        } catch (DataIntegrityViolationException ex) {
            if (isCategoryNameUniqueViolation(ex)) {
                throw new LocationCategoryNameAlreadyExistsException("Location category name already exists: " + name, ex);
            }
            throw new InvalidLocationCategoryException("Location category creation violated data integrity: " + name, ex);
        }
    }

    @Override
    @Transactional
    public LocationCategoryView update(UpdateLocationCategoryCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        if (command.id() == null) {
            throw new InvalidLocationCategoryException("Category id must not be null");
        }

        LocationCategory category = categoryRepository.findById(command.id())
                .orElseThrow(() -> new LocationCategoryNotFoundException("Location category not found with id: " + command.id()));

        String name = validateAndTrimName(command.name());
        String description = trimIfPresent(command.description());

        categoryRepository.findByNameIgnoreCase(name)
                .filter(other -> !other.getId().equals(command.id()))
                .ifPresent(other -> {
                    throw new LocationCategoryNameAlreadyExistsException("Location category name already exists: " + name);
                });

        try {
            category.update(name, description);
            LocationCategory saved = categoryRepository.saveAndFlush(category);
            return toView(saved);
        } catch (DataIntegrityViolationException ex) {
            if (isCategoryNameUniqueViolation(ex)) {
                throw new LocationCategoryNameAlreadyExistsException("Location category name already exists: " + name, ex);
            }
            throw new InvalidLocationCategoryException("Location category update violated data integrity: " + name, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LocationCategoryView findById(Long id) {
        if (id == null) {
            throw new InvalidLocationCategoryException("Category id must not be null");
        }
        return categoryRepository.findById(id)
                .map(this::toView)
                .orElseThrow(() -> new LocationCategoryNotFoundException("Location category not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public LocationCategoryView findByName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidLocationCategoryException("Category name must not be blank");
        }
        return categoryRepository.findByNameIgnoreCase(name.trim())
                .map(this::toView)
                .orElseThrow(() -> new LocationCategoryNotFoundException("Location category not found with name: " + name));
    }

    @Override
    @Transactional
    public void assignCategoryToLocation(Long locationId, Long categoryId) {
        if (locationId == null) {
            throw new InvalidLocationCategoryException("locationId must not be null");
        }
        if (categoryId == null) {
            throw new InvalidLocationCategoryException("categoryId must not be null");
        }
        assignmentRepository.insertIfAbsent(locationId, categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationCategoryView> findCategoriesByLocationId(Long locationId) {
        if (locationId == null) {
            throw new InvalidLocationCategoryException("locationId must not be null");
        }
        List<Long> categoryIds = assignmentRepository.findCategoryIdsByLocationId(locationId);
        if (categoryIds.isEmpty()) {
            return List.of();
        }
        return categoryRepository.findAllById(categoryIds).stream()
                .map(this::toView)
                .toList();
    }

    private String validateAndTrimName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidLocationCategoryException("Category name must not be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 150) {
            throw new InvalidLocationCategoryException("Category name must not exceed 150 characters");
        }
        return trimmed;
    }

    private String trimIfPresent(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private LocationCategoryView toView(LocationCategory category) {
        return new LocationCategoryView(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

    private boolean isCategoryNameUniqueViolation(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException cve && cve.getConstraintName() != null) {
                String cName = cve.getConstraintName().toLowerCase(Locale.ROOT);
                if (cName.contains("uq_ci_location_categories_name") || cName.contains("location_categories_name")) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        Throwable root = ex.getRootCause();
        String rootMsg = root != null && root.getMessage() != null ? root.getMessage().toLowerCase(Locale.ROOT) : "";
        if (rootMsg.contains("uq_ci_location_categories_name") || rootMsg.contains("location_categories_name")) {
            return true;
        }
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase(Locale.ROOT) : "";
        return msg.contains("uq_ci_location_categories_name") || msg.contains("location_categories_name");
    }
}
