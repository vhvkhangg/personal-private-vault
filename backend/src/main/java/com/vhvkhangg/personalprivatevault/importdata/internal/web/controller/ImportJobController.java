package com.vhvkhangg.personalprivatevault.importdata.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.importdata.internal.web.dto.CreateImportJobRequest;
import com.vhvkhangg.personalprivatevault.importdata.internal.web.dto.ExecuteImportJobRequest;
import com.vhvkhangg.personalprivatevault.importdata.internal.web.dto.ImportJobItemResponse;
import com.vhvkhangg.personalprivatevault.importdata.internal.web.dto.ImportJobResponse;
import com.vhvkhangg.personalprivatevault.importdata.internal.web.dto.ParseImportJobRequest;
import com.vhvkhangg.personalprivatevault.importdata.internal.web.mapper.ImportDataWebMapper;
import com.vhvkhangg.personalprivatevault.importdata.job.ImportJobOperations;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobItemView;
import com.vhvkhangg.personalprivatevault.importdata.view.ImportJobView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/imports/jobs")
@RequiredArgsConstructor
@Validated
@Tag(name = "ImportJob", description = "Import job lifecycle: create, parse, validate, execute, cancel, and review items")
public class ImportJobController {

    private final ImportJobOperations importJobOperations;

    @PostMapping
    @Operation(summary = "Create an import job", operationId = "createImportJob")
    public ResponseEntity<ApiResponse<ImportJobResponse>> create(@Valid @RequestBody CreateImportJobRequest request) {
        ImportJobView created = importJobOperations.createJob(ImportDataWebMapper.toCommand(request));
        return ApiResponses.created(ImportDataWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get import job by ID", operationId = "getImportJob")
    public ResponseEntity<ApiResponse<ImportJobResponse>> get(@PathVariable Long id) {
        return importJobOperations.findJobById(id)
                .map(view -> ApiResponses.ok(ImportDataWebMapper.toResponse(view)))
                .orElseGet(() -> ApiResponses.of(HttpStatus.NOT_FOUND, "IMPORT_JOB_NOT_FOUND", "Import job not found"));
    }

    @GetMapping
    @Operation(summary = "Find recent import jobs", operationId = "findRecentImportJobs")
    public ResponseEntity<ApiResponse<List<ImportJobResponse>>> findRecent(
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<ImportJobView> jobs = importJobOperations.findRecentJobs(limit);
        return ApiResponses.ok(jobs.stream().map(ImportDataWebMapper::toResponse).toList());
    }

    @PostMapping("/{id}/parse")
    @Operation(summary = "Parse raw text for an import job", operationId = "parseImportJob")
    public ResponseEntity<ApiResponse<ImportJobResponse>> parse(
            @PathVariable Long id,
            @Valid @RequestBody ParseImportJobRequest request) {
        ImportJobView updated = importJobOperations.parse(id, request.rawText());
        return ApiResponses.ok(ImportDataWebMapper.toResponse(updated));
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Validate parsed items for an import job", operationId = "validateImportJob")
    public ResponseEntity<ApiResponse<ImportJobResponse>> validate(@PathVariable Long id) {
        ImportJobView updated = importJobOperations.validate(id);
        return ApiResponses.ok(ImportDataWebMapper.toResponse(updated));
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute import job with decisions", operationId = "executeImportJob")
    public ResponseEntity<ApiResponse<ImportJobResponse>> execute(
            @PathVariable Long id,
            @Valid @RequestBody ExecuteImportJobRequest request) {
        ImportJobView updated = importJobOperations.execute(id, ImportDataWebMapper.toCommand(request));
        return ApiResponses.ok(ImportDataWebMapper.toResponse(updated));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an import job", operationId = "cancelImportJob")
    public ResponseEntity<ApiResponse<ImportJobResponse>> cancel(@PathVariable Long id) {
        ImportJobView updated = importJobOperations.cancel(id);
        return ApiResponses.ok(ImportDataWebMapper.toResponse(updated));
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Find items for an import job", operationId = "findImportJobItems")
    public ResponseEntity<ApiResponse<List<ImportJobItemResponse>>> findItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") @Positive @Max(100) int limit) {
        List<ImportJobItemView> items = importJobOperations.findJobItems(id, limit);
        return ApiResponses.ok(items.stream().map(ImportDataWebMapper::toResponse).toList());
    }
}
