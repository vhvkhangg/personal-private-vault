package com.vhvkhangg.personalprivatevault.location.internal.web.dto;

import jakarta.validation.Valid;

import java.util.List;

public record ReplaceBusinessHoursScheduleRequest(
        boolean businessHoursKnown,
        List<@Valid BusinessHoursIntervalDto> intervals
) {
}
