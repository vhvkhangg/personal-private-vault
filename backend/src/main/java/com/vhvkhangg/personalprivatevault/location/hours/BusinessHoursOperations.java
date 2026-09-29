package com.vhvkhangg.personalprivatevault.location.hours;

import com.vhvkhangg.personalprivatevault.location.view.BusinessHoursScheduleView;

/**
 * Public capability-oriented operations for Location business hours schedules.
 */
public interface BusinessHoursOperations {

    /**
     * Atomically replaces the complete business hours schedule for a Location.
     * Derives per-day sequence from input order and serializes concurrent replacements per Location.
     *
     * @param command replace schedule command
     * @return the resulting schedule view
     * @throws BusinessHoursNotFoundException if the location does not exist
     * @throws InvalidBusinessHoursException if input parameters are invalid
     */
    BusinessHoursScheduleView replaceSchedule(ReplaceBusinessHoursScheduleCommand command);

    /**
     * Retrieves the business hours schedule for a Location.
     *
     * @param locationId location ID
     * @return the schedule view
     * @throws BusinessHoursNotFoundException if the location does not exist
     */
    BusinessHoursScheduleView getSchedule(Long locationId);
}
