package com.vhvkhangg.personalprivatevault.location.internal.web.advice;

import com.vhvkhangg.personalprivatevault.ApiResponse;
import com.vhvkhangg.personalprivatevault.ApiResponses;
import com.vhvkhangg.personalprivatevault.location.address.AddressNotFoundException;
import com.vhvkhangg.personalprivatevault.location.address.InvalidAddressException;
import com.vhvkhangg.personalprivatevault.location.brand.BrandNotFoundException;
import com.vhvkhangg.personalprivatevault.location.brand.InvalidBrandException;
import com.vhvkhangg.personalprivatevault.location.category.InvalidLocationCategoryException;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryNotFoundException;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursNotFoundException;
import com.vhvkhangg.personalprivatevault.location.hours.InvalidBusinessHoursException;
import com.vhvkhangg.personalprivatevault.location.location.InvalidLocationException;
import com.vhvkhangg.personalprivatevault.location.location.LocationNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.vhvkhangg.personalprivatevault.location.internal.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LocationExceptionAdvice {

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAddressNotFound(AddressNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Address not found");
    }

    @ExceptionHandler(InvalidAddressException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidAddress(InvalidAddressException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "ADDRESS_INVALID", "Invalid address data");
    }

    @ExceptionHandler(BrandNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleBrandNotFound(BrandNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "BRAND_NOT_FOUND", "Brand not found");
    }

    @ExceptionHandler(InvalidBrandException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBrand(InvalidBrandException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "BRAND_INVALID", "Invalid brand data");
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleLocationNotFound(LocationNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "LOCATION_NOT_FOUND", "Location not found");
    }

    @ExceptionHandler(InvalidLocationException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidLocation(InvalidLocationException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "LOCATION_INVALID", "Invalid location data");
    }

    @ExceptionHandler(LocationCategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleLocationCategoryNotFound(LocationCategoryNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "LOCATION_CATEGORY_NOT_FOUND", "Location category not found");
    }

    @ExceptionHandler(LocationCategoryNameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryNameExists(LocationCategoryNameAlreadyExistsException ex) {
        return ApiResponses.of(HttpStatus.CONFLICT, "LOCATION_CATEGORY_NAME_EXISTS", "Location category name already exists");
    }

    @ExceptionHandler(InvalidLocationCategoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidLocationCategory(InvalidLocationCategoryException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "LOCATION_CATEGORY_INVALID", "Invalid location category data");
    }

    @ExceptionHandler(BusinessHoursNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessHoursNotFound(BusinessHoursNotFoundException ex) {
        return ApiResponses.of(HttpStatus.NOT_FOUND, "BUSINESS_HOURS_NOT_FOUND", "Business hours not found");
    }

    @ExceptionHandler(InvalidBusinessHoursException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBusinessHours(InvalidBusinessHoursException ex) {
        return ApiResponses.of(HttpStatus.UNPROCESSABLE_CONTENT, "BUSINESS_HOURS_INVALID", "Invalid business hours schedule");
    }
}
