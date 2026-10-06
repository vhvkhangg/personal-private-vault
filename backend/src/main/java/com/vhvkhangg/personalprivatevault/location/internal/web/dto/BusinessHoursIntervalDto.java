package com.vhvkhangg.personalprivatevault.location.internal.web.dto;

import com.vhvkhangg.personalprivatevault.location.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record BusinessHoursIntervalDto(
        @NotNull(message = "Day of week must not be null")
        DayOfWeek dayOfWeek,
        int sequence,
        @NotNull(message = "Open time must not be null")
        LocalTime openTime,
        @NotNull(message = "Close time must not be null")
        LocalTime closeTime
) {
}
