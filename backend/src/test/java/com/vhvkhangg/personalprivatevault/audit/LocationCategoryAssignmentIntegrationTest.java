package com.vhvkhangg.personalprivatevault.audit;

import com.vhvkhangg.personalprivatevault.location.address.AddressOperations;
import com.vhvkhangg.personalprivatevault.location.address.CreateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.category.CreateLocationCategoryCommand;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryNotFoundException;
import com.vhvkhangg.personalprivatevault.location.category.LocationCategoryOperations;
import com.vhvkhangg.personalprivatevault.location.location.CreateLocationCommand;
import com.vhvkhangg.personalprivatevault.location.location.LocationNotFoundException;
import com.vhvkhangg.personalprivatevault.location.location.LocationOperations;
import com.vhvkhangg.personalprivatevault.location.view.LocationCategoryView;
import com.vhvkhangg.personalprivatevault.location.view.LocationView;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BA15-11 regression test:
 * Verifies that location category assignment validates references cleanly and maps FK/referential races
 * to specific domain exceptions without exposing raw SQL FK errors, and verifies concurrent assignment safety.
 */
class LocationCategoryAssignmentIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private LocationOperations locationOperations;

    @Autowired
    private LocationCategoryOperations categoryOperations;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;

    @Autowired
    private AddressOperations addressOperations;

    @Test
    @DisplayName("BA15-11: Assigning nonexistent category to existing location throws LocationCategoryNotFoundException")
    void assigningNonExistentCategoryThrowsNotFound() {
        var addr = addressOperations.create(new CreateAddressCommand("Addr 1", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null, addr.id(), "Test Branch", null, null, null, null, null, null, null, null
        ));

        assertThatThrownBy(() -> categoryOperations.assignCategoryToLocation(location.id(), 999999L))
                .isInstanceOf(LocationCategoryNotFoundException.class);
    }

    @Test
    @DisplayName("BA15-11: Assigning category to nonexistent location throws LocationNotFoundException")
    void assigningToNonExistentLocationThrowsNotFound() {
        LocationCategoryView category = categoryOperations.create(
                new CreateLocationCategoryCommand("Cafe", null)
        );

        assertThatThrownBy(() -> categoryOperations.assignCategoryToLocation(999999L, category.id()))
                .isInstanceOf(LocationNotFoundException.class);
    }

    @Test
    @DisplayName("BA15-11: Valid category assignment succeeds and duplicate assignment is idempotent")
    void validAndDuplicateAssignmentSucceeds() {
        var addr = addressOperations.create(new CreateAddressCommand("Addr 2", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null, addr.id(), "Coffee Shop", null, null, null, null, null, null, null, null
        ));
        LocationCategoryView category = categoryOperations.create(
                new CreateLocationCategoryCommand("Coffee", null)
        );

        categoryOperations.assignCategoryToLocation(location.id(), category.id());

        List<LocationCategoryView> categories = categoryOperations.findCategoriesByLocationId(location.id());
        assertThat(categories).extracting(LocationCategoryView::id).containsExactly(category.id());

        // Duplicate assignment is conflict-safe and idempotent
        categoryOperations.assignCategoryToLocation(location.id(), category.id());
        List<LocationCategoryView> categoriesAfter = categoryOperations.findCategoriesByLocationId(location.id());
        assertThat(categoriesAfter).extracting(LocationCategoryView::id).containsExactly(category.id());
    }

    @Test
    @DisplayName("BA15-11: Concurrent assignment of same category to location from multiple threads is race-safe")
    void concurrentCategoryAssignmentIsRaceSafe() throws Exception {
        var addr = addressOperations.create(new CreateAddressCommand("Addr Concurrency", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null, addr.id(), "Multi-thread Branch", null, null, null, null, null, null, null, null
        ));
        LocationCategoryView category = categoryOperations.create(
                new CreateLocationCategoryCommand("Bistro", null)
        );

        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> f1 = executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS);
                categoryOperations.assignCategoryToLocation(location.id(), category.id());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Future<?> f2 = executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS);
                categoryOperations.assignCategoryToLocation(location.id(), category.id());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        f1.get(10, TimeUnit.SECONDS);
        f2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        List<LocationCategoryView> assigned = categoryOperations.findCategoriesByLocationId(location.id());
        assertThat(assigned).extracting(LocationCategoryView::id).containsExactly(category.id());
    }

    @Autowired
    private com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationCategoryRepository categoryRepository;

    @Autowired
    private com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationRepository locationRepository;

    @Autowired
    private com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationCategoryAssignmentRepository assignmentRepository;

    @Test
    @DisplayName("BA15-11: Foreign key race translates cleanly to domain exception without raw SQL leak")
    void foreignKeyRaceTranslatesToDomainException() throws Exception {
        var addr = addressOperations.create(new CreateAddressCommand("Addr FK", "office", "VN", "SG", "D1", "BN", "123 Le Loi", "70000"));
        LocationView location = locationOperations.create(new CreateLocationCommand(
                null, addr.id(), "FK Branch", null, null, null, null, null, null, null, null
        ));
        LocationCategoryView category = categoryOperations.create(
                new CreateLocationCategoryCommand("Bakery", null)
        );

        // Proxy categoryRepository: passes existsById check, but deletes category from DB right after
        // reproducing the exact race window between existence check and INSERT
        var proxyRepo = (com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationCategoryRepository)
                java.lang.reflect.Proxy.newProxyInstance(
                        categoryRepository.getClass().getClassLoader(),
                        new Class<?>[]{com.vhvkhangg.personalprivatevault.location.internal.infrastructure.persistence.LocationCategoryRepository.class},
                        (p, method, args) -> {
                            if ("existsById".equals(method.getName()) && args != null && args.length == 1 && category.id().equals(args[0])) {
                                jdbcTemplate.update("DELETE FROM location_categories WHERE id = ?", category.id());
                                return true;
                            }
                            return method.invoke(categoryRepository, args);
                        }
                );

        var service = new com.vhvkhangg.personalprivatevault.location.internal.application.LocationCategoryService(
                proxyRepo, assignmentRepository, locationRepository
        );

        var txTemplate = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // When assignment runs, existsById returns true, but insertIfAbsent hits real DB FK constraint violation
        assertThatThrownBy(() -> txTemplate.execute(status -> {
            service.assignCategoryToLocation(location.id(), category.id());
            return null;
        }))
                .isInstanceOf(LocationCategoryNotFoundException.class)
                .hasMessage("Location category not found with id: " + category.id())
                .hasMessageNotContaining("SQL")
                .hasMessageNotContaining("constraint")
                .hasMessageNotContaining("PostgreSQL");

        // Verify zero assignments in DB
        Integer assignmentCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM location_category_assignments WHERE location_id = ?",
                Integer.class,
                location.id()
        );
        assertThat(assignmentCount).isZero();

        // Also verify HTTP layer returns canonical 404 without SQL details
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/locations/{id}/categories/{categoryId}", location.id(), 999999L)
                        .header(org.springframework.http.HttpHeaders.AUTHORIZATION, bearerHeader()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.error.code").value("LOCATION_CATEGORY_NOT_FOUND"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.error.message").value("Location category not found"));
    }
}
