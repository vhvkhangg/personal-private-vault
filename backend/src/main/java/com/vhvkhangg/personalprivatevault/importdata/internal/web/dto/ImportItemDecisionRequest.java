package com.vhvkhangg.personalprivatevault.importdata.internal.web.dto;

import com.vhvkhangg.personalprivatevault.importdata.enums.ImportItemDecision;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ImportItemDecisionRequest(
        @Min(0) int itemIndex,
        @NotNull ImportItemDecision decision
) {}
