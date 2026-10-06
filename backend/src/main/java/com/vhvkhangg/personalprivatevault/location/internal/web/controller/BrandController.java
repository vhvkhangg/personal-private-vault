package com.vhvkhangg.personalprivatevault.location.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.location.brand.BrandOperations;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.BrandResponse;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateBrandRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateBrandRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.mapper.LocationWebMapper;
import com.vhvkhangg.personalprivatevault.location.view.BrandView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Tag(name = "Brands", description = "Brand management")
public class BrandController {

    private final BrandOperations brandOperations;

    @PostMapping
    @Operation(summary = "Create brand", operationId = "createBrand")
    public ResponseEntity<ApiResponse<BrandResponse>> create(@Valid @RequestBody CreateBrandRequest request) {
        BrandView created = brandOperations.create(LocationWebMapper.toCommand(request));
        return ApiResponses.created(LocationWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID", operationId = "getBrand")
    public ResponseEntity<ApiResponse<BrandResponse>> get(@PathVariable Long id) {
        BrandView brand = brandOperations.findById(id);
        return ApiResponses.ok(LocationWebMapper.toResponse(brand));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update brand", operationId = "updateBrand")
    public ResponseEntity<ApiResponse<BrandResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBrandRequest request) {
        BrandView updated = brandOperations.update(LocationWebMapper.toCommand(id, request));
        return ApiResponses.ok(LocationWebMapper.toResponse(updated));
    }
}
