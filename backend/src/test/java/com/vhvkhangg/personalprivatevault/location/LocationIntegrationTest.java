package com.vhvkhangg.personalprivatevault.location;

import com.vhvkhangg.personalprivatevault.location.address.AddressOperations;
import com.vhvkhangg.personalprivatevault.location.address.CreateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.address.InvalidAddressException;
import com.vhvkhangg.personalprivatevault.location.address.UpdateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.brand.BrandOperations;
import com.vhvkhangg.personalprivatevault.location.brand.CreateBrandCommand;
import com.vhvkhangg.personalprivatevault.location.brand.InvalidBrandException;
import com.vhvkhangg.personalprivatevault.location.brand.UpdateBrandCommand;
import com.vhvkhangg.personalprivatevault.location.category.CreateLocationCategoryCommand;
import com.vhvkhangg.personalprivatevault.location.category.InvalidLocationCategoryException;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryOperations;
import com.vhvkhangg.personalprivatevault.location.category.UpdateLocationCategoryCommand;
import com.vhvkhangg.personalprivatevault.location.enums.DayOfWeek;
import com.vhvkhangg.personalprivatevault.location.enums.DiningServiceStyle;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursIntervalInput;
import com.vhvkhangg.personalprivatevault.location.hours.BusinessHoursOperations;
import com.vhvkhangg.personalprivatevault.location.hours.InvalidBusinessHoursException;
import com.vhvkhangg.personalprivatevault.location.hours.ReplaceBusinessHoursScheduleCommand;
import com.vhvkhangg.personalprivatevault.location.location.CreateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.location.InvalidLocationException;
import com.vhvkhangg.personalprivatevault.location.location.LocationOperations;
import com.vhvkhangg.personalprivatevault.location.location.UpdateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.view.AddressView;
import com.vhvkhangg.personalprivatevault.location.view.BrandView;
import com.vhvkhangg.personalprivatevault.location.view.BusinessHoursIntervalView;
import com.vhvkhangg.personalprivatevault.location.view.BusinessHoursScheduleView;
import com.vhvkhangg.personalprivatevault.location.view.LocationCategoryView;
import com.vhvkhangg.personalprivatevault.location.view.LocationView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private BrandOperations brandOperations;

    @Autowired
    private AddressOperations addressOperations;

    @Autowired
    private LocationCategoryOperations categoryOperations;

    @Autowired
    private LocationOperations locationOperations;

    @Autowired
    private BusinessHoursOperations hoursOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        tearDown();

        // Seed reference fixtures
        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('US', 'United States', 'Hoa Kỳ')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('VN', 'Vietnam', 'Việt Nam')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol) VALUES ('USD', 'US Dollar', '$')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol) VALUES ('VND', 'Vietnamese Dong', '₫')
                ON CONFLICT (code) DO NOTHING
                """);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM location_business_hours");
        jdbcTemplate.execute("DELETE FROM location_dining_service_styles");
        jdbcTemplate.execute("DELETE FROM location_category_assignments");
        jdbcTemplate.execute("DELETE FROM locations");
        jdbcTemplate.execute("DELETE FROM addresses");
        jdbcTemplate.execute("DELETE FROM brands");
        jdbcTemplate.execute("DELETE FROM location_categories");
        jdbcTemplate.execute("DELETE FROM vault_entries WHERE entry_type IN ('BRAND', 'LOCATION')");
    }

    // --- Brand Tests ---

    @Test
    @DisplayName("Creates and updates Brand backed by Vault Entry; allows duplicate brand names")
    void createAndUpdateBrand() {
        BrandView created = brandOperations.create(new CreateBrandCommand(
                "Acme Dining", "https://cdn.example.com/logo.png", "US", "Casual dining chain",
                new BigDecimal("10.00"), new BigDecimal("50.00"), "USD", "Great food and service"
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Acme Dining");
        assertThat(created.nationalityCode()).isEqualTo("US");
        assertThat(created.currencyCode()).isEqualTo("USD");

        // Verify Vault Entry exists with type BRAND
        String vaultType = jdbcTemplate.queryForObject(
                "SELECT entry_type FROM vault_entries WHERE id = ?",
                String.class,
                created.id()
        );
        assertThat(vaultType).isEqualTo("BRAND");

        // Update brand
        BrandView updated = brandOperations.update(new UpdateBrandCommand(
                created.id(), "Acme Dining International", "https://cdn.example.com/logo2.png",
                "VN", "Global casual dining",
                new BigDecimal("15.00"), new BigDecimal("60.00"), "USD", "Updated review"
        ));
        assertThat(updated.name()).isEqualTo("Acme Dining International");
        assertThat(updated.nationalityCode()).isEqualTo("VN");
        assertThat(updated.review()).isEqualTo("Updated review");

        // Brand names are non-unique: creating another brand with the same name succeeds
        BrandView duplicateName = brandOperations.create(new CreateBrandCommand(
                "Acme Dining International", null, null, null, null, null, null, null
        ));
        assertThat(duplicateName.id()).isNotEqualTo(updated.id());
        assertThat(duplicateName.name()).isEqualTo(updated.name());
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back Brand and its Vault Entry")
    void rollbackOfBrandRollsBackVaultEntry() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            brandOperations.create(new CreateBrandCommand(
                    "Doomed Brand", null, null, null, null, null, null, null
            ));
            throw new RuntimeException("Force brand rollback");
        })).hasMessageContaining("Force brand rollback");

        Integer brandCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM brands WHERE name = 'Doomed Brand'",
                Integer.class
        );
        assertThat(brandCount).isZero();

        Integer vaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'BRAND'",
                Integer.class
        );
        assertThat(vaultCount).isZero();
    }

    @Test
    @DisplayName("Enforces Brand Reference catalog and Schema v1 price constraints")
    void brandReferenceAndPriceValidation() {
        // Invalid nationality
        assertThatThrownBy(() -> brandOperations.create(new CreateBrandCommand(
                "Brand", null, "ZZ", null, null, null, null, null
        ))).isInstanceOf(InvalidBrandException.class);

        // Invalid currency
        assertThatThrownBy(() -> brandOperations.create(new CreateBrandCommand(
                "Brand", null, null, null, new BigDecimal("10"), new BigDecimal("20"), "XXX", null
        ))).isInstanceOf(InvalidBrandException.class);

        // minPrice > maxPrice
        assertThatThrownBy(() -> brandOperations.create(new CreateBrandCommand(
                "Brand", null, null, null, new BigDecimal("50"), new BigDecimal("20"), "USD", null
        ))).isInstanceOf(InvalidBrandException.class);

        // Negative minPrice
        assertThatThrownBy(() -> brandOperations.create(new CreateBrandCommand(
                "Brand", null, null, null, new BigDecimal("-1"), new BigDecimal("20"), "USD", null
        ))).isInstanceOf(InvalidBrandException.class);

        // Negative maxPrice
        assertThatThrownBy(() -> brandOperations.create(new CreateBrandCommand(
                "Brand", null, null, null, null, new BigDecimal("-5"), "USD", null
        ))).isInstanceOf(InvalidBrandException.class);

        // Price without currency
        assertThatThrownBy(() -> brandOperations.create(new CreateBrandCommand(
                "Brand", null, null, null, new BigDecimal("10"), null, null, null
        ))).isInstanceOf(InvalidBrandException.class);

        // Currency-only allowed (no prices)
        BrandView currencyOnly = brandOperations.create(new CreateBrandCommand(
                "Currency Only Brand", null, null, null, null, null, "USD", null
        ));
        assertThat(currencyOnly.currencyCode()).isEqualTo("USD");
        assertThat(currencyOnly.minPrice()).isNull();
        assertThat(currencyOnly.maxPrice()).isNull();
    }

    // --- Address Tests ---

    @Test
    @DisplayName("Creates, updates, and validates Address against Reference Country")
    void addressLifecycleAndValidation() {
        AddressView address = addressOperations.create(new CreateAddressCommand(
                "HQ", "commercial", "VN", "SG", "District 1", "Ben Nghe",
                "123 Le Loi", "70000"
        ));
        assertThat(address.id()).isNotNull();
        assertThat(address.countryCode()).isEqualTo("VN");
        assertThat(address.streetAddress()).isEqualTo("123 Le Loi");

        AddressView updated = addressOperations.update(new UpdateAddressCommand(
                address.id(), "HQ New", "office", "US", "CA", "San Francisco",
                null, "456 Market St", "94105"
        ));
        assertThat(updated.countryCode()).isEqualTo("US");
        assertThat(updated.locality()).isEqualTo("San Francisco");

        // Invalid country code throws
        assertThatThrownBy(() -> addressOperations.create(new CreateAddressCommand(
                "Invalid", null, "ZZ", null, null, null, null, null
        ))).isInstanceOf(InvalidAddressException.class);
    }

    // --- Location Tests ---

    @Test
    @DisplayName("Creates and updates Location backed by Vault Entry; allows duplicate names")
    void createAndUpdateLocation() {
        AddressView address = addressOperations.create(new CreateAddressCommand(
                "Main", null, "VN", null, null, null, "10 Nguyen Hue", null
        ));
        BrandView brand = brandOperations.create(new CreateBrandCommand(
                "Cafe Chain", null, null, null, null, null, null, null
        ));

        LocationView location = locationOperations.create(new CreateLocationCommand(
                brand.id(), address.id(), "Cafe Central", "https://cdn.example.com/loc.jpg",
                "Flagship store", "+84123456789", "https://cafe.example.com",
                new BigDecimal("20000"), new BigDecimal("80000"), "VND", "Cozy vibe"
        ));

        assertThat(location.id()).isNotNull();
        assertThat(location.name()).isEqualTo("Cafe Central");
        assertThat(location.brandId()).isEqualTo(brand.id());
        assertThat(location.addressId()).isEqualTo(address.id());

        // Verify Vault Entry exists with type LOCATION
        String vaultType = jdbcTemplate.queryForObject(
                "SELECT entry_type FROM vault_entries WHERE id = ?",
                String.class,
                location.id()
        );
        assertThat(vaultType).isEqualTo("LOCATION");

        // Update Location
        LocationView updated = locationOperations.update(new UpdateLocationCommand(
                location.id(), brand.id(), address.id(), "Cafe Central Premier",
                "https://cdn.example.com/loc2.jpg", "Renovated store",
                "+84987654321", "https://cafe.example.com/flagship",
                new BigDecimal("30000"), new BigDecimal("100000"), "VND", "Excellent atmosphere"
        ));
        assertThat(updated.name()).isEqualTo("Cafe Central Premier");
        assertThat(updated.review()).isEqualTo("Excellent atmosphere");

        // Location names are non-unique: another location with identical name succeeds
        LocationView duplicate = locationOperations.create(new CreateLocationCommand(
                null, address.id(), "Cafe Central Premier", null, null, null, null, null, null, null, null
        ));
        assertThat(duplicate.id()).isNotEqualTo(updated.id());
        assertThat(duplicate.name()).isEqualTo(updated.name());
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back Location and its Vault Entry")
    void rollbackOfLocationRollsBackVaultEntry() {
        AddressView address = addressOperations.create(new CreateAddressCommand(
                "Temp", null, "US", null, null, null, "100 Broadway", null
        ));

        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            locationOperations.create(new CreateLocationCommand(
                    null, address.id(), "Doomed Location", null, null, null, null, null, null, null, null
            ));
            throw new RuntimeException("Force location rollback");
        })).hasMessageContaining("Force location rollback");

        Integer locCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM locations WHERE name = 'Doomed Location'",
                Integer.class
        );
        assertThat(locCount).isZero();

        Integer vaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'LOCATION'",
                Integer.class
        );
        assertThat(vaultCount).isZero();
    }

    @Test
    @DisplayName("Enforces Location Reference catalog, Address/Brand existence, and price constraints")
    void locationValidationRules() {
        AddressView address = addressOperations.create(new CreateAddressCommand(
                "Valid Addr", null, "US", null, null, null, "123 Main St", null
        ));

        // Invalid address ID
        assertThatThrownBy(() -> locationOperations.create(new CreateLocationCommand(
                null, -999L, "Loc", null, null, null, null, null, null, null, null
        ))).isInstanceOf(InvalidLocationException.class);

        // Invalid brand ID
        assertThatThrownBy(() -> locationOperations.create(new CreateLocationCommand(
                -999L, address.id(), "Loc", null, null, null, null, null, null, null, null
        ))).isInstanceOf(InvalidLocationException.class);

        // Invalid currency code
        assertThatThrownBy(() -> locationOperations.create(new CreateLocationCommand(
                null, address.id(), "Loc", null, null, null, null,
                new BigDecimal("10"), new BigDecimal("20"), "ZZZ", null
        ))).isInstanceOf(InvalidLocationException.class);

        // minPrice > maxPrice
        assertThatThrownBy(() -> locationOperations.create(new CreateLocationCommand(
                null, address.id(), "Loc", null, null, null, null,
                new BigDecimal("100"), new BigDecimal("20"), "USD", null
        ))).isInstanceOf(InvalidLocationException.class);

        // Negative price
        assertThatThrownBy(() -> locationOperations.create(new CreateLocationCommand(
                null, address.id(), "Loc", null, null, null, null,
                new BigDecimal("-5"), new BigDecimal("20"), "USD", null
        ))).isInstanceOf(InvalidLocationException.class);

        // Price without currency
        assertThatThrownBy(() -> locationOperations.create(new CreateLocationCommand(
                null, address.id(), "Loc", null, null, null, null,
                new BigDecimal("10"), null, null, null
        ))).isInstanceOf(InvalidLocationException.class);

        // Currency-only allowed
        LocationView currencyOnly = locationOperations.create(new CreateLocationCommand(
                null, address.id(), "Currency Only Loc", null, null, null, null,
                null, null, "USD", null
        ));
        assertThat(currencyOnly.currencyCode()).isEqualTo("USD");
        assertThat(currencyOnly.minPrice()).isNull();
    }

    // --- Category Tests & Race Contention ---

    @Test
    @DisplayName("Creates, updates Location Category; rejects case-insensitive duplicate names")
    void locationCategoryLifecycleAndCaseInsensitiveUniqueness() {
        LocationCategoryView cat = categoryOperations.create(new CreateLocationCategoryCommand(
                "Coffee Shop", "Places for specialty coffee"
        ));
        assertThat(cat.id()).isNotNull();
        assertThat(cat.name()).isEqualTo("Coffee Shop");

        LocationCategoryView updated = categoryOperations.update(new UpdateLocationCategoryCommand(
                cat.id(), "Artisan Coffee", "Specialty beans and brews"
        ));
        assertThat(updated.name()).isEqualTo("Artisan Coffee");

        // Duplicate case-insensitive name throws LocationCategoryNameAlreadyExistsException
        assertThatThrownBy(() -> categoryOperations.create(new CreateLocationCategoryCommand(
                "artisan coffee", "Duplicate description"
        ))).isInstanceOf(LocationCategoryNameAlreadyExistsException.class);

        assertThatThrownBy(() -> categoryOperations.create(new CreateLocationCategoryCommand(
                "ARTISAN COFFEE", "Duplicate description"
        ))).isInstanceOf(LocationCategoryNameAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Concurrent duplicate LocationCategory creation recovers from PostgreSQL unique constraint race")
    void concurrentDuplicateLocationCategoryThrowsConflict() throws Exception {
        String categoryName = "Bistro";
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: In new transaction, inserts category and holds uncommitted lock in PostgreSQL
            Future<Long> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                Long catId = jdbcTemplate.queryForObject(
                        "INSERT INTO location_categories (name, description) VALUES (?, ?) RETURNING id",
                        Long.class,
                        categoryName, "French style bistro"
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return catId;
            }));

            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Attempts to create category with same name (lowercase) via service.
            // Bypasses initial precheck because Thread 1 is uncommitted, then blocks on PostgreSQL unique index.
            Future<LocationCategoryView> thread2Future = executor.submit(() -> categoryOperations.create(
                    new CreateLocationCategoryCommand(categoryName.toLowerCase(), "Competing bistro")
            ));

            // Observe PostgreSQL lock contention on location_categories table
            awaitCompetingLock("location_categories", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(id1).isNotNull();

            // Thread 2 must receive LocationCategoryNameAlreadyExistsException, not raw DataIntegrityViolationException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(LocationCategoryNameAlreadyExistsException.class)
                    .hasMessageContaining(categoryName.toLowerCase());

            // Verify only one category exists in PostgreSQL
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM location_categories WHERE lower(name) = lower(?)",
                    Integer.class,
                    categoryName
            );
            assertThat(count).isEqualTo(1);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Concurrent update to duplicate category name recovers from PostgreSQL unique constraint race")
    void concurrentUpdateDuplicateLocationCategoryThrowsConflict() throws Exception {
        LocationCategoryView catA = categoryOperations.create(new CreateLocationCategoryCommand("Cafe", "Cafe desc"));
        LocationCategoryView catB = categoryOperations.create(new CreateLocationCategoryCommand("Diner", "Diner desc"));

        String targetName = "Bistro";
        CountDownLatch thread1Updated = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: In new transaction, updates catA to "Bistro" and holds uncommitted lock in PostgreSQL
            Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                jdbcTemplate.update(
                        "UPDATE location_categories SET name = ? WHERE id = ?",
                        targetName, catA.id()
                );
                thread1Updated.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return null;
            }));

            assertThat(thread1Updated.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Attempts to update catB to "bistro" (same case-insensitive name) via service.
            // Bypasses initial precheck because Thread 1 is uncommitted, then blocks on PostgreSQL unique index during saveAndFlush.
            Future<LocationCategoryView> thread2Future = executor.submit(() -> categoryOperations.update(
                    new UpdateLocationCategoryCommand(catB.id(), targetName.toLowerCase(), "Updated diner")
            ));

            // Observe PostgreSQL lock contention on location_categories table
            awaitCompetingLock("location_categories", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);

            // Thread 2 must receive LocationCategoryNameAlreadyExistsException, not raw DataIntegrityViolationException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(LocationCategoryNameAlreadyExistsException.class)
                    .hasMessageContaining(targetName.toLowerCase());

            // Verify catB still has its original name "Diner" and only catA has "Bistro"
            LocationCategoryView reloadedB = categoryOperations.findById(catB.id());
            assertThat(reloadedB.name()).isEqualTo("Diner");

            LocationCategoryView reloadedA = categoryOperations.findById(catA.id());
            assertThat(reloadedA.name()).isEqualTo(targetName);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    // --- Category Assignment & Dining Style Idempotency with Contention ---

    @Test
    @DisplayName("Sequential and concurrent LocationCategory assignments are idempotent sets")
    void locationCategoryAssignmentIdempotencyAndDeterministicContention() throws Exception {
        AddressView address = addressOperations.create(new CreateAddressCommand("Addr", null, "US", null, null, null, "1 St", null));
        LocationView location = locationOperations.create(new CreateLocationCommand(null, address.id(), "Loc", null, null, null, null, null, null, null, null));
        LocationCategoryView cat1 = categoryOperations.create(new CreateLocationCategoryCommand("Category 1", null));
        LocationCategoryView cat2 = categoryOperations.create(new CreateLocationCategoryCommand("Category 2", null));

        // Sequential duplicate assignment
        categoryOperations.assignCategoryToLocation(location.id(), cat1.id());
        categoryOperations.assignCategoryToLocation(location.id(), cat1.id());

        List<LocationCategoryView> assigned = categoryOperations.findCategoriesByLocationId(location.id());
        assertThat(assigned).extracting(LocationCategoryView::id).containsExactly(cat1.id());

        // Concurrent duplicate assignment for cat2
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                jdbcTemplate.update(
                        "INSERT INTO location_category_assignments (location_id, category_id) VALUES (?, ?)",
                        location.id(),
                        cat2.id()
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return null;
            }));

            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Void> thread2Future = executor.submit(() -> {
                categoryOperations.assignCategoryToLocation(location.id(), cat2.id());
                return null;
            });

            // Observe PostgreSQL lock contention on location_category_assignments
            awaitCompetingLock("location_category_assignments", Duration.ofSeconds(5));

            thread2ReadyToCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);
            thread2Future.get(10, TimeUnit.SECONDS);

            Integer rowCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM location_category_assignments WHERE location_id = ? AND category_id = ?",
                    Integer.class,
                    location.id(),
                    cat2.id()
            );
            assertThat(rowCount).isEqualTo(1);

            List<LocationCategoryView> allAssigned = categoryOperations.findCategoriesByLocationId(location.id());
            assertThat(allAssigned).extracting(LocationCategoryView::id).containsExactlyInAnyOrder(cat1.id(), cat2.id());
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Sequential and concurrent dining service style assignments are idempotent sets")
    void diningServiceStyleAssignmentIdempotencyAndDeterministicContention() throws Exception {
        AddressView address = addressOperations.create(new CreateAddressCommand("Addr", null, "US", null, null, null, "1 St", null));
        LocationView location = locationOperations.create(new CreateLocationCommand(null, address.id(), "Diner", null, null, null, null, null, null, null, null));

        // Sequential duplicate assignment
        locationOperations.assignDiningServiceStyle(location.id(), DiningServiceStyle.A_LA_CARTE);
        locationOperations.assignDiningServiceStyle(location.id(), DiningServiceStyle.A_LA_CARTE);

        Set<DiningServiceStyle> styles = locationOperations.findDiningServiceStylesByLocationId(location.id());
        assertThat(styles).containsExactly(DiningServiceStyle.A_LA_CARTE);

        // Concurrent duplicate assignment for BUFFET
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                jdbcTemplate.update(
                        "INSERT INTO location_dining_service_styles (location_id, service_style) VALUES (?, ?::dining_service_style)",
                        location.id(),
                        DiningServiceStyle.BUFFET.name()
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return null;
            }));

            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            Future<Void> thread2Future = executor.submit(() -> {
                locationOperations.assignDiningServiceStyle(location.id(), DiningServiceStyle.BUFFET);
                return null;
            });

            // Observe PostgreSQL lock contention on location_dining_service_styles
            awaitCompetingLock("location_dining_service_styles", Duration.ofSeconds(5));

            thread2ReadyToCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);
            thread2Future.get(10, TimeUnit.SECONDS);

            Integer rowCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM location_dining_service_styles WHERE location_id = ? AND service_style = ?::dining_service_style",
                    Integer.class,
                    location.id(),
                    DiningServiceStyle.BUFFET.name()
            );
            assertThat(rowCount).isEqualTo(1);

            Set<DiningServiceStyle> allStyles = locationOperations.findDiningServiceStylesByLocationId(location.id());
            assertThat(allStyles).containsExactlyInAnyOrder(DiningServiceStyle.A_LA_CARTE, DiningServiceStyle.BUFFET);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    // --- Business Hours Tests & Competing Replacement Serialization ---

    @Test
    @DisplayName("Covers unknown, closed, split, and overnight business hours schedule semantics")
    void businessHoursSemantics() {
        AddressView address = addressOperations.create(new CreateAddressCommand("Addr", null, "US", null, null, null, "1 St", null));
        LocationView location = locationOperations.create(new CreateLocationCommand(null, address.id(), "Bistro Loc", null, null, null, null, null, null, null, null));

        // Initial schedule: unknown
        BusinessHoursScheduleView initial = hoursOperations.getSchedule(location.id());
        assertThat(initial.businessHoursKnown()).isFalse();
        assertThat(initial.intervals()).isEmpty();

        // Unknown schedule with non-empty intervals throws InvalidBusinessHoursException
        assertThatThrownBy(() -> hoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                location.id(), false, List.of(new BusinessHoursIntervalInput(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)))
        ))).isInstanceOf(InvalidBusinessHoursException.class);

        // Replace with known schedule:
        // - MONDAY: split intervals (08:00 - 12:00, 13:00 - 17:00)
        // - TUESDAY: closed (no intervals)
        // - SATURDAY: overnight interval (21:00 - 02:00)
        BusinessHoursScheduleView schedule = hoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                location.id(),
                true,
                List.of(
                        new BusinessHoursIntervalInput(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                        new BusinessHoursIntervalInput(DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(17, 0)),
                        new BusinessHoursIntervalInput(DayOfWeek.SATURDAY, LocalTime.of(21, 0), LocalTime.of(2, 0))
                )
        ));

        assertThat(schedule.businessHoursKnown()).isTrue();
        assertThat(schedule.intervals()).hasSize(3);

        BusinessHoursIntervalView mon1 = schedule.intervals().get(0);
        assertThat(mon1.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(mon1.sequence()).isEqualTo(1);
        assertThat(mon1.openTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(mon1.closeTime()).isEqualTo(LocalTime.of(12, 0));

        BusinessHoursIntervalView mon2 = schedule.intervals().get(1);
        assertThat(mon2.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(mon2.sequence()).isEqualTo(2);
        assertThat(mon2.openTime()).isEqualTo(LocalTime.of(13, 0));
        assertThat(mon2.closeTime()).isEqualTo(LocalTime.of(17, 0));

        BusinessHoursIntervalView sat = schedule.intervals().get(2);
        assertThat(sat.dayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(sat.sequence()).isEqualTo(1);
        assertThat(sat.openTime()).isEqualTo(LocalTime.of(21, 0));
        assertThat(sat.closeTime()).isEqualTo(LocalTime.of(2, 0)); // overnight interval

        // Verify rows in database
        Integer rowCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM location_business_hours WHERE location_id = ?",
                Integer.class,
                location.id()
        );
        assertThat(rowCount).isEqualTo(3);

        // Transition back to unknown: clears all interval rows
        BusinessHoursScheduleView cleared = hoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                location.id(), false, List.of()
        ));
        assertThat(cleared.businessHoursKnown()).isFalse();
        assertThat(cleared.intervals()).isEmpty();

        Integer clearedCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM location_business_hours WHERE location_id = ?",
                Integer.class,
                location.id()
        );
        assertThat(clearedCount).isZero();
    }

    @Test
    @DisplayName("Serializes competing schedule replacements per Location via pessimistic lock without interleave")
    void concurrentCompetingScheduleReplacementSerializedPerLocation() throws Exception {
        AddressView address = addressOperations.create(new CreateAddressCommand("Addr", null, "US", null, null, null, "1 St", null));
        LocationView location = locationOperations.create(new CreateLocationCommand(null, address.id(), "Contended Loc", null, null, null, null, null, null, null, null));

        CountDownLatch thread1Locked = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: In new transaction, holds exclusive pessimistic row lock on locations
            Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                jdbcTemplate.queryForObject(
                        "SELECT id FROM locations WHERE id = ? FOR UPDATE",
                        Long.class,
                        location.id()
                );
                thread1Locked.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                // Insert Schedule A intervals
                jdbcTemplate.update(
                        "INSERT INTO location_business_hours (location_id, day_of_week, sequence, open_time, close_time) " +
                        "VALUES (?, 'MONDAY'::day_of_week, 1, '09:00:00'::time, '17:00:00'::time)",
                        location.id()
                );
                return null;
            }));

            assertThat(thread1Locked.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Calls replaceSchedule with Schedule B (Friday 18:00 - 23:00).
            // It will attempt to acquire pessimistic lock via findByIdForUpdate, blocking on Thread 1.
            Future<BusinessHoursScheduleView> thread2Future = executor.submit(() -> hoursOperations.replaceSchedule(
                    new ReplaceBusinessHoursScheduleCommand(
                            location.id(),
                            true,
                            List.of(new BusinessHoursIntervalInput(DayOfWeek.FRIDAY, LocalTime.of(18, 0), LocalTime.of(23, 0)))
                    )
            ));

            // Observe PostgreSQL lock contention on locations table
            awaitCompetingLock("locations", Duration.ofSeconds(5));

            // Release Thread 1 to finish and commit
            thread2ReadyToCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);
            BusinessHoursScheduleView scheduleB = thread2Future.get(10, TimeUnit.SECONDS);

            // Final schedule must be cleanly Schedule B (Friday only), with Schedule A's Monday completely deleted
            assertThat(scheduleB.businessHoursKnown()).isTrue();
            assertThat(scheduleB.intervals()).hasSize(1);
            assertThat(scheduleB.intervals().get(0).dayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);

            BusinessHoursScheduleView reloaded = hoursOperations.getSchedule(location.id());
            assertThat(reloaded.intervals()).hasSize(1);
            assertThat(reloaded.intervals().get(0).dayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);

            Integer mondayRows = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM location_business_hours WHERE location_id = ? AND day_of_week = 'MONDAY'::day_of_week",
                    Integer.class,
                    location.id()
            );
            assertThat(mondayRows).isZero();
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Concurrent schedule read coordinates with writer lock and sees coherent replacement")
    void concurrentScheduleReadCoordinatesWithReplacementLock() throws Exception {
        AddressView address = addressOperations.create(new CreateAddressCommand("Addr", null, "US", null, null, null, "1 St", null));
        LocationView location = locationOperations.create(new CreateLocationCommand(null, address.id(), "Coherent Loc", null, null, null, null, null, null, null, null));

        CountDownLatch thread1Writing = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: In new transaction, starts replacing schedule by acquiring FOR UPDATE on locations
            Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                jdbcTemplate.queryForObject(
                        "SELECT id FROM locations WHERE id = ? FOR UPDATE",
                        Long.class,
                        location.id()
                );
                // Update to known=true and insert Wednesday 10:00 - 18:00
                jdbcTemplate.update("UPDATE locations SET business_hours_known = true WHERE id = ?", location.id());
                jdbcTemplate.update(
                        "INSERT INTO location_business_hours (location_id, day_of_week, sequence, open_time, close_time) " +
                        "VALUES (?, 'WEDNESDAY'::day_of_week, 1, '10:00:00'::time, '18:00:00'::time)",
                        location.id()
                );
                thread1Writing.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return null;
            }));

            assertThat(thread1Writing.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Calls getSchedule via service. It executes findByIdForShare and blocks on Thread 1's FOR UPDATE lock.
            Future<BusinessHoursScheduleView> thread2Future = executor.submit(() -> hoursOperations.getSchedule(location.id()));

            // Observe PostgreSQL lock contention on locations table
            awaitCompetingLock("locations", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);
            BusinessHoursScheduleView readSchedule = thread2Future.get(10, TimeUnit.SECONDS);

            // Thread 2 must see the fully committed coherent schedule: known=true with the 1 Wednesday interval
            assertThat(readSchedule.businessHoursKnown()).isTrue();
            assertThat(readSchedule.intervals()).hasSize(1);
            assertThat(readSchedule.intervals().get(0).dayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Concurrent schedule replacement waits for schedule reader shared lock")
    void concurrentReplacementWaitsForScheduleReaderLock() throws Exception {
        AddressView address = addressOperations.create(new CreateAddressCommand("Addr", null, "US", null, null, null, "1 St", null));
        LocationView location = locationOperations.create(new CreateLocationCommand(null, address.id(), "Reader Loc", null, null, null, null, null, null, null, null));

        // Initial schedule: Thursday 09:00 - 17:00
        hoursOperations.replaceSchedule(new ReplaceBusinessHoursScheduleCommand(
                location.id(),
                true,
                List.of(new BusinessHoursIntervalInput(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)))
        ));

        CountDownLatch thread1Reading = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: In new transaction, acquires FOR SHARE on locations and holds the lock
            Future<Void> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                jdbcTemplate.queryForObject(
                        "SELECT id FROM locations WHERE id = ? FOR SHARE",
                        Long.class,
                        location.id()
                );
                thread1Reading.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return null;
            }));

            assertThat(thread1Reading.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: Calls replaceSchedule with Sunday hours. It calls findByIdForUpdate and blocks on Thread 1's FOR SHARE.
            Future<BusinessHoursScheduleView> thread2Future = executor.submit(() -> hoursOperations.replaceSchedule(
                    new ReplaceBusinessHoursScheduleCommand(
                            location.id(),
                            true,
                            List.of(new BusinessHoursIntervalInput(DayOfWeek.SUNDAY, LocalTime.of(11, 0), LocalTime.of(16, 0)))
                    )
            ));

            // Observe PostgreSQL lock contention on locations table
            awaitCompetingLock("locations", Duration.ofSeconds(5));

            // Release Thread 1 to finish reading and commit
            thread2ReadyToCommit.countDown();

            thread1Future.get(10, TimeUnit.SECONDS);
            BusinessHoursScheduleView finalSchedule = thread2Future.get(10, TimeUnit.SECONDS);

            // Final schedule is Sunday
            assertThat(finalSchedule.businessHoursKnown()).isTrue();
            assertThat(finalSchedule.intervals()).hasSize(1);
            assertThat(finalSchedule.intervals().get(0).dayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void awaitCompetingLock(String tablePattern, Duration timeout) throws Exception {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM pg_locks l " +
                    "JOIN pg_stat_activity a ON l.pid = a.pid " +
                    "WHERE NOT l.granted " +
                    "  AND a.pid != pg_backend_pid() " +
                    "  AND a.query ILIKE ?",
                    Integer.class,
                    "%" + tablePattern + "%"
            );
            if (count != null && count > 0) {
                return;
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Timed out waiting for competing transaction to reach PostgreSQL lock on " + tablePattern);
    }
}
