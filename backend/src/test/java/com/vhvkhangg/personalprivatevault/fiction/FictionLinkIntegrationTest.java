package com.vhvkhangg.personalprivatevault.fiction;

import com.vhvkhangg.personalprivatevault.fiction.enums.ConsumptionStatus;
import com.vhvkhangg.personalprivatevault.fiction.enums.FictionFormat;
import com.vhvkhangg.personalprivatevault.fiction.enums.ProgressStatus;
import com.vhvkhangg.personalprivatevault.fiction.fiction.FictionOperations;
import com.vhvkhangg.personalprivatevault.fiction.fiction.command.CreateFictionCommand;
import com.vhvkhangg.personalprivatevault.fiction.genre.FictionGenreOperations;
import com.vhvkhangg.personalprivatevault.fiction.genre.command.CreateFictionGenreCommand;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionGenreView;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionView;
import com.vhvkhangg.personalprivatevault.fiction.link.FictionLinkOperations;
import com.vhvkhangg.personalprivatevault.fiction.link.command.CreateFictionLinkCommand;
import com.vhvkhangg.personalprivatevault.fiction.link.command.UpdateFictionLinkCommand;
import com.vhvkhangg.personalprivatevault.fiction.link.exception.FictionLinkNotFoundException;
import com.vhvkhangg.personalprivatevault.fiction.link.exception.InvalidFictionLinkException;
import com.vhvkhangg.personalprivatevault.fiction.view.FictionLinkView;
import com.vhvkhangg.personalprivatevault.people.person.PersonOperations;
import com.vhvkhangg.personalprivatevault.people.person.command.CreatePersonCommand;
import com.vhvkhangg.personalprivatevault.people.view.PersonView;
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

class FictionLinkIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private FictionLinkOperations fictionLinkOperations;

    @Autowired
    private FictionOperations fictionOperations;

    @Autowired
    private FictionGenreOperations genreOperations;

    @Autowired
    private PersonOperations personOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long fictionId;
    private Long genreId;

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

        PersonView author = personOperations.create(new CreatePersonCommand(
                "Author One", null, null, null, null, null, null, null
        ));
        FictionGenreView genre = genreOperations.create(new CreateFictionGenreCommand(
                "Adventure", "Adventure genre"
        ));
        genreId = genre.id();
        FictionView fiction = fictionOperations.create(new CreateFictionCommand(
                "Test Adventure Fiction", null, null, null, FictionFormat.NOVEL, false,
                genreId, author.id(), null, null, 100,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));
        fictionId = fiction.id();
    }

    @Test
    @DisplayName("Creates and finds link scoped to parent fiction")
    void createAndFindLink() {
        FictionLinkView created = fictionLinkOperations.create(new CreateFictionLinkCommand(
                fictionId, "en", "ORIGINAL", "Official Site", "https://official.example.com", true
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.fictionId()).isEqualTo(fictionId);
        assertThat(created.languageCode()).isEqualTo("en");
        assertThat(created.linkType()).isEqualTo("ORIGINAL");
        assertThat(created.label()).isEqualTo("Official Site");
        assertThat(created.url()).isEqualTo("https://official.example.com");
        assertThat(created.isPrimary()).isTrue();
        assertThat(created.createdAt()).isNotNull();

        Optional<FictionLinkView> found = fictionLinkOperations.findById(fictionId, created.id());
        assertThat(found).isPresent();
        assertThat(found.get().url()).isEqualTo("https://official.example.com");
    }

    @Test
    @DisplayName("Multiple links with the same language and type are permitted by Schema v1")
    void multipleLinksSameLanguageAndTypeAllowed() {
        FictionLinkView link1 = fictionLinkOperations.create(new CreateFictionLinkCommand(
                fictionId, "en", "TRANSLATION", "Translator A", "https://trans-a.example.com", false
        ));
        FictionLinkView link2 = fictionLinkOperations.create(new CreateFictionLinkCommand(
                fictionId, "en", "TRANSLATION", "Translator B", "https://trans-b.example.com", false
        ));

        assertThat(link1.id()).isNotEqualTo(link2.id());

        List<FictionLinkView> links = fictionLinkOperations.findByFictionId(fictionId);
        assertThat(links).hasSize(2);
        assertThat(links).extracting(FictionLinkView::url)
                .containsExactly("https://trans-a.example.com", "https://trans-b.example.com");
    }

    @Test
    @DisplayName("Creates link with null language code succeeds")
    void createLinkWithoutLanguageSucceeds() {
        FictionLinkView link = fictionLinkOperations.create(new CreateFictionLinkCommand(
                fictionId, null, "CONVERT", "Epub Download", "https://dl.example.com/file.epub", false
        ));

        assertThat(link.languageCode()).isNull();
        assertThat(link.linkType()).isEqualTo("CONVERT");
    }

    @Test
    @DisplayName("Rejects link creation with non-existent language code")
    void rejectsInvalidLanguageCode() {
        assertThatThrownBy(() -> fictionLinkOperations.create(new CreateFictionLinkCommand(
                fictionId, "zz", "ORIGINAL", null, "https://example.com", false
        )))
                .isInstanceOf(InvalidFictionLinkException.class)
                .hasMessageContaining("Language code 'zz' does not exist in reference catalog");
    }

    @Test
    @DisplayName("Updates existing link scoped to parent fiction")
    void updateLinkSuccess() {
        FictionLinkView created = fictionLinkOperations.create(new CreateFictionLinkCommand(
                fictionId, "en", "ORIGINAL", "Old Label", "https://old.example.com", false
        ));

        FictionLinkView updated = fictionLinkOperations.update(new UpdateFictionLinkCommand(
                created.id(), fictionId, "vi", "TRANSLATION", "Vietnamese Translation", "https://new.example.com", true
        ));

        assertThat(updated.languageCode()).isEqualTo("vi");
        assertThat(updated.linkType()).isEqualTo("TRANSLATION");
        assertThat(updated.label()).isEqualTo("Vietnamese Translation");
        assertThat(updated.url()).isEqualTo("https://new.example.com");
        assertThat(updated.isPrimary()).isTrue();

        FictionLinkView reloaded = fictionLinkOperations.findById(fictionId, created.id()).orElseThrow();
        assertThat(reloaded.url()).isEqualTo("https://new.example.com");
    }

    @Test
    @DisplayName("Update throws FictionLinkNotFoundException when fictionId does not match link parent")
    void updateLinkWithMismatchedParentThrowsNotFound() {
        // Create a second fiction
        PersonView author2 = personOperations.create(new CreatePersonCommand(
                "Author Two", null, null, null, null, null, null, null
        ));
        FictionView fiction2 = fictionOperations.create(new CreateFictionCommand(
                "Second Fiction", null, null, null, FictionFormat.COMIC, false,
                genreId, author2.id(), null, null, null,
                ProgressStatus.ONGOING, ConsumptionStatus.UNCONSUMED, null, null
        ));

        FictionLinkView link = fictionLinkOperations.create(new CreateFictionLinkCommand(
                fictionId, "en", "ORIGINAL", null, "https://example.com", false
        ));

        // Try updating link with fiction2.id()
        assertThatThrownBy(() -> fictionLinkOperations.update(new UpdateFictionLinkCommand(
                link.id(), fiction2.id(), "en", "ORIGINAL", null, "https://example.com", false
        )))
                .isInstanceOf(FictionLinkNotFoundException.class)
                .hasMessageContaining("does not belong to fiction " + fiction2.id());
    }

    @Test
    @DisplayName("Parent-scoped findById returns empty when querying with mismatched fictionId")
    void findByIdReturnsEmptyForMismatchedParent() {
        FictionLinkView link = fictionLinkOperations.create(new CreateFictionLinkCommand(
                fictionId, "en", "ORIGINAL", null, "https://example.com", false
        ));

        Optional<FictionLinkView> notFound = fictionLinkOperations.findById(999999L, link.id());
        assertThat(notFound).isEmpty();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM fiction_story_archetypes");
        jdbcTemplate.execute("DELETE FROM fiction_world_settings");
        jdbcTemplate.execute("DELETE FROM fiction_links");
        jdbcTemplate.execute("DELETE FROM fictions");
        jdbcTemplate.execute("DELETE FROM fiction_genres");
        jdbcTemplate.execute("DELETE FROM creator_group_members");
        jdbcTemplate.execute("DELETE FROM creator_groups");
        jdbcTemplate.execute("DELETE FROM person_roles");
        jdbcTemplate.execute("DELETE FROM persons");
        jdbcTemplate.execute("DELETE FROM vault_entry_tags");
        jdbcTemplate.execute("DELETE FROM favorites");
        jdbcTemplate.execute("DELETE FROM ratings");
        jdbcTemplate.execute("DELETE FROM vault_entries");
    }
}
