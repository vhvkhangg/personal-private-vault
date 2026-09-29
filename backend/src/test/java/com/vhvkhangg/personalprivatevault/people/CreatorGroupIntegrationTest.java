package com.vhvkhangg.personalprivatevault.people;

import com.vhvkhangg.personalprivatevault.people.group.CreatorGroupOperations;
import com.vhvkhangg.personalprivatevault.people.group.command.CreateCreatorGroupCommand;
import com.vhvkhangg.personalprivatevault.people.group.command.UpdateCreatorGroupCommand;
import com.vhvkhangg.personalprivatevault.people.group.exception.CreatorGroupNameAlreadyExistsException;
import com.vhvkhangg.personalprivatevault.people.group.exception.CreatorGroupNotFoundException;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupMemberView;
import com.vhvkhangg.personalprivatevault.people.view.CreatorGroupView;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreatorGroupIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private CreatorGroupOperations creatorGroupOperations;

    @Autowired
    private PersonOperations personOperations;

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
    }

    @Test
    @DisplayName("Creator groups are not Vault Entries")
    void creatorGroupIsNotVaultEntry() {
        CreatorGroupView group = creatorGroupOperations.create(new CreateCreatorGroupCommand(
                "Queen", "British rock band formed in London"
        ));

        assertThat(group.id()).isNotNull();

        // Ensure row exists in creator_groups
        Integer groupCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM creator_groups WHERE id = ?",
                Integer.class,
                group.id()
        );
        assertThat(groupCount).isEqualTo(1);

        // Ensure NO row exists in vault_entries
        Integer vaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE id = ?",
                Integer.class,
                group.id()
        );
        assertThat(vaultCount).isZero();
    }

    @Test
    @DisplayName("Creates and finds creator group with bounded read")
    void createsAndFindsCreatorGroup() {
        CreatorGroupView created = creatorGroupOperations.create(new CreateCreatorGroupCommand(
                "Pink Floyd", "English rock band formed in London in 1965"
        ));

        Optional<CreatorGroupView> found = creatorGroupOperations.find(created.id());
        assertThat(found).isPresent();

        CreatorGroupView view = found.get();
        assertThat(view.name()).isEqualTo("Pink Floyd");
        assertThat(view.description()).isEqualTo("English rock band formed in London in 1965");
        assertThat(view.createdAt()).isNotNull();
        assertThat(view.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Exact duplicate group name create throws CreatorGroupNameAlreadyExistsException")
    void exactDuplicateGroupNameCreateThrowsDomainException() {
        creatorGroupOperations.create(new CreateCreatorGroupCommand(
                "The Beatles", "Legendary rock band"
        ));

        assertThatThrownBy(() -> creatorGroupOperations.create(new CreateCreatorGroupCommand(
                "The Beatles", "Duplicate attempt"
        )))
                .isInstanceOf(CreatorGroupNameAlreadyExistsException.class)
                .hasMessageContaining("The Beatles");
    }

    @Test
    @DisplayName("Concurrent duplicate group creates result in exactly one winner and domain-conflict losers")
    void concurrentDuplicateGroupNameCreatesHaveOneWinnerAndDomainConflictLosers() throws Exception {
        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger unexpectedErrorCount = new AtomicInteger(0);

        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                try {
                    creatorGroupOperations.create(new CreateCreatorGroupCommand(
                            "Radiohead", "Alternative rock band"
                    ));
                    successCount.incrementAndGet();
                } catch (CreatorGroupNameAlreadyExistsException ex) {
                    conflictCount.incrementAndGet();
                } catch (Exception ex) {
                    unexpectedErrorCount.incrementAndGet();
                }
                return null;
            }));
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        for (Future<Void> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(threadCount - 1);
        assertThat(unexpectedErrorCount.get()).isZero();

        Integer totalInDb = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM creator_groups WHERE name = 'Radiohead'",
                Integer.class
        );
        assertThat(totalInDb).isEqualTo(1);
    }

    @Test
    @DisplayName("Updates creator group attributes and updates updatedAt timestamp")
    void updateCreatorGroupSuccessAndTimestampUpdated() throws InterruptedException {
        CreatorGroupView created = creatorGroupOperations.create(new CreateCreatorGroupCommand(
                "Original Band", "Original Description"
        ));

        Thread.sleep(10); // Ensure timestamp advances

        CreatorGroupView updated = creatorGroupOperations.update(new UpdateCreatorGroupCommand(
                created.id(), "Updated Band Name", "Updated Description"
        ));

        assertThat(updated.name()).isEqualTo("Updated Band Name");
        assertThat(updated.description()).isEqualTo("Updated Description");
        assertThat(updated.updatedAt()).isAfterOrEqualTo(created.updatedAt());

        CreatorGroupView reloaded = creatorGroupOperations.find(created.id()).orElseThrow();
        assertThat(reloaded.name()).isEqualTo("Updated Band Name");
    }

    @Test
    @DisplayName("Updating creator group name to one already taken by another group throws CreatorGroupNameAlreadyExistsException")
    void updateCreatorGroupDuplicateNameConflict() {
        creatorGroupOperations.create(new CreateCreatorGroupCommand("Group Alpha", "First"));
        CreatorGroupView beta = creatorGroupOperations.create(new CreateCreatorGroupCommand("Group Beta", "Second"));

        assertThatThrownBy(() -> creatorGroupOperations.update(new UpdateCreatorGroupCommand(
                beta.id(), "Group Alpha", "New desc"
        )))
                .isInstanceOf(CreatorGroupNameAlreadyExistsException.class)
                .hasMessageContaining("Group Alpha");
    }

    @Test
    @DisplayName("Sequential duplicate membership addition is idempotent")
    void sequentialDuplicateMembershipAdditionIsIdempotent() {
        CreatorGroupView group = creatorGroupOperations.create(new CreateCreatorGroupCommand(
                "U2", "Irish rock band"
        ));
        PersonView bono = personOperations.create(new CreatePersonCommand(
                "Bono", null, null, null, null, null, null, null
        ));

        creatorGroupOperations.addMember(group.id(), bono.id());
        creatorGroupOperations.addMember(group.id(), bono.id());
        creatorGroupOperations.addMember(group.id(), bono.id());

        Integer memberCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM creator_group_members WHERE creator_group_id = ? AND person_id = ?",
                Integer.class,
                group.id(),
                bono.id()
        );
        assertThat(memberCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Concurrent duplicate membership additions converge idempotently to exactly one row")
    void concurrentDuplicateMembershipAdditionIsIdempotent() throws Exception {
        CreatorGroupView group = creatorGroupOperations.create(new CreateCreatorGroupCommand(
                "Led Zeppelin", "English rock band"
        ));
        PersonView plant = personOperations.create(new CreatePersonCommand(
                "Robert Plant", null, null, null, null, null, null, null
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
                creatorGroupOperations.addMember(group.id(), plant.id());
                return null;
            }));
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        for (Future<Void> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        Integer memberCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM creator_group_members WHERE creator_group_id = ? AND person_id = ?",
                Integer.class,
                group.id(),
                plant.id()
        );
        assertThat(memberCount).isEqualTo(1);
    }

    @Test
    @DisplayName("getMembers returns bounded ordered views with person names")
    void getMembersReturnsBoundedOrderedViews() {
        CreatorGroupView group = creatorGroupOperations.create(new CreateCreatorGroupCommand(
                "The Police", "English rock band"
        ));

        PersonView sting = personOperations.create(new CreatePersonCommand(
                "Sting", null, null, null, null, null, null, null
        ));
        PersonView summers = personOperations.create(new CreatePersonCommand(
                "Andy Summers", null, null, null, null, null, null, null
        ));
        PersonView copeland = personOperations.create(new CreatePersonCommand(
                "Stewart Copeland", null, null, null, null, null, null, null
        ));

        creatorGroupOperations.addMember(group.id(), sting.id());
        creatorGroupOperations.addMember(group.id(), summers.id());
        creatorGroupOperations.addMember(group.id(), copeland.id());

        List<CreatorGroupMemberView> members = creatorGroupOperations.getMembers(group.id());
        assertThat(members).hasSize(3);

        // Ordered by name: Andy Summers, Stewart Copeland, Sting
        assertThat(members)
                .extracting(CreatorGroupMemberView::personName)
                .containsExactly("Andy Summers", "Stewart Copeland", "Sting");

        assertThat(members)
                .extracting(CreatorGroupMemberView::groupId)
                .containsOnly(group.id());
    }

    @Test
    @DisplayName("Bounded reads: find returns empty and getMembers throws for non-existent group")
    void boundedReadsForNonExistentGroup() {
        assertThat(creatorGroupOperations.find(999999L)).isEmpty();

        assertThatThrownBy(() -> creatorGroupOperations.getMembers(999999L))
                .isInstanceOf(CreatorGroupNotFoundException.class)
                .hasMessageContaining("Creator group not found with id: 999999");
    }

    @Test
    @DisplayName("getMembers returns an immutable list that rejects modification")
    void getMembersReturnsImmutableCollection() {
        CreatorGroupView group = creatorGroupOperations.create(new CreateCreatorGroupCommand(
                "Immutable Band", "Description"
        ));
        PersonView member = personOperations.create(new CreatePersonCommand(
                "Musician", null, null, null, null, null, null, null
        ));
        creatorGroupOperations.addMember(group.id(), member.id());

        List<CreatorGroupMemberView> members = creatorGroupOperations.getMembers(group.id());
        assertThat(members).hasSize(1);

        assertThatThrownBy(() -> members.add(new CreatorGroupMemberView(group.id(), 999L, "Fake")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Adding member to newly created group within an uncommitted enclosing transaction succeeds")
    void addingMemberToNewlyCreatedGroupWithinEnclosingTransactionSucceeds() {
        PersonView person = personOperations.create(new CreatePersonCommand(
                "Existing Musician", null, null, null, null, null, null, null
        ));

        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        Long groupId = tx.execute(status -> {
            CreatorGroupView group = creatorGroupOperations.create(new CreateCreatorGroupCommand(
                    "New Transaction Band", "Formed inside transaction"
            ));
            // Add member to the newly created, uncommitted group before commit
            creatorGroupOperations.addMember(group.id(), person.id());
            return group.id();
        });

        assertThat(groupId).isNotNull();
        List<CreatorGroupMemberView> members = creatorGroupOperations.getMembers(groupId);
        assertThat(members).hasSize(1);
        assertThat(members.get(0).personName()).isEqualTo("Existing Musician");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM creator_group_members WHERE creator_group_id = ? AND person_id = ?",
                Integer.class,
                groupId,
                person.id()
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back group creation")
    void rollbackOfEnclosingTransactionRollsBackCreatedGroup() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            creatorGroupOperations.create(new CreateCreatorGroupCommand(
                    "Doomed Group", "Will be rolled back"
            ));
            throw new RuntimeException("Force rollback of group create");
        })).hasMessageContaining("Force rollback of group create");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM creator_groups WHERE name = 'Doomed Group'",
                Integer.class
        );
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back group update")
    void rollbackOfEnclosingTransactionRollsBackUpdatedGroup() {
        CreatorGroupView original = creatorGroupOperations.create(new CreateCreatorGroupCommand(
                "Original Unchanged", "Original Desc"
        ));

        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            creatorGroupOperations.update(new UpdateCreatorGroupCommand(
                    original.id(), "Modified Name", "Modified Desc"
            ));
            throw new RuntimeException("Force rollback of group update");
        })).hasMessageContaining("Force rollback of group update");

        CreatorGroupView current = creatorGroupOperations.find(original.id()).orElseThrow();
        assertThat(current.name()).isEqualTo("Original Unchanged");
        assertThat(current.description()).isEqualTo("Original Desc");
    }

    @Test
    @DisplayName("Rollback of enclosing transaction rolls back membership additions")
    void rollbackOfEnclosingTransactionRollsBackAddedMember() {
        CreatorGroupView group = creatorGroupOperations.create(new CreateCreatorGroupCommand(
                "Persistent Group", "Desc"
        ));
        PersonView person = personOperations.create(new CreatePersonCommand(
                "Persistent Person", null, null, null, null, null, null, null
        ));

        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> tx.execute(status -> {
            creatorGroupOperations.addMember(group.id(), person.id());
            throw new RuntimeException("Force rollback of add member");
        })).hasMessageContaining("Force rollback of add member");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM creator_group_members WHERE creator_group_id = ? AND person_id = ?",
                Integer.class,
                group.id(),
                person.id()
        );
        assertThat(count).isZero();
        assertThat(creatorGroupOperations.getMembers(group.id())).isEmpty();
    }
}
