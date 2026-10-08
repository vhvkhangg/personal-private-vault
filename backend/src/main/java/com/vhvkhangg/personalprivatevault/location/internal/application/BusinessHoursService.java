package com.vhvkhangg.personalprivatevault.location.internal.application;

import com.vhvkhangg.personalprivatevault.location.enums.DayOfWeek;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursIntervalInput;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursNotFoundException;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursOperations;
import com.vhvkhangg.personalprivatevault.location.hours.InvalidBusinessHoursException;
import com.vhvkhangg.personalprivatevault.location.hours.ReplaceBusinessHoursScheduleCommand;
import com.vhvkhangg.personalprivatevault.location.internal.domain.Location;
import com.vhvkhangg.personalprivatevault.location.internal.domain.LocationBusinessHour;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationBusinessHourRepository;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationRepository;
import com.vhvkhangg.personalprivatevault.location.view.BusinessHoursIntervalView;
import com.vhvkhangg.personalprivatevault.location.view.BusinessHoursScheduleView;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BusinessHoursService implements BusinessHoursOperations {

    private final LocationRepository locationRepository;
    private final LocationBusinessHourRepository businessHourRepository;
    private final EntityManager entityManager;

    @Autowired
    public BusinessHoursService(
            LocationRepository locationRepository,
            LocationBusinessHourRepository businessHourRepository,
            EntityManager entityManager
    ) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepository must not be null");
        this.businessHourRepository = Objects.requireNonNull(businessHourRepository, "businessHourRepository must not be null");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null");
    }

    @Override
    @Transactional
    public BusinessHoursScheduleView replaceSchedule(ReplaceBusinessHoursScheduleCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        if (command.locationId() == null) {
            throw new InvalidBusinessHoursException("locationId must not be null");
        }

        Location location = locationRepository.findByIdForUpdate(command.locationId())
                .orElseThrow(() -> new BusinessHoursNotFoundException(command.locationId()));
        entityManager.refresh(location);

        if (!command.businessHoursKnown()) {
            if (command.intervals() != null && !command.intervals().isEmpty()) {
                throw new InvalidBusinessHoursException("Intervals must be empty when business hours are unknown");
            }
            businessHourRepository.deleteByLocationId(location.getId());
            location.setBusinessHoursKnown(false);
            locationRepository.save(location);
            return new BusinessHoursScheduleView(location.getId(), false, List.of());
        }

        businessHourRepository.deleteByLocationId(location.getId());
        location.setBusinessHoursKnown(true);
        locationRepository.save(location);

        List<BusinessHoursIntervalInput> inputs = command.intervals() != null ? command.intervals() : List.of();
        Map<DayOfWeek, Integer> perDaySequence = new EnumMap<>(DayOfWeek.class);
        List<LocationBusinessHour> hoursToSave = new ArrayList<>(inputs.size());
        List<BusinessHoursIntervalView> views = new ArrayList<>(inputs.size());

        for (BusinessHoursIntervalInput input : inputs) {
            if (input == null) {
                throw new InvalidBusinessHoursException("Interval input must not be null");
            }
            if (input.dayOfWeek() == null) {
                throw new InvalidBusinessHoursException("dayOfWeek must not be null");
            }
            if (input.openTime() == null) {
                throw new InvalidBusinessHoursException("openTime must not be null");
            }
            if (input.closeTime() == null) {
                throw new InvalidBusinessHoursException("closeTime must not be null");
            }

            int seq = perDaySequence.compute(input.dayOfWeek(), (k, v) -> v == null ? 1 : v + 1);
            LocationBusinessHour entity = new LocationBusinessHour(
                    location.getId(),
                    input.dayOfWeek(),
                    seq,
                    input.openTime(),
                    input.closeTime()
            );
            hoursToSave.add(entity);
            views.add(new BusinessHoursIntervalView(input.dayOfWeek(), seq, input.openTime(), input.closeTime()));
        }

        if (!hoursToSave.isEmpty()) {
            businessHourRepository.saveAll(hoursToSave);
        }

        return new BusinessHoursScheduleView(location.getId(), true, views);
    }

    @Override
    @Transactional
    public BusinessHoursScheduleView getSchedule(Long locationId) {
        if (locationId == null) {
            throw new InvalidBusinessHoursException("locationId must not be null");
        }

        Location location = locationRepository.findByIdForShare(locationId)
                .orElseThrow(() -> new BusinessHoursNotFoundException(locationId));
        entityManager.refresh(location);

        List<LocationBusinessHour> hours = businessHourRepository.findByLocationIdOrderByDayOfWeekAscSequenceAsc(locationId);
        List<BusinessHoursIntervalView> intervalViews = hours.stream()
                .map(h -> new BusinessHoursIntervalView(h.getDayOfWeek(), h.getSequence(), h.getOpenTime(), h.getCloseTime()))
                .toList();

        return new BusinessHoursScheduleView(location.getId(), location.isBusinessHoursKnown(), intervalViews);
    }
}
