package com.vhvkhangg.personalprivatevault.location.internal.web.mapper;

import com.vhvkhangg.personalprivatevault.location.address.CreateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.address.UpdateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.brand.CreateBrandCommand;
import com.vhvkhangg.personalprivatevault.location.brand.UpdateBrandCommand;
import com.vhvkhangg.personalprivatevault.location.category.CreateLocationCategoryCommand;
import com.vhvkhangg.personalprivatevault.location.category.UpdateLocationCategoryCommand;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursIntervalInput;
import com.vhvkhangg.personalprivatevault.location.hours.ReplaceBusinessHoursScheduleCommand;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.AddressResponse;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.BrandResponse;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.BusinessHoursIntervalDto;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.BusinessHoursScheduleResponse;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateAddressRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateBrandRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateLocationCategoryRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateLocationRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.LocationCategoryResponse;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.LocationResponse;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.ReplaceBusinessHoursScheduleRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateAddressRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateBrandRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateLocationCategoryRequest;
import com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateLocationRequest;
import com.vhvkhangg.personalprivatevault.location.location.CreateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.location.UpdateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.view.AddressView;
import com.vhvkhangg.personalprivatevault.location.view.BrandView;
import com.vhvkhangg.personalprivatevault.location.view.BusinessHoursIntervalView;
import com.vhvkhangg.personalprivatevault.location.view.BusinessHoursScheduleView;
import com.vhvkhangg.personalprivatevault.location.view.LocationCategoryView;
import com.vhvkhangg.personalprivatevault.location.view.LocationView;

import java.util.List;

public final class LocationWebMapper {

    private LocationWebMapper() {}

    public static AddressResponse toResponse(AddressView view) {
        if (view == null) return null;
        return new AddressResponse(
                view.id(),
                view.label(),
                view.addressType(),
                view.countryCode(),
                view.administrativeArea(),
                view.locality(),
                view.sublocality(),
                view.streetAddress(),
                view.postalCode(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    public static CreateAddressCommand toCommand(CreateAddressRequest request) {
        if (request == null) return null;
        return new CreateAddressCommand(
                request.label(),
                request.addressType(),
                request.countryCode(),
                request.administrativeArea(),
                request.locality(),
                request.sublocality(),
                request.streetAddress(),
                request.postalCode()
        );
    }

    public static UpdateAddressCommand toCommand(Long id, UpdateAddressRequest request) {
        if (request == null) return null;
        return new UpdateAddressCommand(
                id,
                request.label(),
                request.addressType(),
                request.countryCode(),
                request.administrativeArea(),
                request.locality(),
                request.sublocality(),
                request.streetAddress(),
                request.postalCode()
        );
    }

    public static BrandResponse toResponse(BrandView view) {
        if (view == null) return null;
        return new BrandResponse(
                view.id(),
                view.name(),
                view.logoUrl(),
                view.nationalityCode(),
                view.description(),
                view.minPrice(),
                view.maxPrice(),
                view.currencyCode(),
                view.review()
        );
    }

    public static CreateBrandCommand toCommand(CreateBrandRequest request) {
        if (request == null) return null;
        return new CreateBrandCommand(
                request.name(),
                request.logoUrl(),
                request.nationalityCode(),
                request.description(),
                request.minPrice(),
                request.maxPrice(),
                request.currencyCode(),
                request.review()
        );
    }

    public static UpdateBrandCommand toCommand(Long id, UpdateBrandRequest request) {
        if (request == null) return null;
        return new UpdateBrandCommand(
                id,
                request.name(),
                request.logoUrl(),
                request.nationalityCode(),
                request.description(),
                request.minPrice(),
                request.maxPrice(),
                request.currencyCode(),
                request.review()
        );
    }

    public static LocationCategoryResponse toResponse(LocationCategoryView view) {
        if (view == null) return null;
        return new LocationCategoryResponse(view.id(), view.name(), view.description());
    }

    public static CreateLocationCategoryCommand toCommand(CreateLocationCategoryRequest request) {
        if (request == null) return null;
        return new CreateLocationCategoryCommand(request.name(), request.description());
    }

    public static UpdateLocationCategoryCommand toCommand(Long id, UpdateLocationCategoryRequest request) {
        if (request == null) return null;
        return new UpdateLocationCategoryCommand(id, request.name(), request.description());
    }

    public static LocationResponse toResponse(LocationView view) {
        if (view == null) return null;
        return new LocationResponse(
                view.id(),
                view.brandId(),
                view.addressId(),
                view.name(),
                view.imageUrl(),
                view.description(),
                view.phone(),
                view.websiteUrl(),
                view.businessHoursKnown(),
                view.minPrice(),
                view.maxPrice(),
                view.currencyCode(),
                view.review()
        );
    }

    public static CreateLocationCommand toCommand(CreateLocationRequest request) {
        if (request == null) return null;
        return new CreateLocationCommand(
                request.brandId(),
                request.addressId(),
                request.name(),
                request.imageUrl(),
                request.description(),
                request.phone(),
                request.websiteUrl(),
                request.minPrice(),
                request.maxPrice(),
                request.currencyCode(),
                request.review()
        );
    }

    public static UpdateLocationCommand toCommand(Long id, UpdateLocationRequest request) {
        if (request == null) return null;
        return new UpdateLocationCommand(
                id,
                request.brandId(),
                request.addressId(),
                request.name(),
                request.imageUrl(),
                request.description(),
                request.phone(),
                request.websiteUrl(),
                request.minPrice(),
                request.maxPrice(),
                request.currencyCode(),
                request.review()
        );
    }

    public static BusinessHoursIntervalDto toDto(BusinessHoursIntervalView view) {
        if (view == null) return null;
        return new BusinessHoursIntervalDto(
                view.dayOfWeek(),
                view.sequence(),
                view.openTime(),
                view.closeTime()
        );
    }

    public static BusinessHoursScheduleResponse toResponse(BusinessHoursScheduleView view) {
        if (view == null) return null;
        List<BusinessHoursIntervalDto> intervals = view.intervals() != null
                ? view.intervals().stream().map(LocationWebMapper::toDto).toList()
                : List.of();
        return new BusinessHoursScheduleResponse(
                view.locationId(),
                view.businessHoursKnown(),
                intervals
        );
    }

    public static ReplaceBusinessHoursScheduleCommand toCommand(Long locationId, ReplaceBusinessHoursScheduleRequest request) {
        if (request == null) return null;
        List<BusinessHoursIntervalInput> inputs = request.intervals() != null
                ? request.intervals().stream()
                .map(dto -> new BusinessHoursIntervalInput(dto.dayOfWeek(), dto.openTime(), dto.closeTime()))
                .toList()
                : List.of();
        return new ReplaceBusinessHoursScheduleCommand(
                locationId,
                request.businessHoursKnown(),
                inputs
        );
    }
}
