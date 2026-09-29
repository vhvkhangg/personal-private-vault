package com.vhvkhangg.personalprivatevault.location.view;

import com.vhvkhangg.personalprivatevault.location.enums.DayOfWeek;

import java.time.LocalTime;

/**
 * Immutable view of a single business hours interval.
 */
public record BusinessHoursIntervalView(
        DayOfWeek dayOfWeek,
        int sequence,
        LocalTime openTime,
        LocalTime closeTime
) {
}
