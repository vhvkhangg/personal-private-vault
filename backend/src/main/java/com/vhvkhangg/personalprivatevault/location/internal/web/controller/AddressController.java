package com.vhvkhangg.personalprivatevault.location.internal.web.controller;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.location.address.AddressOperations;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.AddressResponse;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateAddressRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateAddressRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.mapper.LocationWebMapper;
import com.vhvkhangg.personalprivatevault.location.view.AddressView;
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
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Physical address management")
public class AddressController {

    private final AddressOperations addressOperations;

    @PostMapping
    @Operation(summary = "Create address", operationId = "createAddress")
    public ResponseEntity<ApiResponse<AddressResponse>> create(@Valid @RequestBody CreateAddressRequest request) {
        AddressView created = addressOperations.create(LocationWebMapper.toCommand(request));
        return ApiResponses.created(LocationWebMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID", operationId = "getAddress")
    public ResponseEntity<ApiResponse<AddressResponse>> get(@PathVariable Long id) {
        AddressView address = addressOperations.findById(id);
        return ApiResponses.ok(LocationWebMapper.toResponse(address));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address", operationId = "updateAddress")
    public ResponseEntity<ApiResponse<AddressResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAddressRequest request) {
        AddressView updated = addressOperations.update(LocationWebMapper.toCommand(id, request));
        return ApiResponses.ok(LocationWebMapper.toResponse(updated));
    }
}
