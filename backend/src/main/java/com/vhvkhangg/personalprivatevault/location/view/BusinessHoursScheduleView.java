package com.vhvkhangg.personalprivatevault.location.view;

import java.util.List;

/**
 * Immutable view of a Location's complete business hours schedule.
 */
public record BusinessHoursScheduleView(
        Long locationId,
        boolean businessHoursKnown,
        List<BusinessHoursIntervalView> intervals
) {
}
