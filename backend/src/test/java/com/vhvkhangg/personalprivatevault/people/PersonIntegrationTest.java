package com.vhvkhangg.personalprivatevault.people;

import com.vhvkhangg.personalprivatevault.people.enums.Gender;
import com.vhvkhangg.personalprivatevault.people.enums.PersonRole;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.person.command.UpdatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.person.exception.InvalidPersonException;
import com.vhvkhangg.personalprivatevault.people.person.exception.PersonNotFoundException;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private PersonOperations personOperations;

    @Autowired
    private VaultEntryOperations vaultEntryOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("DELETE FROM creator_group_members");
        jdbcTemplate.execute("DELETE FROM creator_groups");
        jdbcTemplate.execute("DELETE FROM person_roles");
        jdbcTemplate.execute("DELETE FROM persons");
        jdbcTemplate.execute("DELETE FROM vault_entry_tags");
        jdbcTemplate.execute("DELETE FROM favorites");
        jdbcTemplate.execute("DELETE FROM ratings");
        jdbcTemplate.execute("DELETE FROM vault_entries");

        // Seed reference country fixtures needed for tests
        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('US', 'United States', 'Hoa Kỳ')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('VN', 'Vietnam', 'Việt Nam')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('GB', 'United Kingdom', 'Vương quốc Anh')
                ON CONFLICT (code) DO NOTHING
                """);
    }

    @Test
    @DisplayName("Creates person profile with shared Vault Entry identity")
    void createsPersonWithSharedVaultIdentity() {
        CreatePersonCommand command = new CreatePersonCommand(
                "Christopher Nolan",
                "https://example.com/nolan.jpg",
                Gender.MALE,
                LocalDate.of(1970, 7, 30),
                new BigDecimal("181.00"),
                new BigDecimal("74.50"),
                "GB",
                "Acclaimed filmmaker"
        );

        PersonView created = personOperations.create(command);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Christopher Nolan");
        assertThat(created.gender()).isEqualTo(Gender.MALE);
        assertThat(created.birthDate()).isEqualTo(LocalDate.of(1970, 7, 30));
        assertThat(created.nationalityCode()).isEqualTo("GB");
        assertThat(created.roles()).isEmpty();

        // Verify shared vault entry exists with entry_type = PERSON
        String entryType = jdbcTemplate.queryForObject(
                "SELECT entry_type FROM vault_entries WHERE id = ?",
                String.class,
                created.id()
        );
        assertThat(entryType).isEqualTo("PERSON");

        // Verify public VaultEntryOperations sees the entry
        assertThat(vaultEntryOperations.find(created.id())).isPresent();
    }

    @Test
    @DisplayName("Failed person creation rolls back transaction and leaves no orphan Vault Entry")
    void failedPersonCreationLeavesNoOrphanVaultEntry() {
        long initialVaultEntriesCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries",
                Long.class
        );

        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            // Create a valid person
            personOperations.create(new CreatePersonCommand(
                    "Temporary Person",
                    null, null, null, null, null, null, null
            ));
            // Force an exception within the same transaction
            throw new RuntimeException("Simulated transaction failure");
        })).hasMessageContaining("Simulated transaction failure");

        long finalVaultEntriesCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries",
                Long.class
        );
        long finalPersonsCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM persons",
                Long.class
        );

        assertThat(finalVaultEntriesCount).isEqualTo(initialVaultEntriesCount);
        assertThat(finalPersonsCount).isZero();
    }

    @Test
    @DisplayName("Reads person view with assigned roles and validates immutability")
    void readPersonReturnsBoundedViewWithRoles() {
        PersonView created = personOperations.create(new CreatePersonCommand(
                "David Bowie", null, Gender.MALE, LocalDate.of(1947, 1, 8),
                new BigDecimal("178.00"), null, "GB", "Music legend"
        ));

        personOperations.addRole(created.id(), PersonRole.SINGER);
        personOperations.addRole(created.id(), PersonRole.ACTOR);

        Optional<PersonView> found = personOperations.find(created.id());
        assertThat(found).isPresent();

        PersonView view = found.get();
        assertThat(view.name()).isEqualTo("David Bowie");
        assertThat(view.roles()).containsExactlyInAnyOrder(PersonRole.SINGER, PersonRole.ACTOR);

        // Verify roles set is immutable
        Set<PersonRole> roles = view.roles();
        assertThatThrownBy(() -> roles.add(PersonRole.DIRECTOR))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Updates person profile attributes while preserving roles and vault entry")
    void updatePersonModifiesProfilePreservingRolesAndVaultEntry() {
        PersonView created = personOperations.create(new CreatePersonCommand(
                "Original Name", null, Gender.FEMALE, null, null, null, null, null
        ));
        personOperations.addRole(created.id(), PersonRole.ARTIST);

        UpdatePersonCommand updateCmd = new UpdatePersonCommand(
                created.id(),
                "Updated Name",
                "https://example.com/avatar.png",
                Gender.FEMALE,
                LocalDate.of(1995, 5, 15),
                new BigDecimal("165.50"),
                new BigDecimal("52.00"),
                "VN",
                "Updated biography"
        );

        PersonView updated = personOperations.update(updateCmd);

        assertThat(updated.name()).isEqualTo("Updated Name");
        assertThat(updated.avatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(updated.birthDate()).isEqualTo(LocalDate.of(1995, 5, 15));
        assertThat(updated.heightCm()).isEqualByComparingTo(new BigDecimal("165.50"));
        assertThat(updated.weightKg()).isEqualByComparingTo(new BigDecimal("52.00"));
        assertThat(updated.nationalityCode()).isEqualTo("VN");
        assertThat(updated.notes()).isEqualTo("Updated biography");
        assertThat(updated.roles()).containsExactly(PersonRole.ARTIST);

        // Vault entry remains intact
        assertThat(vaultEntryOperations.find(created.id())).isPresent();
    }

    @Test
    @DisplayName("Sequential duplicate role addition is idempotent")
    void sequentialDuplicateRoleAdditionIsIdempotent() {
        PersonView created = personOperations.create(new CreatePersonCommand(
                "Test Actor", null, null, null, null, null, null, null
        ));

        personOperations.addRole(created.id(), PersonRole.ACTOR);
        personOperations.addRole(created.id(), PersonRole.ACTOR);
        personOperations.addRole(created.id(), PersonRole.ACTOR);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM person_roles WHERE person_id = ? AND role = 'ACTOR'",
                Integer.class,
                created.id()
        );
        assertThat(count).isEqualTo(1);
        assertThat(personOperations.getRoles(created.id())).containsExactly(PersonRole.ACTOR);
    }

    @Test
    @DisplayName("Concurrent duplicate role additions converge idempotently to exactly one row")
    void concurrentDuplicateRoleAdditionIsIdempotent() throws Exception {
        PersonView created = personOperations.create(new CreatePersonCommand(
                "Concurrent Actor", null, null, null, null, null, null, null
        ));

        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                personOperations.addRole(created.id(), PersonRole.SINGER);
                return null;
            }));
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        for (Future<Void> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM person_roles WHERE person_id = ? AND role = 'SINGER'",
                Integer.class,
                created.id()
        );
        assertThat(count).isEqualTo(1);
        assertThat(personOperations.getRoles(created.id())).containsExactly(PersonRole.SINGER);
    }

    @Test
    @DisplayName("Supports assigning all five frozen roles to a single person")
    void supportsAssigningAllFiveRoles() {
        PersonView created = personOperations.create(new CreatePersonCommand(
                "Multitalented Artist", null, null, null, null, null, null, null
        ));

        for (PersonRole role : PersonRole.values()) {
            personOperations.addRole(created.id(), role);
        }

        Set<PersonRole> roles = personOperations.getRoles(created.id());
        assertThat(roles).containsExactlyInAnyOrder(
                PersonRole.ACTOR,
                PersonRole.SINGER,
                PersonRole.DIRECTOR,
                PersonRole.AUTHOR,
                PersonRole.ARTIST
        );
    }

    @Test
    @DisplayName("Validates nationality code against ReferenceCatalog CountryView")
    void nationalityResolvesAgainstReferenceCatalog() {
        PersonView person = personOperations.create(new CreatePersonCommand(
                "John Doe", null, null, null, null, null, "us", null
        ));
        assertThat(person.nationalityCode()).isEqualTo("US");

        assertThatThrownBy(() -> personOperations.create(new CreatePersonCommand(
                "Jane Doe", null, null, null, null, null, "FR", null
        )))
                .isInstanceOf(InvalidPersonException.class)
                .hasMessageContaining("Nationality code 'FR' does not exist in reference catalog");
    }

    @Test
    @DisplayName("Bounded reads: find returns empty and getRoles throws for non-existent person")
    void boundedReadsForNonExistentPerson() {
        assertThat(personOperations.find(999999L)).isEmpty();

        assertThatThrownBy(() -> personOperations.getRoles(999999L))
                .isInstanceOf(PersonNotFoundException.class)
                .hasMessageContaining("Person not found with id: 999999");
    }

    @Test
    @DisplayName("Adding role to newly created person within an uncommitted enclosing transaction succeeds")
    void addingRoleToNewlyCreatedPersonWithinEnclosingTransactionSucceeds() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        Long personId = tx.execute(status -> {
            PersonView person = personOperations.create(new CreatePersonCommand(
                    "Transaction Person", null, null, null, null, null, null, null
            ));
            // Add role to the newly created, uncommitted person before commit
            personOperations.addRole(person.id(), PersonRole.ACTOR);
            return person.id();
        });

        assertThat(personId).isNotNull();
        assertThat(personOperations.getRoles(personId)).containsExactly(PersonRole.ACTOR);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM person_roles WHERE person_id = ? AND role = 'ACTOR'",
                Integer.class,
                personId
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back role additions")
    void rollbackOfEnclosingTransactionRollsBackAddedRole() {
        PersonView person = personOperations.create(new CreatePersonCommand(
                "Existing Person", null, null, null, null, null, null, null
        ));

        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            personOperations.addRole(person.id(), PersonRole.SINGER);
            throw new RuntimeException("Simulated failure to force rollback");
        })).hasMessageContaining("Simulated failure to force rollback");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM person_roles WHERE person_id = ? AND role = 'SINGER'",
                Integer.class,
                person.id()
        );
        assertThat(count).isZero();
        assertThat(personOperations.getRoles(person.id())).isEmpty();
    }
}
