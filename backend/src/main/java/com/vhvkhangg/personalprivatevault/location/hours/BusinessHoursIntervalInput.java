package com.vhvkhangg.personalprivatevault.location.hours;

import com.vhvkhangg.personalprivatevault.location.enums.DayOfWeek;

import java.time.LocalTime;

/**
 * Single business hours interval input.
 */
public record BusinessHoursIntervalInput(
        DayOfWeek dayOfWeek,
        LocalTime openTime,
        LocalTime closeTime
) {
}
