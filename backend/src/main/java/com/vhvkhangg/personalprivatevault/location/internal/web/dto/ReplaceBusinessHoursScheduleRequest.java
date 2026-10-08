package com.vhvkhangg.personalprivatevault.location.internal.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReplaceBusinessHoursScheduleRequest(
        boolean businessHoursKnown,
        List<@NotNull @Valid BusinessHoursIntervalDto> intervals
) {
}
