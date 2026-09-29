package com.vhvkhangg.personalprivatevault.location.hours;

import java.util.List;

/**
 * Command to atomically replace the complete business hours schedule for a Location.
 */
public record ReplaceBusinessHoursScheduleCommand(
        Long locationId,
        boolean businessHoursKnown,
        List<BusinessHoursIntervalInput> intervals
) {
}
