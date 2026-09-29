package com.vhvkhangg.personalprivatevault.location;

import com.vhvkhangg.personalprivatevault.location.address.AddressNotFoundException;
import com.vhvkhangg.personalprivatevault.location.address.CreateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.address.InvalidAddressException;
import com.vhvkhangg.personalprivatevault.location.address.UpdateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.brand.BrandNotFoundException;
import com.vhvkhangg.personalprivatevault.location.brand.CreateBrandCommand;
import com.vhvkhangg.personalprivatevault.location.brand.InvalidBrandException;
import com.vhvkhangg.personalprivatevault.location.brand.UpdateBrandCommand;
import com.vhvkhangg.personalprivatevault.location.category.CreateLocationCategoryCommand;
import com.vhvkhangg.personalprivatevault.location.category.InvalidLocationCategoryException;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryNotFoundException;
import com.vhvkhangg.personalprivatevault.location.category.UpdateLocationCategoryCommand;
import com.vhvkhangg.personalprivatevault.location.enums.DayOfWeek;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursIntervalInput;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursNotFoundException;
import com.vhvkhangg.personalprivatevault.location.hours.InvalidBusinessHoursException;
import com.vhvkhangg.personalprivatevault.location.hours.ReplaceBusinessHoursScheduleCommand;
import com.vhvkhangg.personalprivatevault.location.internal.application.AddressService;
import com.vhvkhangg.personalprivatevault.location.internal.application.BrandService;
import com.vhvkhangg.personalprivatevault.location.internal.application.BusinessHoursService;
import com.vhvkhangg.personalprivatevault.location.internal.application.LocationCategoryService;
import com.vhvkhangg.personalprivatevault.location.internal.application.LocationService;
import com.vhvkhangg.personalprivatevault.location.internal.domain.Location;
import com.vhvkhangg.personalprivatevault.location.internal.domain.LocationCategory;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.AddressRepository;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.BrandRepository;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationBusinessHourRepository;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationCategoryAssignmentRepository;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationCategoryRepository;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationDiningServiceStyleRepository;
import com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationRepository;
import com.vhvkhangg.personalprivatevault.location.location.CreateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.location.InvalidLocationException;
import com.vhvkhangg.personalprivatevault.location.location.LocationNotFoundException;
import com.vhvkhangg.personalprivatevault.location.location.UpdateLocationCommand;
import com.vhvkhangg.personalprivatevault.reference.catalog.ReferenceCatalog;
import com.vhvkhangg.personalprivatevault.reference.view.CountryView;
import com.vhvkhangg.personalprivatevault.reference.view.CurrencyView;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationValidationTest {

    private BrandRepository brandRepository;
    private AddressRepository addressRepository;
    private LocationCategoryRepository categoryRepository;
    private LocationCategoryAssignmentRepository categoryAssignmentRepository;
    private LocationRepository locationRepository;
    private LocationDiningServiceStyleRepository diningStyleRepository;
    private LocationBusinessHourRepository businessHourRepository;
    private VaultEntryOperations vaultEntryOperations;
    private ReferenceCatalog referenceCatalog;

    private BrandService brandService;
    private AddressService addressService;
    private LocationCategoryService categoryService;
    private LocationService locationService;
    private BusinessHoursService businessHoursService;

    @BeforeEach
    void setUp() {
        brandRepository = mock(BrandRepository.class);
        addressRepository = mock(AddressRepository.class);
        categoryRepository = mock(LocationCategoryRepository.class);
        categoryAssignmentRepository = mock(LocationCategoryAssignmentRepository.class);
        locationRepository = mock(LocationRepository.class);
        diningStyleRepository = mock(LocationDiningServiceStyleRepository.class);
        businessHourRepository = mock(LocationBusinessHourRepository.class);
        vaultEntryOperations = mock(VaultEntryOperations.class);
        referenceCatalog = mock(ReferenceCatalog.class);

        when(referenceCatalog.country("VN")).thenReturn(Optional.of(mock(CountryView.class)));
        when(referenceCatalog.country("US")).thenReturn(Optional.of(mock(CountryView.class)));
        when(referenceCatalog.currency("VND")).thenReturn(Optional.of(mock(CurrencyView.class)));
        when(referenceCatalog.currency("USD")).thenReturn(Optional.of(mock(CurrencyView.class)));

        brandService = new BrandService(brandRepository, vaultEntryOperations, referenceCatalog);
        addressService = new AddressService(addressRepository, referenceCatalog);
        categoryService = new LocationCategoryService(categoryRepository, categoryAssignmentRepository);
        locationService = new LocationService(
                locationRepository, brandRepository, addressRepository,
                diningStyleRepository, vaultEntryOperations, referenceCatalog
        );
        businessHoursService = new BusinessHoursService(locationRepository, businessHourRepository);
    }

    @Nested
    @DisplayName("Brand validation")
    class BrandValidation {

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Blank brand name throws InvalidBrandException")
        void blankNameThrows(String name) {
            CreateBrandCommand cmd = new CreateBrandCommand(name, null, null, null, null, null, null, null);
            assertThatThrownBy(() -> brandService.create(cmd))
                    .isInstanceOf(InvalidBrandException.class)
                    .hasMessageContaining("Brand name must not be blank");
        }

        @Test
        @DisplayName("Invalid nationality code throws InvalidBrandException")
        void invalidCountryThrows() {
            when(referenceCatalog.country("XX")).thenReturn(Optional.empty());
            CreateBrandCommand cmd = new CreateBrandCommand("Brand", null, "XX", null, null, null, null, null);
            assertThatThrownBy(() -> brandService.create(cmd))
                    .isInstanceOf(InvalidBrandException.class)
                    .hasMessageContaining("Nationality code 'XX' does not exist");
        }

        @Test
        @DisplayName("Invalid currency code throws InvalidBrandException")
        void invalidCurrencyThrows() {
            when(referenceCatalog.currency("XYZ")).thenReturn(Optional.empty());
            CreateBrandCommand cmd = new CreateBrandCommand("Brand", null, null, null, null, null, "XYZ", null);
            assertThatThrownBy(() -> brandService.create(cmd))
                    .isInstanceOf(InvalidBrandException.class)
                    .hasMessageContaining("Currency code 'XYZ' does not exist");
        }

        @Test
        @DisplayName("Negative min price throws InvalidBrandException")
        void negativeMinPriceThrows() {
            CreateBrandCommand cmd = new CreateBrandCommand(
                    "Brand", null, null, null, new BigDecimal("-10"), null, "USD", null
            );
            assertThatThrownBy(() -> brandService.create(cmd))
                    .isInstanceOf(InvalidBrandException.class)
                    .hasMessageContaining("minPrice must be non-negative");
        }

        @Test
        @DisplayName("Negative max price throws InvalidBrandException")
        void negativeMaxPriceThrows() {
            CreateBrandCommand cmd = new CreateBrandCommand(
                    "Brand", null, null, null, null, new BigDecimal("-5"), "USD", null
            );
            assertThatThrownBy(() -> brandService.create(cmd))
                    .isInstanceOf(InvalidBrandException.class)
                    .hasMessageContaining("maxPrice must be non-negative");
        }

        @Test
        @DisplayName("minPrice exceeding maxPrice throws InvalidBrandException")
        void minPriceExceedsMaxPriceThrows() {
            CreateBrandCommand cmd = new CreateBrandCommand(
                    "Brand", null, null, null, new BigDecimal("100"), new BigDecimal("50"), "USD", null
            );
            assertThatThrownBy(() -> brandService.create(cmd))
                    .isInstanceOf(InvalidBrandException.class)
                    .hasMessageContaining("minPrice cannot exceed maxPrice");
        }

        @Test
        @DisplayName("Price specified without currency throws InvalidBrandException")
        void priceWithoutCurrencyThrows() {
            CreateBrandCommand cmd = new CreateBrandCommand(
                    "Brand", null, null, null, new BigDecimal("100"), null, null, null
            );
            assertThatThrownBy(() -> brandService.create(cmd))
                    .isInstanceOf(InvalidBrandException.class)
                    .hasMessageContaining("currencyCode is required when minPrice or maxPrice is specified");
        }

        @Test
        @DisplayName("Update brand not found throws BrandNotFoundException")
        void updateNotFoundThrows() {
            when(brandRepository.findById(999L)).thenReturn(Optional.empty());
            UpdateBrandCommand cmd = new UpdateBrandCommand(999L, "Brand", null, null, null, null, null, null, null);
            assertThatThrownBy(() -> brandService.update(cmd))
                    .isInstanceOf(BrandNotFoundException.class)
                    .hasMessageContaining("Brand not found with id: 999");
        }
    }

    @Nested
    @DisplayName("Address validation")
    class AddressValidation {

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Blank country code throws InvalidAddressException")
        void blankCountryThrows(String code) {
            CreateAddressCommand cmd = new CreateAddressCommand(null, null, code, null, null, null, null, null);
            assertThatThrownBy(() -> addressService.create(cmd))
                    .isInstanceOf(InvalidAddressException.class)
                    .hasMessageContaining("countryCode must not be blank");
        }

        @Test
        @DisplayName("Unknown country code throws InvalidAddressException")
        void unknownCountryThrows() {
            when(referenceCatalog.country("ZZ")).thenReturn(Optional.empty());
            CreateAddressCommand cmd = new CreateAddressCommand(null, null, "ZZ", null, null, null, null, null);
            assertThatThrownBy(() -> addressService.create(cmd))
                    .isInstanceOf(InvalidAddressException.class)
                    .hasMessageContaining("Country code 'ZZ' does not exist");
        }

        @Test
        @DisplayName("Street address exceeding 500 chars throws InvalidAddressException")
        void streetAddressTooLongThrows() {
            String longStreet = "Street ".repeat(100);
            CreateAddressCommand cmd = new CreateAddressCommand(null, null, "VN", null, null, null, longStreet, null);
            assertThatThrownBy(() -> addressService.create(cmd))
                    .isInstanceOf(InvalidAddressException.class)
                    .hasMessageContaining("Street address must not exceed 500 characters");
        }

        @Test
        @DisplayName("Update address not found throws AddressNotFoundException")
        void updateNotFoundThrows() {
            when(addressRepository.findById(999L)).thenReturn(Optional.empty());
            UpdateAddressCommand cmd = new UpdateAddressCommand(999L, null, null, "VN", null, null, null, null, null);
            assertThatThrownBy(() -> addressService.update(cmd))
                    .isInstanceOf(AddressNotFoundException.class)
                    .hasMessageContaining("Address not found with id: 999");
        }
    }

    @Nested
    @DisplayName("LocationCategory validation")
    class LocationCategoryValidation {

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Blank category name throws InvalidLocationCategoryException")
        void blankNameThrows(String name) {
            assertThatThrownBy(() -> categoryService.create(new CreateLocationCategoryCommand(name, null)))
                    .isInstanceOf(InvalidLocationCategoryException.class)
                    .hasMessageContaining("Category name must not be blank");
        }

        @Test
        @DisplayName("Category name exceeding 150 chars throws InvalidLocationCategoryException")
        void nameTooLongThrows() {
            String longName = "c".repeat(151);
            assertThatThrownBy(() -> categoryService.create(new CreateLocationCategoryCommand(longName, null)))
                    .isInstanceOf(InvalidLocationCategoryException.class)
                    .hasMessageContaining("Category name must not exceed 150 characters");
        }

        @Test
        @DisplayName("Existing category name throws LocationCategoryNameAlreadyExistsException")
        void duplicateNameThrows() {
            when(categoryRepository.findByNameIgnoreCase("Cafe")).thenReturn(Optional.of(mock(LocationCategory.class)));
            assertThatThrownBy(() -> categoryService.create(new CreateLocationCategoryCommand("Cafe", null)))
                    .isInstanceOf(LocationCategoryNameAlreadyExistsException.class)
                    .hasMessageContaining("Cafe");
        }

        @Test
        @DisplayName("Persistence conflict on category name throws LocationCategoryNameAlreadyExistsException")
        void persistenceConflictOnCategoryNameThrows() {
            when(categoryRepository.findByNameIgnoreCase("Bistro")).thenReturn(Optional.empty());
            when(categoryRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate key violates unique constraint uq_ci_location_categories_name"));
            assertThatThrownBy(() -> categoryService.create(new CreateLocationCategoryCommand("Bistro", null)))
                    .isInstanceOf(LocationCategoryNameAlreadyExistsException.class)
                    .hasMessageContaining("Bistro");
        }

        @Test
        @DisplayName("Unrelated DataIntegrityViolationException on create throws InvalidLocationCategoryException")
        void unrelatedDataIntegrityViolationOnCreateThrows() {
            when(categoryRepository.findByNameIgnoreCase("Bakery")).thenReturn(Optional.empty());
            when(categoryRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("check constraint failed"));
            assertThatThrownBy(() -> categoryService.create(new CreateLocationCategoryCommand("Bakery", null)))
                    .isInstanceOf(InvalidLocationCategoryException.class);
        }

        @Test
        @DisplayName("Persistence conflict on update category name throws LocationCategoryNameAlreadyExistsException")
        void persistenceConflictOnUpdateCategoryNameThrows() {
            LocationCategory existing = mock(LocationCategory.class);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(categoryRepository.findByNameIgnoreCase("Bistro")).thenReturn(Optional.empty());
            when(categoryRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate key violates unique constraint uq_ci_location_categories_name"));
            assertThatThrownBy(() -> categoryService.update(new UpdateLocationCategoryCommand(1L, "Bistro", null)))
                    .isInstanceOf(LocationCategoryNameAlreadyExistsException.class)
                    .hasMessageContaining("Bistro");
        }

        @Test
        @DisplayName("Find by non-existent name throws LocationCategoryNotFoundException")
        void findByNameNotFoundThrows() {
            when(categoryRepository.findByNameIgnoreCase("Unknown")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> categoryService.findByName("Unknown"))
                    .isInstanceOf(LocationCategoryNotFoundException.class)
                    .hasMessageContaining("Unknown");
        }
    }

    @Nested
    @DisplayName("Location validation")
    class LocationValidation {

        @Test
        @DisplayName("Null addressId throws InvalidLocationException")
        void nullAddressThrows() {
            CreateLocationCommand cmd = new CreateLocationCommand(
                    null, null, "Coffee Shop", null, null, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> locationService.create(cmd))
                    .isInstanceOf(InvalidLocationException.class)
                    .hasMessageContaining("addressId must not be null");
        }

        @Test
        @DisplayName("Non-existent addressId throws InvalidLocationException")
        void nonExistentAddressThrows() {
            when(addressRepository.existsById(123L)).thenReturn(false);
            CreateLocationCommand cmd = new CreateLocationCommand(
                    null, 123L, "Coffee Shop", null, null, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> locationService.create(cmd))
                    .isInstanceOf(InvalidLocationException.class)
                    .hasMessageContaining("Address not found with id: 123");
        }

        @Test
        @DisplayName("Non-existent brandId throws InvalidLocationException")
        void nonExistentBrandThrows() {
            when(addressRepository.existsById(1L)).thenReturn(true);
            when(brandRepository.existsById(888L)).thenReturn(false);
            CreateLocationCommand cmd = new CreateLocationCommand(
                    888L, 1L, "Coffee Shop", null, null, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> locationService.create(cmd))
                    .isInstanceOf(InvalidLocationException.class)
                    .hasMessageContaining("Brand not found with id: 888");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Blank location name throws InvalidLocationException")
        void blankNameThrows(String name) {
            when(addressRepository.existsById(1L)).thenReturn(true);
            CreateLocationCommand cmd = new CreateLocationCommand(
                    null, 1L, name, null, null, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> locationService.create(cmd))
                    .isInstanceOf(InvalidLocationException.class)
                    .hasMessageContaining("Location name must not be blank");
        }

        @Test
        @DisplayName("Location update not found throws LocationNotFoundException")
        void updateNotFoundThrows() {
            when(locationRepository.findById(999L)).thenReturn(Optional.empty());
            UpdateLocationCommand cmd = new UpdateLocationCommand(
                    999L, null, 1L, "Loc", null, null, null, null, null, null, null, null
            );
            assertThatThrownBy(() -> locationService.update(cmd))
                    .isInstanceOf(LocationNotFoundException.class)
                    .hasMessageContaining("Location not found with id: 999");
        }
    }

    @Nested
    @DisplayName("BusinessHours validation")
    class BusinessHoursValidation {

        @Test
        @DisplayName("Null locationId throws InvalidBusinessHoursException")
        void nullLocationIdThrows() {
            ReplaceBusinessHoursScheduleCommand cmd = new ReplaceBusinessHoursScheduleCommand(
                    null, false, List.of()
            );
            assertThatThrownBy(() -> businessHoursService.replaceSchedule(cmd))
                    .isInstanceOf(InvalidBusinessHoursException.class)
                    .hasMessageContaining("locationId must not be null");
        }

        @Test
        @DisplayName("Non-existent location throws BusinessHoursNotFoundException")
        void nonExistentLocationThrows() {
            when(locationRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());
            ReplaceBusinessHoursScheduleCommand cmd = new ReplaceBusinessHoursScheduleCommand(
                    999L, false, List.of()
            );
            assertThatThrownBy(() -> businessHoursService.replaceSchedule(cmd))
                    .isInstanceOf(BusinessHoursNotFoundException.class);
        }

        @Test
        @DisplayName("Unknown hours schedule with intervals throws InvalidBusinessHoursException")
        void unknownHoursWithIntervalsThrows() {
            Location mockLoc = mock(Location.class);
            when(locationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(mockLoc));
            List<BusinessHoursIntervalInput> intervals = List.of(
                    new BusinessHoursIntervalInput(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))
            );
            ReplaceBusinessHoursScheduleCommand cmd = new ReplaceBusinessHoursScheduleCommand(
                    1L, false, intervals
            );
            assertThatThrownBy(() -> businessHoursService.replaceSchedule(cmd))
                    .isInstanceOf(InvalidBusinessHoursException.class)
                    .hasMessageContaining("Intervals must be empty when business hours are unknown");
        }

        @Test
        @DisplayName("Known hours with null interval parameters throws InvalidBusinessHoursException")
        void knownHoursWithNullIntervalParamsThrows() {
            Location mockLoc = mock(Location.class);
            when(locationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(mockLoc));
            List<BusinessHoursIntervalInput> intervals = List.of(
                    new BusinessHoursIntervalInput(null, LocalTime.of(9, 0), LocalTime.of(17, 0))
            );
            ReplaceBusinessHoursScheduleCommand cmd = new ReplaceBusinessHoursScheduleCommand(
                    1L, true, intervals
            );
            assertThatThrownBy(() -> businessHoursService.replaceSchedule(cmd))
                    .isInstanceOf(InvalidBusinessHoursException.class)
                    .hasMessageContaining("dayOfWeek must not be null");
        }
    }
}
