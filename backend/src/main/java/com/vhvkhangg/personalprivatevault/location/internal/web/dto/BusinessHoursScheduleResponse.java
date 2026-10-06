package com.vhvkhangg.personalprivatevault.location.internal.web.dto;

import java.util.List;

public record BusinessHoursScheduleResponse(
        Long locationId,
        boolean businessHoursKnown,
        List<BusinessHoursIntervalDto> intervals
) {
}
