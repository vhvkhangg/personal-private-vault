package com.vhvkhangg.personalprivatevault.personal;

import com.vhvkhangg.personalprivatevault.location.address.AddressOperations;
import com.vhvkhangg.personalprivatevault.location.address.CreateAddressCommand;
import com.vhvkhangg.personalprivatevault.location.view.AddressView;
import com.vhvkhangg.personalprivatevault.personal.enums.Gender;
import com.vhvkhangg.personalprivatevault.personal.profile.PersonalProfileOperations;
import com.vhvkhangg.personalprivatevault.personal.profile.command.CreatePersonalProfileCommand;
import com.vhvkhangg.personalprivatevault.personal.profile.command.UpdatePersonalProfileCommand;
import com.vhvkhangg.personalprivatevault.personal.profile.exception.InvalidPersonalProfileException;
import com.vhvkhangg.personalprivatevault.personal.profile.exception.PersonalProfileConflictException;
import com.vhvkhangg.personalprivatevault.personal.profile.exception.PersonalProfileNotFoundException;
import com.vhvkhangg.personalprivatevault.personal.view.PersonalProfileView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class PersonalProfileIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private PersonalProfileOperations personalProfileOperations;

    @Autowired
    private AddressOperations addressOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long testAddressId;

    @BeforeEach
    void setUp() {
        cleanUp();

        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('US', 'United States', 'Hoa Kỳ')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('VN', 'Vietnam', 'Việt Nam')
                ON CONFLICT (code) DO NOTHING
                """);

        AddressView address = addressOperations.create(new CreateAddressCommand(
                "Home", "RESIDENTIAL", "US", "CA", "San Francisco", null, "123 Market St", "94105"
        ));
        testAddressId = address.id();
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM personal_profiles");
        jdbcTemplate.execute("DELETE FROM addresses WHERE label = 'Home'");
    }

    @Test
    @DisplayName("Creates, updates, and reloads personal profile with exact markdown preservation and reference validation")
    void createUpdateAndReloadProfileWithExactMarkdown() {
        String exactNotes = "  ### Health & Preferences\n\n- Blood Type: O+\n- Allergies: Peanuts\n\nNotes with spaces   ";

        CreatePersonalProfileCommand createCmd = new CreatePersonalProfileCommand(
                "Alice Vault",
                "Self",
                true,
                Gender.FEMALE,
                LocalDate.of(1995, 5, 20),
                "US",
                "+1-555-0199",
                "alice@example.com",
                testAddressId,
                "Software Engineer",
                exactNotes
        );

        PersonalProfileView created = personalProfileOperations.createProfile(createCmd);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Alice Vault");
        assertThat(created.relationship()).isEqualTo("Self");
        assertThat(created.isSelf()).isTrue();
        assertThat(created.gender()).isEqualTo(Gender.FEMALE);
        assertThat(created.birthDate()).isEqualTo(LocalDate.of(1995, 5, 20));
        assertThat(created.nationalityCode()).isEqualTo("US");
        assertThat(created.phone()).isEqualTo("+1-555-0199");
        assertThat(created.email()).isEqualTo("alice@example.com");
        assertThat(created.addressId()).isEqualTo(testAddressId);
        assertThat(created.occupation()).isEqualTo("Software Engineer");
        assertThat(created.notesMarkdown()).isEqualTo(exactNotes);
        assertThat(created.createdAt()).isNotNull();
        assertThat(created.updatedAt()).isNotNull();
        assertThat(created.deletedAt()).isNull();

        // findSelfProfile
        Optional<PersonalProfileView> selfProfile = personalProfileOperations.findSelfProfile();
        assertThat(selfProfile).isPresent();
        assertThat(selfProfile.get().id()).isEqualTo(created.id());

        // Update profile
        String updatedNotes = "Updated notes\nwith newlines\n  and spaces  ";
        UpdatePersonalProfileCommand updateCmd = new UpdatePersonalProfileCommand(
                created.id(),
                "Alice Updated",
                "Primary",
                true,
                Gender.FEMALE,
                LocalDate.of(1995, 5, 20),
                "US",
                "+1-555-0200",
                "alice.updated@example.com",
                testAddressId,
                "Staff Engineer",
                updatedNotes
        );

        PersonalProfileView updated = personalProfileOperations.updateProfile(updateCmd);
        assertThat(updated.name()).isEqualTo("Alice Updated");
        assertThat(updated.relationship()).isEqualTo("Primary");
        assertThat(updated.notesMarkdown()).isEqualTo(updatedNotes);

        PersonalProfileView reloaded = personalProfileOperations.findProfileById(created.id());
        assertThat(reloaded.notesMarkdown()).isEqualTo(updatedNotes);
    }

    @Test
    @DisplayName("Allows multiple non-self duplicate profiles")
    void allowsMultipleNonSelfDuplicates() {
        PersonalProfileView p1 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Bob Friend", "Friend", false, Gender.MALE, null, null, null, null, null, null, null
        ));
        PersonalProfileView p2 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Bob Friend", "Friend", false, Gender.MALE, null, null, null, null, null, null, null
        ));

        assertThat(p1.id()).isNotEqualTo(p2.id());
        assertThat(p1.name()).isEqualTo(p2.name());

        List<PersonalProfileView> profiles = personalProfileOperations.findProfiles(10);
        assertThat(profiles).hasSize(2);
    }

    @Test
    @DisplayName("Sequential second-self creation throws PersonalProfileConflictException")
    void sequentialSecondSelfThrowsConflict() {
        personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Self 1", "Self", true, null, null, null, null, null, null, null, null
        ));

        assertThatThrownBy(() -> personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Self 2", "Myself", true, null, null, null, null, null, null, null, null
        )))
                .isInstanceOf(PersonalProfileConflictException.class)
                .hasMessageContaining("An active self profile already exists");
    }

    @Test
    @DisplayName("Sequential update to self throws PersonalProfileConflictException if another active self exists")
    void sequentialUpdateToSelfThrowsConflict() {
        personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Self 1", "Self", true, null, null, null, null, null, null, null, null
        ));
        PersonalProfileView p2 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Other 2", "Friend", false, null, null, null, null, null, null, null, null
        ));

        assertThatThrownBy(() -> personalProfileOperations.updateProfile(new UpdatePersonalProfileCommand(
                p2.id(), "Other 2", "Friend", true, null, null, null, null, null, null, null, null
        )))
                .isInstanceOf(PersonalProfileConflictException.class)
                .hasMessageContaining("An active self profile already exists");
    }

    @Test
    @DisplayName("Soft-deleting self frees the active-self slot, and restoring while another self exists throws conflict")
    void softDeleteSelfFreesSlotAndRestoreConflict() {
        PersonalProfileView self1 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Self 1", "Self", true, null, null, null, null, null, null, null, null
        ));

        // Soft delete self 1
        personalProfileOperations.softDeleteProfile(self1.id());
        assertThat(personalProfileOperations.findSelfProfile()).isEmpty();

        // Create new self profile succeeds because slot is free
        PersonalProfileView self2 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Self 2", "Self", true, null, null, null, null, null, null, null, null
        ));
        assertThat(personalProfileOperations.findSelfProfile()).isPresent();
        assertThat(personalProfileOperations.findSelfProfile().get().id()).isEqualTo(self2.id());

        // Restoring self 1 now conflicts because self 2 is active self
        assertThatThrownBy(() -> personalProfileOperations.restoreProfile(self1.id()))
                .isInstanceOf(PersonalProfileConflictException.class)
                .hasMessageContaining("An active self profile already exists");

        // Soft-deleting self 2 allows restoring self 1
        personalProfileOperations.softDeleteProfile(self2.id());
        personalProfileOperations.restoreProfile(self1.id());
        assertThat(personalProfileOperations.findSelfProfile().get().id()).isEqualTo(self1.id());
    }

    @Test
    @DisplayName("Changing self profile to non-self frees the active-self slot")
    void changingSelfToNonSelfFreesSlot() {
        PersonalProfileView self = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "My Profile", "Self", true, null, null, null, null, null, null, null, null
        ));

        // Change to non-self
        personalProfileOperations.updateProfile(new UpdatePersonalProfileCommand(
                self.id(), "My Profile", "Colleague", false, null, null, null, null, null, null, null, null
        ));

        assertThat(personalProfileOperations.findSelfProfile()).isEmpty();

        // Create new self profile succeeds
        PersonalProfileView newSelf = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Real Self", "Self", true, null, null, null, null, null, null, null, null
        ));
        assertThat(personalProfileOperations.findSelfProfile()).isPresent();
        assertThat(personalProfileOperations.findSelfProfile().get().id()).isEqualTo(newSelf.id());
    }

    @Test
    @DisplayName("Concurrent create-to-self conflict relies on PostgreSQL partial unique constraint with safe translation")
    void concurrentCreateSelfConflictReliesOnPostgresPartialIndex(CapturedOutput output) throws Exception {
        CountDownLatch thread1Inserted = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1 inserts self profile in uncommitted transaction
            Future<Long> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                Long id = jdbcTemplate.queryForObject(
                        "INSERT INTO personal_profiles (name, relationship, is_self) VALUES ('Thread1 Self', 'Self', true) RETURNING id",
                        Long.class
                );
                thread1Inserted.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return id;
            }));

            assertThat(thread1Inserted.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2 calls createProfile with is_self = true; application precheck passes because Thread 1 is uncommitted,
            // then Thread 2 blocks on uq_personal_profiles_one_active_self.
            Future<PersonalProfileView> thread2Future = executor.submit(() -> personalProfileOperations.createProfile(
                    new CreatePersonalProfileCommand("Thread2 Self", "Myself", true, null, null, null, null, null, null, null, null)
            ));

            // Await PostgreSQL lock contention
            awaitCompetingLock("personal_profiles", Duration.ofSeconds(5));

            // Let thread 1 commit
            thread2ReadyToCommit.countDown();

            Long id1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(id1).isNotNull();

            // Thread 2 must fail with PersonalProfileConflictException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(PersonalProfileConflictException.class)
                    .hasMessageContaining("An active self profile already exists");

            // Exactly 1 active self profile exists
            Integer selfCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM personal_profiles WHERE is_self = true AND deleted_at IS NULL",
                    Integer.class
            );
            assertThat(selfCount).isEqualTo(1);

            // Privacy verification: no raw database constraint details leaked in log
            assertThat(output.getAll())
                    .doesNotContain("Detail: Key (is_self)=(")
                    .doesNotContain("uq_personal_profiles_one_active_self");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-3 contention: Concurrent update-to-self conflict relies on PostgreSQL partial unique constraint with safe translation")
    void concurrentUpdateSelfConflictReliesOnPostgresPartialIndex(CapturedOutput output) throws Exception {
        PersonalProfileView p1 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Profile 1", "Friend", false, null, null, null, null, null, null, null, null
        ));
        PersonalProfileView p2 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Profile 2", "Colleague", false, null, null, null, null, null, null, null, null
        ));

        CountDownLatch thread1Updated = new CountDownLatch(1);
        CountDownLatch thread2ReadyToCommit = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        try {
            // Thread 1: updates p1 to is_self = true in uncommitted transaction
            Future<PersonalProfileView> thread1Future = executor.submit(() -> txTemplate.execute(status -> {
                PersonalProfileView updated = personalProfileOperations.updateProfile(new UpdatePersonalProfileCommand(
                        p1.id(), "Profile 1 Self", "Self", true, null, null, null, null, null, null, null, null
                ));
                thread1Updated.countDown();
                try {
                    boolean awaited = thread2ReadyToCommit.await(5, TimeUnit.SECONDS);
                    assertThat(awaited).isTrue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return updated;
            }));

            assertThat(thread1Updated.await(5, TimeUnit.SECONDS)).isTrue();

            // Thread 2: attempts to update p2 to is_self = true.
            // Application precheck passes because Thread 1 is uncommitted, then Thread 2 blocks on uq_personal_profiles_one_active_self.
            Future<PersonalProfileView> thread2Future = executor.submit(() -> personalProfileOperations.updateProfile(
                    new UpdatePersonalProfileCommand(
                            p2.id(), "Profile 2 Self", "Self", true, null, null, null, null, null, null, null, null
                    )
            ));

            // Observe lock contention
            awaitCompetingLock("personal_profiles", Duration.ofSeconds(5));

            // Release Thread 1 to commit
            thread2ReadyToCommit.countDown();

            PersonalProfileView res1 = thread1Future.get(10, TimeUnit.SECONDS);
            assertThat(res1.isSelf()).isTrue();

            // Thread 2 must unblock and fail with PersonalProfileConflictException
            assertThatThrownBy(() -> {
                try {
                    thread2Future.get(10, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
            })
                    .isInstanceOf(PersonalProfileConflictException.class)
                    .hasMessageContaining("An active self profile already exists");

            // Verify p2 is still not self
            PersonalProfileView reloadedP2 = personalProfileOperations.findProfileById(p2.id());
            assertThat(reloadedP2.isSelf()).isFalse();

            // Exactly 1 active self profile in DB
            Integer selfCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM personal_profiles WHERE is_self = true AND deleted_at IS NULL",
                    Integer.class
            );
            assertThat(selfCount).isEqualTo(1);

            // Privacy verification: no raw constraint details in log
            assertThat(output.getAll())
                    .doesNotContain("Detail: Key (is_self)=(")
                    .doesNotContain("uq_personal_profiles_one_active_self");
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("FR11-4: Nationality normalization (null, empty, whitespace normalized to null; valid country trimmed and persisted; unknown rejected)")
    void nationalityNormalizationAndValidation() {
        // Create with null nationality -> null
        PersonalProfileView p1 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "P1", "Friend", false, null, null, null, null, null, null, null, null
        ));
        assertThat(p1.nationalityCode()).isNull();

        // Create with empty nationality -> null
        PersonalProfileView p2 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "P2", "Friend", false, null, null, "", null, null, null, null, null
        ));
        assertThat(p2.nationalityCode()).isNull();

        // Create with whitespace nationality -> null
        PersonalProfileView p3 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "P3", "Friend", false, null, null, "   ", null, null, null, null, null
        ));
        assertThat(p3.nationalityCode()).isNull();

        // Create with padded valid nationality -> trimmed and persisted
        PersonalProfileView p4 = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "P4", "Friend", false, null, null, "  US  ", null, null, null, null, null
        ));
        assertThat(p4.nationalityCode()).isEqualTo("US");

        // Create with invalid country -> throws InvalidPersonalProfileException
        assertThatThrownBy(() -> personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "P5", "Friend", false, null, null, "ZZ", null, null, null, null, null
        )))
                .isInstanceOf(InvalidPersonalProfileException.class)
                .hasMessageContaining("Unknown nationality code: ZZ");

        // Update tests:
        // Update to null
        PersonalProfileView u1 = personalProfileOperations.updateProfile(new UpdatePersonalProfileCommand(
                p4.id(), "P4", "Friend", false, null, null, null, null, null, null, null, null
        ));
        assertThat(u1.nationalityCode()).isNull();

        // Update to whitespace -> null
        PersonalProfileView u2 = personalProfileOperations.updateProfile(new UpdatePersonalProfileCommand(
                p4.id(), "P4", "Friend", false, null, null, "   ", null, null, null, null, null
        ));
        assertThat(u2.nationalityCode()).isNull();

        // Update to padded VN -> trimmed to VN
        PersonalProfileView u3 = personalProfileOperations.updateProfile(new UpdatePersonalProfileCommand(
                p4.id(), "P4", "Friend", false, null, null, " VN ", null, null, null, null, null
        ));
        assertThat(u3.nationalityCode()).isEqualTo("VN");

        // Update to unknown -> throws InvalidPersonalProfileException and rolls back
        assertThatThrownBy(() -> personalProfileOperations.updateProfile(new UpdatePersonalProfileCommand(
                p4.id(), "P4", "Friend", false, null, null, "YY", null, null, null, null, null
        )))
                .isInstanceOf(InvalidPersonalProfileException.class)
                .hasMessageContaining("Unknown nationality code: YY");

        // Reload p4: nationalityCode must still be VN (rollback preservation)
        PersonalProfileView reloaded = personalProfileOperations.findProfileById(p4.id());
        assertThat(reloaded.nationalityCode()).isEqualTo("VN");
    }

    @Test
    @DisplayName("FR11-6: Non-uniqueness errors on self profile do not get misdiagnosed as active-self conflicts")
    void nonUniquenessErrorsOnSelfProfileNotMisdiagnosed() {
        // Command with isSelf = true, but invalid country code -> InvalidPersonalProfileException, NOT PersonalProfileConflictException
        assertThatThrownBy(() -> personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Self 1", "Self", true, null, null, "INVALID", null, null, null, null, null
        )))
                .isInstanceOf(InvalidPersonalProfileException.class)
                .hasMessageContaining("Unknown nationality code: INVALID")
                .isNotInstanceOf(PersonalProfileConflictException.class);

        // Command with isSelf = true, but invalid address id -> InvalidPersonalProfileException, NOT PersonalProfileConflictException
        assertThatThrownBy(() -> personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Self 2", "Self", true, null, null, null, null, null, 9999999L, null, null
        )))
                .isInstanceOf(InvalidPersonalProfileException.class)
                .hasMessageContaining("Address with id 9999999 does not exist")
                .isNotInstanceOf(PersonalProfileConflictException.class);

        // Create a self profile
        PersonalProfileView self = personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "My Self", "Self", true, null, null, null, null, null, null, null, null
        ));

        // Update on self profile with invalid country code -> InvalidPersonalProfileException, NOT PersonalProfileConflictException
        assertThatThrownBy(() -> personalProfileOperations.updateProfile(new UpdatePersonalProfileCommand(
                self.id(), "My Self", "Self", true, null, null, "INVALID", null, null, null, null, null
        )))
                .isInstanceOf(InvalidPersonalProfileException.class)
                .hasMessageContaining("Unknown nationality code: INVALID")
                .isNotInstanceOf(PersonalProfileConflictException.class);
    }

    @Test
    @DisplayName("Validates country through public Reference and address through public Location Address API")
    void validatesReferenceCountryAndLocationAddress() {
        // Non-existent country code
        assertThatThrownBy(() -> personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Name", "Friend", false, null, null, "ZZ", null, null, null, null, null
        )))
                .isInstanceOf(InvalidPersonalProfileException.class)
                .hasMessageContaining("Unknown nationality code: ZZ");

        // Non-existent address ID
        assertThatThrownBy(() -> personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Name", "Friend", false, null, null, null, null, null, 999999L, null, null
        )))
                .isInstanceOf(InvalidPersonalProfileException.class)
                .hasMessageContaining("Address with id 999999 does not exist");

        // Blank name
        assertThatThrownBy(() -> personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "   ", "Friend", false, null, null, null, null, null, null, null, null
        )))
                .isInstanceOf(InvalidPersonalProfileException.class)
                .hasMessageContaining("name must not be blank");

        // Blank relationship
        assertThatThrownBy(() -> personalProfileOperations.createProfile(new CreatePersonalProfileCommand(
                "Name", "   ", false, null, null, null, null, null, null, null, null
        )))
                .isInstanceOf(InvalidPersonalProfileException.class)
                .hasMessageContaining("relationship must not be blank");
    }

    @Test
    @DisplayName("Orders active profiles deterministically by name ASC, id ASC with positive limit")
    void boundedReadsAndOrdering() {
        personalProfileOperations.createProfile(new CreatePersonalProfileCommand("Charlie", "Friend", false, null, null, null, null, null, null, null, null));
        personalProfileOperations.createProfile(new CreatePersonalProfileCommand("Alice", "Colleague", false, null, null, null, null, null, null, null, null));
        personalProfileOperations.createProfile(new CreatePersonalProfileCommand("Bob", "Family", false, null, null, null, null, null, null, null, null));

        List<PersonalProfileView> profiles = personalProfileOperations.findProfiles(2);
        assertThat(profiles).hasSize(2);
        assertThat(profiles.get(0).name()).isEqualTo("Alice");
        assertThat(profiles.get(1).name()).isEqualTo("Bob");

        // Limit validation
        assertThatThrownBy(() -> personalProfileOperations.findProfiles(0))
                .isInstanceOf(InvalidPersonalProfileException.class)
                .hasMessageContaining("Limit must be positive");
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
