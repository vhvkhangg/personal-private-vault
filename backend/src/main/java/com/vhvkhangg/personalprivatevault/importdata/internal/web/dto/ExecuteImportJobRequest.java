package com.vhvkhangg.personalprivatevault.importdata.internal.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExecuteImportJobRequest(
        @NotNull @Valid List<ImportItemDecisionRequest> itemDecisions
) {}
