package com.vhvkhangg.personalprivatevault.film;

import com.vhvkhangg.personalprivatevault.film.credit.FilmCreditOperations;
import com.vhvkhangg.personalprivatevault.film.credit.command.CreateFilmCreditCommand;
import com.vhvkhangg.personalprivatevault.film.credit.exception.InvalidFilmCreditException;
import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmCreditRole;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.film.film.FilmOperations;
import com.vhvkhangg.personalprivatevault.film.film.command.CreateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.film.exception.FilmNotFoundException;
import com.vhvkhangg.personalprivatevault.film.view.FilmCreditView;
import com.vhvkhangg.personalprivatevault.film.view.FilmView;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.enums.RatingGrade;
import com.vhvkhangg.personalprivatevault.vault.metadata.VaultMetadataOperations;
import com.vhvkhangg.personalprivatevault.vault.view.TagView;
import com.vhvkhangg.personalprivatevault.vault.view.VaultMetadataView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilmCreditIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FilmCreditOperations filmCreditOperations;

    @Autowired
    private FilmOperations filmOperations;

    @Autowired
    private PersonOperations personOperations;

    @Autowired
    private VaultMetadataOperations vaultMetadataOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long filmId;
    private Long personId;

    @BeforeEach
    void cleanUp() {
        tearDown();

        PersonView actor = personOperations.create(new CreatePersonCommand(
                "Keanu Reeves", null, null, null, null, null, null, null
        ));
        personId = actor.id();

        FilmView film = filmOperations.create(new CreateFilmCommand(
                "The Matrix", null, null, null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                null, "Sci-fi masterpiece", null,
                ProgressStatus.COMPLETED, ConsumptionStatus.CONSUMED, null, null
        ));
        filmId = film.id();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM favorites");
        jdbcTemplate.execute("DELETE FROM ratings");
        jdbcTemplate.execute("DELETE FROM vault_entry_tags");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM film_credits");
        jdbcTemplate.execute("DELETE FROM film_links");
        jdbcTemplate.execute("DELETE FROM film_story_archetypes");
        jdbcTemplate.execute("DELETE FROM film_world_settings");
        jdbcTemplate.execute("DELETE FROM film_genre_assignments");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM persons");
        jdbcTemplate.execute("DELETE FROM vault_entries");
    }

    @Test
    @DisplayName("Creates and finds film credit backed by FILM_CREDIT vault entry")
    void createAndFindCredit() {
        FilmCreditView credit = filmCreditOperations.create(new CreateFilmCreditCommand(
                filmId, personId, FilmCreditRole.MAIN, "Neo", "The One"
        ));

        assertThat(credit.id()).isNotNull();
        assertThat(credit.filmId()).isEqualTo(filmId);
        assertThat(credit.personId()).isEqualTo(personId);
        assertThat(credit.role()).isEqualTo(FilmCreditRole.MAIN);
        assertThat(credit.characterName()).isEqualTo("Neo");
        assertThat(credit.note()).isEqualTo("The One");

        // Verify underlying vault entry has type FILM_CREDIT and matching ID
        String entryType = jdbcTemplate.queryForObject(
                "SELECT entry_type::text FROM vault_entries WHERE id = ?",
                String.class,
                credit.id()
        );
        assertThat(entryType).isEqualTo("FILM_CREDIT");

        Optional<FilmCreditView> reloaded = filmCreditOperations.find(credit.id());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().characterName()).isEqualTo("Neo");
    }

    @Test
    @DisplayName("Rollback during credit creation leaves zero orphan vault entries or credit rows")
    void creditRollbackLeavesNoOrphanVaultEntry() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        long initialVaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'FILM_CREDIT'",
                Long.class
        );

        assertThatThrownBy(() -> tx.execute(status -> {
            filmCreditOperations.create(new CreateFilmCreditCommand(
                    filmId, personId, FilmCreditRole.SUPPORTING, "Morpheus", null
            ));
            throw new RuntimeException("Force credit rollback");
        })).hasMessageContaining("Force credit rollback");

        long finalVaultCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vault_entries WHERE entry_type = 'FILM_CREDIT'",
                Long.class
        );
        long creditCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM film_credits",
                Long.class
        );

        assertThat(finalVaultCount).isEqualTo(initialVaultCount);
        assertThat(creditCount).isZero();
    }

    @Test
    @DisplayName("Distinct-record semantics: repeated creates with identical parameters create distinct records")
    void distinctRecordSemanticsAllowMultipleIdenticalCredits() {
        FilmCreditView credit1 = filmCreditOperations.create(new CreateFilmCreditCommand(
                filmId, personId, FilmCreditRole.MAIN, "Neo", "First credit"
        ));
        FilmCreditView credit2 = filmCreditOperations.create(new CreateFilmCreditCommand(
                filmId, personId, FilmCreditRole.MAIN, "Neo", "First credit"
        ));

        assertThat(credit1.id()).isNotEqualTo(credit2.id());

        List<FilmCreditView> credits = filmCreditOperations.findByFilmId(filmId);
        assertThat(credits).hasSize(2);
        assertThat(credits).extracting(FilmCreditView::id).containsExactly(credit1.id(), credit2.id());
    }

    @Test
    @DisplayName("Rejects credit creation with non-existent person or non-existent film")
    void rejectsNonExistentPersonOrFilm() {
        assertThatThrownBy(() -> filmCreditOperations.create(new CreateFilmCreditCommand(
                filmId, 999999L, FilmCreditRole.CAMEO, "Stranger", null
        )))
                .isInstanceOf(InvalidFilmCreditException.class)
                .hasMessageContaining("Person with ID 999999 does not exist");

        assertThatThrownBy(() -> filmCreditOperations.create(new CreateFilmCreditCommand(
                999999L, personId, FilmCreditRole.CAMEO, "Stranger", null
        )))
                .isInstanceOf(FilmNotFoundException.class)
                .hasMessageContaining("Film with ID 999999 not found");
    }

    @Test
    @DisplayName("Vault capability integration for FILM_CREDIT: favorite succeeds, rating and tagging are rejected")
    void vaultCapabilityIntegrationForFilmCredit() {
        FilmCreditView credit = filmCreditOperations.create(new CreateFilmCreditCommand(
                filmId, personId, FilmCreditRole.MAIN, "Neo", null
        ));

        // Favorite: allowed
        vaultMetadataOperations.favorite(credit.id());
        VaultMetadataView meta = vaultMetadataOperations.metadata(credit.id());
        assertThat(meta.favorite()).isTrue();

        // Rating: rejected by VaultCapabilityMatrix
        assertThatThrownBy(() -> vaultMetadataOperations.setRating(credit.id(), RatingGrade.S))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not support ratings");

        // Tags: rejected by VaultCapabilityMatrix
        TagView tag = vaultMetadataOperations.createTag("Favorite Actor Performance");
        assertThatThrownBy(() -> vaultMetadataOperations.attachTag(credit.id(), tag.id()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not support tags");
    }
}
