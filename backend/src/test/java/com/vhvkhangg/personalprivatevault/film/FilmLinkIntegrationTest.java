package com.vhvkhangg.personalprivatevault.film;

import com.vhvkhangg.personalprivatevault.film.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.film.enums.FilmFormat;
import com.vhvkhangg.personalprivatevault.film.enums.FilmProductionStyle;
import com.vhvkhangg.personalprivatevault.film.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.film.film.FilmOperations;
import com.vhvkhangg.personalprivatevault.film.film.command.CreateFilmCommand;
import com.vhvkhangg.personalprivatevault.film.link.FilmLinkOperations;
import com.vhvkhangg.personalprivatevault.film.link.command.CreateFilmLinkCommand;
import com.vhvkhangg.personalprivatevault.film.link.command.UpdateFilmLinkCommand;
import com.vhvkhangg.personalprivatevault.film.link.exception.FilmLinkNotFoundException;
import com.vhvkhangg.personalprivatevault.film.link.exception.InvalidFilmLinkException;
import com.vhvkhangg.personalprivatevault.film.view.FilmLinkView;
import com.vhvkhangg.personalprivatevault.film.view.FilmView;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilmLinkIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FilmLinkOperations filmLinkOperations;

    @Autowired
    private FilmOperations filmOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long filmId;

    @BeforeEach
    void cleanUp() {
        tearDown();

        jdbcTemplate.update("""
                INSERT INTO languages (code, name_en, name_vi) VALUES ('en', 'English', 'Tiếng Anh')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO languages (code, name_en, name_vi) VALUES ('vi', 'Vietnamese', 'Tiếng Việt')
                ON CONFLICT (code) DO NOTHING
                """);

        FilmView film = filmOperations.create(new CreateFilmCommand(
                "Test Cinema Film", null, null, null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                null, "A thrilling movie", null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));
        filmId = film.id();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM film_credits");
        jdbcTemplate.execute("DELETE FROM film_links");
        jdbcTemplate.execute("DELETE FROM film_story_archetypes");
        jdbcTemplate.execute("DELETE FROM film_world_settings");
        jdbcTemplate.execute("DELETE FROM film_genre_assignments");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM film_genres");
    }

    @Test
    @DisplayName("Creates and finds link scoped to parent film")
    void createAndFindLink() {
        FilmLinkView created = filmLinkOperations.create(new CreateFilmLinkCommand(
                filmId, "en", "Official Site", "https://official.film.example.com", true
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.filmId()).isEqualTo(filmId);
        assertThat(created.languageCode()).isEqualTo("en");
        assertThat(created.label()).isEqualTo("Official Site");
        assertThat(created.url()).isEqualTo("https://official.film.example.com");
        assertThat(created.isPrimary()).isTrue();
        assertThat(created.createdAt()).isNotNull();

        Optional<FilmLinkView> found = filmLinkOperations.findById(filmId, created.id());
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(created.id());
        assertThat(found.get().url()).isEqualTo("https://official.film.example.com");
    }

    @Test
    @DisplayName("Updates existing link scoped to parent film")
    void updateLink() {
        FilmLinkView created = filmLinkOperations.create(new CreateFilmLinkCommand(
                filmId, "en", "Initial", "https://initial.example.com", false
        ));

        FilmLinkView updated = filmLinkOperations.update(new UpdateFilmLinkCommand(
                created.id(), filmId, "vi", "Updated Label", "https://updated.example.com", true
        ));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.languageCode()).isEqualTo("vi");
        assertThat(updated.label()).isEqualTo("Updated Label");
        assertThat(updated.url()).isEqualTo("https://updated.example.com");
        assertThat(updated.isPrimary()).isTrue();

        Optional<FilmLinkView> reloaded = filmLinkOperations.findById(filmId, created.id());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().label()).isEqualTo("Updated Label");
        assertThat(reloaded.get().isPrimary()).isTrue();
    }

    @Test
    @DisplayName("Updating a link belonging to a different film throws FilmLinkNotFoundException")
    void rejectsUpdateBelongingToDifferentFilm() {
        FilmView anotherFilm = filmOperations.create(new CreateFilmCommand(
                "Another Film", null, null, null,
                FilmFormat.SERIES, FilmProductionStyle.ANIMATION, false,
                null, null, 12,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));

        FilmLinkView createdOnAnother = filmLinkOperations.create(new CreateFilmLinkCommand(
                anotherFilm.id(), "en", "Another Link", "https://another.example.com", false
        ));

        assertThatThrownBy(() -> filmLinkOperations.update(new UpdateFilmLinkCommand(
                createdOnAnother.id(), filmId, "en", "Hijacked", "https://hijacked.example.com", false
        )))
                .isInstanceOf(FilmLinkNotFoundException.class)
                .hasMessageContaining("does not belong to film " + filmId);
    }

    @Test
    @DisplayName("Rejects creating link with invalid language code")
    void rejectsInvalidLanguageCode() {
        assertThatThrownBy(() -> filmLinkOperations.create(new CreateFilmLinkCommand(
                filmId, "invalid_lang", "Label", "https://example.com", false
        )))
                .isInstanceOf(InvalidFilmLinkException.class)
                .hasMessageContaining("Language code 'invalid_lang' does not exist in reference catalog");
    }

    @Test
    @DisplayName("Allows multiple links with duplicate language and URL")
    void allowsDuplicateLinksWithoutUnsupportedUniqueness() {
        FilmLinkView link1 = filmLinkOperations.create(new CreateFilmLinkCommand(
                filmId, "en", "Link 1", "https://shared.example.com", false
        ));
        FilmLinkView link2 = filmLinkOperations.create(new CreateFilmLinkCommand(
                filmId, "en", "Link 2", "https://shared.example.com", true
        ));

        assertThat(link1.id()).isNotEqualTo(link2.id());
        assertThat(link1.url()).isEqualTo(link2.url());

        List<FilmLinkView> links = filmLinkOperations.findByFilmId(filmId);
        assertThat(links).hasSize(2);
        assertThat(links).extracting(FilmLinkView::id).containsExactly(link1.id(), link2.id());
    }

    @Test
    @DisplayName("findById returns empty when queried with mismatched parent film ID")
    void findByIdReturnsEmptyForMismatchedParent() {
        FilmLinkView link = filmLinkOperations.create(new CreateFilmLinkCommand(
                filmId, "en", "Test", "https://example.com", false
        ));

        FilmView otherFilm = filmOperations.create(new CreateFilmCommand(
                "Other Film", null, null, null,
                FilmFormat.MOVIE, FilmProductionStyle.LIVE_ACTION, false,
                null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));

        Optional<FilmLinkView> foundOnOther = filmLinkOperations.findById(otherFilm.id(), link.id());
        assertThat(foundOnOther).isEmpty();
    }
}
