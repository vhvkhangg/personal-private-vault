package com.vhvkhangg.personalprivatevault.search;

import com.vhvkhangg.personalprivatevault.search.enums.SearchDomain;
import com.vhvkhangg.personalprivatevault.search.enums.SearchMatchKind;
import com.vhvkhangg.personalprivatevault.search.query.GlobalSearchOperations;
import com.vhvkhangg.personalprivatevault.search.query.GlobalSearchQuery;
import com.vhvkhangg.personalprivatevault.search.view.GlobalSearchPage;
import com.vhvkhangg.personalprivatevault.search.view.GlobalSearchResult;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.vhvkhangg.personalprivatevault.account.search.AccountSearchQuery;
import com.vhvkhangg.personalprivatevault.collection.search.CollectionSearchQuery;
import com.vhvkhangg.personalprivatevault.feed.search.FeedSearchQuery;
import com.vhvkhangg.personalprivatevault.fiction.search.FictionSearchQuery;
import com.vhvkhangg.personalprivatevault.film.search.FilmSearchQuery;
import com.vhvkhangg.personalprivatevault.knowledge.search.KnowledgeSearchQuery;
import com.vhvkhangg.personalprivatevault.location.search.LocationSearchQuery;
import com.vhvkhangg.personalprivatevault.media.search.MediaSearchQuery;
import com.vhvkhangg.personalprivatevault.people.search.PeopleSearchOperations;
import com.vhvkhangg.personalprivatevault.people.search.PeopleSearchQuery;
import com.vhvkhangg.personalprivatevault.vault.search.VaultSearchOperations;
import com.vhvkhangg.personalprivatevault.vault.search.VaultTagSearchQuery;

import com.vhvkhangg.personalprivatevault.account.search.AccountSearchOperations;
import com.vhvkhangg.personalprivatevault.collection.music.search.MusicSearchOperations;
import com.vhvkhangg.personalprivatevault.collection.search.CollectionSearchHit;
import com.vhvkhangg.personalprivatevault.collection.search.CollectionSearchOperations;
import com.vhvkhangg.personalprivatevault.collection.shopping.search.ShoppingSearchOperations;
import com.vhvkhangg.personalprivatevault.collection.software.search.SoftwareSearchOperations;
import com.vhvkhangg.personalprivatevault.feed.search.FeedSearchOperations;
import com.vhvkhangg.personalprivatevault.fiction.search.FictionSearchOperations;
import com.vhvkhangg.personalprivatevault.film.search.FilmSearchOperations;
import com.vhvkhangg.personalprivatevault.knowledge.information.search.InformationSearchOperations;
import com.vhvkhangg.personalprivatevault.knowledge.note.search.NoteSearchOperations;
import com.vhvkhangg.personalprivatevault.knowledge.search.KnowledgeSearchOperations;
import com.vhvkhangg.personalprivatevault.knowledge.study.search.StudySearchOperations;
import com.vhvkhangg.personalprivatevault.knowledge.vocabulary.search.VocabularySearchOperations;
import com.vhvkhangg.personalprivatevault.location.search.LocationSearchOperations;
import com.vhvkhangg.personalprivatevault.media.search.MediaSearchOperations;
import com.vhvkhangg.personalprivatevault.people.search.PeopleSearchDocument;
import com.vhvkhangg.personalprivatevault.people.search.PeopleSearchHit;
import com.vhvkhangg.personalprivatevault.search.internal.application.GlobalSearchService;
import com.vhvkhangg.personalprivatevault.vault.search.VaultTagCandidateHit;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlobalSearchIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private GlobalSearchOperations searchOperations;

    @Autowired
    private PeopleSearchOperations peopleSearchOperations;

    @Autowired
    private VaultSearchOperations vaultSearchOperations;

    @Autowired
    private FictionSearchOperations fictionSearchOperations;

    @Autowired
    private FilmSearchOperations filmSearchOperations;

    @Autowired
    private MediaSearchOperations mediaSearchOperations;

    @Autowired
    private LocationSearchOperations locationSearchOperations;

    @Autowired
    private KnowledgeSearchOperations knowledgeSearchOperations;

    @Autowired
    private CollectionSearchOperations collectionSearchOperations;

    @Autowired
    private AccountSearchOperations accountSearchOperations;

    @Autowired
    private FeedSearchOperations feedSearchOperations;

    @Autowired
    private StudySearchOperations studySearchOperations;

    @Autowired
    private MusicSearchOperations musicSearchOperations;

    @Autowired
    private ShoppingSearchOperations shoppingSearchOperations;

    @Autowired
    private SoftwareSearchOperations softwareSearchOperations;

    @Autowired
    private NoteSearchOperations noteSearchOperations;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private Long testPlatformId;
    private Long testFictionGenreId;
    private Long testAddressId;

    @BeforeEach
    void setUp() {
        cleanUp();

        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('VN', 'Vietnam', 'Việt Nam')
                ON CONFLICT (code) DO NOTHING
                """);

        jdbcTemplate.update("""
                INSERT INTO languages (code, name_en, name_vi) VALUES ('en', 'English', 'Tiếng Anh')
                ON CONFLICT (code) DO NOTHING
                """);

        testPlatformId = jdbcTemplate.queryForObject(
                "INSERT INTO platforms (name, kind, url) VALUES ('Web Platform', 'WEB', 'https://platform.example.com') RETURNING id",
                Long.class);

        testFictionGenreId = jdbcTemplate.queryForObject(
                "INSERT INTO fiction_genres (name) VALUES ('Sci-Fi') RETURNING id",
                Long.class);

        testAddressId = jdbcTemplate.queryForObject(
                "INSERT INTO addresses (country_code, locality, street_address) VALUES ('VN', 'Hanoi City', '123 Trang Tien') RETURNING id",
                Long.class);
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        jdbcTemplate.execute("DELETE FROM saved_resource_conversions");
        jdbcTemplate.execute("DELETE FROM saved_resources");
        jdbcTemplate.execute("DELETE FROM follower_snapshot_entries");
        jdbcTemplate.execute("DELETE FROM follower_snapshots");
        jdbcTemplate.execute("DELETE FROM external_account_relationships");
        jdbcTemplate.execute("DELETE FROM external_accounts");
        jdbcTemplate.execute("DELETE FROM software_item_platforms");
        jdbcTemplate.execute("DELETE FROM software_items");
        jdbcTemplate.execute("DELETE FROM shopping_items");
        jdbcTemplate.execute("DELETE FROM music_track_people");
        jdbcTemplate.execute("DELETE FROM music_tracks");
        jdbcTemplate.execute("DELETE FROM notes");
        jdbcTemplate.execute("DELETE FROM vocabulary_reviews");
        jdbcTemplate.execute("DELETE FROM vocabulary_items");
        jdbcTemplate.execute("DELETE FROM information_items");
        jdbcTemplate.execute("DELETE FROM study_items");
        jdbcTemplate.execute("DELETE FROM location_business_hours");
        jdbcTemplate.execute("DELETE FROM location_dining_service_styles");
        jdbcTemplate.execute("DELETE FROM location_category_assignments");
        jdbcTemplate.execute("DELETE FROM locations");
        jdbcTemplate.execute("DELETE FROM addresses");
        jdbcTemplate.execute("DELETE FROM brands");
        jdbcTemplate.execute("DELETE FROM images");
        jdbcTemplate.execute("DELETE FROM albums");
        jdbcTemplate.execute("DELETE FROM film_credits");
        jdbcTemplate.execute("DELETE FROM film_links");
        jdbcTemplate.execute("DELETE FROM film_world_settings");
        jdbcTemplate.execute("DELETE FROM film_story_archetypes");
        jdbcTemplate.execute("DELETE FROM film_genre_assignments");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM fiction_links");
        jdbcTemplate.execute("DELETE FROM fiction_world_settings");
        jdbcTemplate.execute("DELETE FROM fiction_story_archetypes");
        jdbcTemplate.execute("DELETE FROM fictions");
        jdbcTemplate.execute("DELETE FROM fiction_genres");
        jdbcTemplate.execute("DELETE FROM creator_group_members");
        jdbcTemplate.execute("DELETE FROM creator_groups");
        jdbcTemplate.execute("DELETE FROM person_roles");
        jdbcTemplate.execute("DELETE FROM persons");
        jdbcTemplate.execute("DELETE FROM vault_entry_tags");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM favorites");
        jdbcTemplate.execute("DELETE FROM ratings");
        jdbcTemplate.execute("DELETE FROM vault_entries");
        jdbcTemplate.execute("DELETE FROM platforms");
    }

    private long insertVaultEntry(VaultEntryType type, Instant deletedAt) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO vault_entries (entry_type, created_at, updated_at, deleted_at) VALUES (?::vault_entry_type, now(), now(), ?) RETURNING id",
                Long.class,
                type.name(),
                deletedAt != null ? Timestamp.from(deletedAt) : null
        );
    }

    private long insertTag(String name) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO tags (name, created_at) VALUES (?, now()) ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name RETURNING id",
                Long.class,
                name
        );
    }

    private void attachTag(long vaultEntryId, long tagId) {
        jdbcTemplate.update(
                "INSERT INTO vault_entry_tags (vault_entry_id, tag_id, created_at) VALUES (?, ?, now()) ON CONFLICT DO NOTHING",
                vaultEntryId, tagId);
    }

    private long createPerson(String name, String notes) {
        long id = insertVaultEntry(VaultEntryType.PERSON, null);
        jdbcTemplate.update("INSERT INTO persons (id, name, notes) VALUES (?, ?, ?)", id, name, notes);
        return id;
    }

    private long createFiction(String title, String originalTitle, String description, String review) {
        long authorId = createPerson("Fiction Author", null);
        long id = insertVaultEntry(VaultEntryType.FICTION, null);
        jdbcTemplate.update(
                """
                INSERT INTO fictions (id, title, original_title, genre_id, author_person_id, format, progress_status, consumption_status, description, review)
                VALUES (?, ?, ?, ?, ?, 'NOVEL', 'ONGOING', 'UNCONSUMED', ?, ?)
                """,
                id, title, originalTitle, testFictionGenreId, authorId, description, review);
        return id;
    }

    private long createFilm(String title, String originalTitle, String description, String review) {
        long id = insertVaultEntry(VaultEntryType.FILM, null);
        jdbcTemplate.update(
                """
                INSERT INTO films (id, title, original_title, format, production_style, progress_status, consumption_status, description, review)
                VALUES (?, ?, ?, 'MOVIE', 'LIVE_ACTION', 'ONGOING', 'UNCONSUMED', ?, ?)
                """,
                id, title, originalTitle, description, review);
        return id;
    }

    private long createFilmCredit(long filmId, String characterName, String note) {
        long personId = createPerson("Film Actor", null);
        long id = insertVaultEntry(VaultEntryType.FILM_CREDIT, null);
        jdbcTemplate.update(
                "INSERT INTO film_credits (id, film_id, person_id, role, character_name, note) VALUES (?, ?, ?, 'MAIN', ?, ?)",
                id, filmId, personId, characterName, note);
        return id;
    }

    private long createAlbum(String title, String description) {
        long id = insertVaultEntry(VaultEntryType.ALBUM, null);
        jdbcTemplate.update("INSERT INTO albums (id, title, description) VALUES (?, ?, ?)", id, title, description);
        return id;
    }

    private long createImage(String title, String locationText) {
        long id = insertVaultEntry(VaultEntryType.IMAGE, null);
        jdbcTemplate.update(
                "INSERT INTO images (id, title, location_text, object_key) VALUES (?, ?, ?, ?)",
                id, title, locationText, "keys/" + id + ".png");
        return id;
    }

    private long createBrand(String name, String description, String review) {
        long id = insertVaultEntry(VaultEntryType.BRAND, null);
        jdbcTemplate.update("INSERT INTO brands (id, name, description, review) VALUES (?, ?, ?, ?)", id, name, description, review);
        return id;
    }

    private long createLocation(String name, String description, String review) {
        long id = insertVaultEntry(VaultEntryType.LOCATION, null);
        jdbcTemplate.update(
                "INSERT INTO locations (id, name, address_id, description, review) VALUES (?, ?, ?, ?, ?)",
                id, name, testAddressId, description, review);
        return id;
    }

    private long createStudyItem(String title, String description, String review) {
        long id = insertVaultEntry(VaultEntryType.STUDY, null);
        jdbcTemplate.update(
                "INSERT INTO study_items (id, title, type, description, review) VALUES (?, ?, 'BOOK', ?, ?)",
                id, title, description, review);
        return id;
    }

    private long createInformationItem(String title, String description) {
        long id = insertVaultEntry(VaultEntryType.INFORMATION, null);
        jdbcTemplate.update(
                "INSERT INTO information_items (id, title, type, description) VALUES (?, ?, 'TECHNOLOGY', ?)",
                id, title, description);
        return id;
    }

    private long createVocabularyItem(String word, String pronunciation, String meaning) {
        long id = insertVaultEntry(VaultEntryType.VOCABULARY, null);
        jdbcTemplate.update(
                "INSERT INTO vocabulary_items (id, word, language_code, pronunciation, meaning) VALUES (?, ?, 'en', ?, ?)",
                id, word, pronunciation, meaning);
        return id;
    }

    private long createNote(String title, String summary) {
        long id = insertVaultEntry(VaultEntryType.NOTE, null);
        jdbcTemplate.update(
                "INSERT INTO notes (id, title, content_markdown, summary) VALUES (?, ?, ?, ?)",
                id, title, "# " + title + "\nBody text", summary);
        return id;
    }

    private long createMusicTrack(String title) {
        long id = insertVaultEntry(VaultEntryType.MUSIC, null);
        jdbcTemplate.update("INSERT INTO music_tracks (id, title) VALUES (?, ?)", id, title);
        return id;
    }

    private long createShoppingItem(String name, String description) {
        long id = insertVaultEntry(VaultEntryType.SHOPPING, null);
        jdbcTemplate.update("INSERT INTO shopping_items (id, name, description) VALUES (?, ?, ?)", id, name, description);
        return id;
    }

    private long createSoftwareItem(String name, String description, String review) {
        long id = insertVaultEntry(VaultEntryType.SOFTWARE, null);
        jdbcTemplate.update(
                "INSERT INTO software_items (id, name, type, description, review) VALUES (?, ?, 'APPLICATION', ?, ?)",
                id, name, description, review);
        return id;
    }

    private long createExternalAccount(String displayName, String username, String ownerName, String notes) {
        long id = insertVaultEntry(VaultEntryType.EXTERNAL_ACCOUNT, null);
        jdbcTemplate.update(
                """
                INSERT INTO external_accounts (id, platform_id, ownership, account_type, display_name, username, owner_name, notes)
                VALUES (?, ?, 'OWNED', 'SOCIAL', ?, ?, ?, ?)
                """,
                id, testPlatformId, displayName, username, ownerName, notes);
        return id;
    }

    private long createSavedResource(String title, String sourceName, String author, String summary) {
        long id = insertVaultEntry(VaultEntryType.SAVED_RESOURCE, null);
        jdbcTemplate.update(
                """
                INSERT INTO saved_resources (id, kind, title, source_name, author, summary, resource_url, resource_url_hash)
                VALUES (?, 'ARTICLE', ?, ?, ?, ?, ?, ?)
                """,
                id, title, sourceName, author, summary, "https://example.com/res/" + id, "hash_" + id);
        return id;
    }

    @Test
    @DisplayName("Searches all 17 Vault-backed types across all 9 domains successfully")
    void searchesAll17VaultBackedTypes() {
        createPerson("Isaac Asimov", "Science fiction author");
        createFiction("Foundation Novel", "Original Foundation", "Classic sci-fi", "Outstanding");
        long filmId = createFilm("Blade Runner", "Do Androids Dream", "Dystopian film", "Masterpiece");
        createFilmCredit(filmId, "Rick Deckard", "Blade runner detective");
        createAlbum("Holiday Photos", "Summer holiday trip");
        createImage("Sunset Beach", "Da Nang Coastline");
        createBrand("Sony Electronics", "Global tech brand", "Great hardware");
        createLocation("Highlands Coffee", "Popular coffee chain", "Good espresso");
        createStudyItem("Quantum Mechanics", "Introductory physics syllabus", "Challenging course");
        createInformationItem("PostgreSQL Trigram Indexing", "GIN indexes on text columns");
        createVocabularyItem("Serendipity", "se-ren-di-pi-ty", "Fortunate occurrence by chance");
        createNote("Meeting Notes", "Quarterly goals and review");
        createMusicTrack("Bohemian Rhapsody");
        createShoppingItem("Ergonomic Keyboard", "Split wireless mechanical keyboard");
        createSoftwareItem("IntelliJ IDEA", "Java IDE by JetBrains", "Indispensable tool");
        createExternalAccount("GitHub Profile", "devuser", "Jane Doe", "Primary dev account");
        createSavedResource("Clean Architecture Article", "Uncle Bob Blog", "Robert Martin", "Software design overview");

        // Search each individual type with a unique keyword
        assertThat(searchOperations.search(new GlobalSearchQuery("Isaac", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> {
                    assertThat(r.entryType()).isEqualTo(VaultEntryType.PERSON);
                    assertThat(r.domain()).isEqualTo(SearchDomain.PEOPLE);
                });

        assertThat(searchOperations.search(new GlobalSearchQuery("Foundation", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> {
                    assertThat(r.entryType()).isEqualTo(VaultEntryType.FICTION);
                    assertThat(r.domain()).isEqualTo(SearchDomain.FICTION);
                });

        assertThat(searchOperations.search(new GlobalSearchQuery("Runner", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .extracting(GlobalSearchResult::entryType)
                .contains(VaultEntryType.FILM, VaultEntryType.FILM_CREDIT);

        assertThat(searchOperations.search(new GlobalSearchQuery("Holiday", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.ALBUM));

        assertThat(searchOperations.search(new GlobalSearchQuery("Sunset", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.IMAGE));

        assertThat(searchOperations.search(new GlobalSearchQuery("Sony", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.BRAND));

        assertThat(searchOperations.search(new GlobalSearchQuery("Highlands", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.LOCATION));

        assertThat(searchOperations.search(new GlobalSearchQuery("Quantum", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.STUDY));

        assertThat(searchOperations.search(new GlobalSearchQuery("PostgreSQL", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.INFORMATION));

        assertThat(searchOperations.search(new GlobalSearchQuery("Serendipity", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.VOCABULARY));

        assertThat(searchOperations.search(new GlobalSearchQuery("Meeting", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.NOTE));

        assertThat(searchOperations.search(new GlobalSearchQuery("Bohemian", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.MUSIC));

        assertThat(searchOperations.search(new GlobalSearchQuery("Ergonomic", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.SHOPPING));

        assertThat(searchOperations.search(new GlobalSearchQuery("IntelliJ", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.SOFTWARE));

        assertThat(searchOperations.search(new GlobalSearchQuery("GitHub", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.EXTERNAL_ACCOUNT));

        assertThat(searchOperations.search(new GlobalSearchQuery("Clean Architecture", Set.of(), Set.of(), Set.of(), 0, 10)).items())
                .hasSize(1).first().satisfies(r -> assertThat(r.entryType()).isEqualTo(VaultEntryType.SAVED_RESOURCE));
    }

    @Test
    @DisplayName("Verifies ranking rubric order: 600 > 550 > 500 > 450 > 400 > 350 > 300 > 200")
    void verifiesRankingRubricOrder() {
        String query = "Alpha";

        // 600: PRIMARY_EXACT
        long id600 = createPerson("Alpha", "Random notes");

        // 550: PRIMARY_PREFIX
        long id550 = createPerson("Alpha Beta", "Random notes");

        // 500: PRIMARY_SUBSTRING
        long id500 = createPerson("The Alpha Story", "Random notes");

        // 450: SECONDARY_EXACT / SECONDARY_PREFIX
        long id450 = createFiction("Some Book", "Alpha Original", "Desc", "Rev");

        // 400: SECONDARY_SUBSTRING
        long id400 = createFiction("Another Book", "The Alpha Novel", "Desc", "Rev");

        // 350: SHORT_FUZZY (trigram similarity >= 0.30)
        long id350 = createPerson("Alphx", "Random notes");

        // 300: TAG match
        long id300 = createPerson("Zeta Person", "Unrelated notes");
        long tagId = insertTag("Alpha");
        attachTag(id300, tagId);

        // 200: BODY substring match
        long id200 = createPerson("Omega Person", "Here is an Alpha inside the body text");

        GlobalSearchPage page = searchOperations.search(new GlobalSearchQuery(query, Set.of(), Set.of(), Set.of(), 0, 10));
        List<GlobalSearchResult> results = page.items();

        assertThat(results).extracting(GlobalSearchResult::vaultEntryId)
                .containsExactly(id600, id550, id500, id450, id400, id350, id300, id200);

        assertThat(results).extracting(GlobalSearchResult::matchKind)
                .containsExactly(
                        SearchMatchKind.PRIMARY_EXACT,
                        SearchMatchKind.PRIMARY_PREFIX,
                        SearchMatchKind.PRIMARY_SUBSTRING,
                        SearchMatchKind.SECONDARY_PREFIX,
                        SearchMatchKind.SECONDARY_SUBSTRING,
                        SearchMatchKind.SHORT_FUZZY,
                        SearchMatchKind.TAG,
                        SearchMatchKind.BODY
                );
    }

    @Test
    @DisplayName("Verifies fuzzy matching threshold >= 0.30 and suppression for query length < 3")
    void verifiesFuzzyMatchingThresholdAndShortQuerySuppression() {
        // Query length >= 3:
        // "Elephant" vs "Elephnt" has high trigram similarity (>= 0.30)
        // "Elephant" vs "X" has low similarity (< 0.30)
        long matchedId = createPerson("Elephnt", "Notes");
        long unmatchedId = createPerson("Completely Unrelated", "Notes");

        GlobalSearchPage page = searchOperations.search(new GlobalSearchQuery("Elephant", Set.of(), Set.of(), Set.of(), 0, 10));
        assertThat(page.items()).extracting(GlobalSearchResult::vaultEntryId).contains(matchedId);
        assertThat(page.items()).extracting(GlobalSearchResult::vaultEntryId).doesNotContain(unmatchedId);

        // Short query (< 3 chars): fuzzy matching suppressed
        // "Ab" query should NOT fuzzy match "Abcdefgh" unless substring/prefix matches
        long exactShort = createPerson("Ab", "Notes");
        long distantShort = createPerson("Ax", "Notes"); // fuzzy similar to Ab if enabled, but suppressed

        GlobalSearchPage shortPage = searchOperations.search(new GlobalSearchQuery("Ab", Set.of(), Set.of(), Set.of(), 0, 10));
        assertThat(shortPage.items()).extracting(GlobalSearchResult::vaultEntryId).contains(exactShort);
        assertThat(shortPage.items()).extracting(GlobalSearchResult::vaultEntryId).doesNotContain(distantShort);
    }

    @Test
    @DisplayName("Verifies literal wildcard escaping for %, _, and backslash")
    void verifiesLiteralWildcardEscaping() {
        long literalPercent = createPerson("100% Guaranteed", "Desc");
        long otherPercent = createPerson("100 Reasons For Something", "Desc");

        long literalUnderscore = createPerson("user_special_code", "Desc");
        long otherUnderscore = createPerson("user-special-code-profile", "Desc");

        long literalBackslash = createPerson("path\\system\\folder", "Desc");
        long otherBackslash = createPerson("path-system-folder-item", "Desc");

        // Search "100%" should only match literal "100% Guaranteed", not "100 Reasons For Something"
        GlobalSearchPage percentPage = searchOperations.search(new GlobalSearchQuery("100%", Set.of(), Set.of(), Set.of(), 0, 10));
        assertThat(percentPage.items()).extracting(GlobalSearchResult::vaultEntryId)
                .contains(literalPercent)
                .doesNotContain(otherPercent);

        // Search "user_" should only match "user_special_code", not "user-special-code-profile"
        GlobalSearchPage underscorePage = searchOperations.search(new GlobalSearchQuery("user_", Set.of(), Set.of(), Set.of(), 0, 10));
        assertThat(underscorePage.items()).extracting(GlobalSearchResult::vaultEntryId)
                .contains(literalUnderscore)
                .doesNotContain(otherUnderscore);

        // Search "path\" should only match "path\system\folder", not "path-system-folder-item"
        GlobalSearchPage backslashPage = searchOperations.search(new GlobalSearchQuery("path\\", Set.of(), Set.of(), Set.of(), 0, 10));
        assertThat(backslashPage.items()).extracting(GlobalSearchResult::vaultEntryId)
                .contains(literalBackslash)
                .doesNotContain(otherBackslash);
    }

    @Test
    @DisplayName("P12-1: More than K equal-score tag hits with reverse ID order and tie-breaking")
    void verifiesP121EqualScoreTagCandidatesOrdering() {
        long tagId = insertTag("P12TagTest");

        // Insert 60 persons with identical score tag match, inserted in reverse order
        for (int i = 60; i >= 1; i--) {
            long pId = createPerson("Candidate " + String.format("%03d", i), "No text match");
            attachTag(pId, tagId);
        }

        GlobalSearchPage page = searchOperations.search(new GlobalSearchQuery("P12TagTest", Set.of(), Set.of(), Set.of(), 0, 50));
        assertThat(page.items()).hasSize(50);
        assertThat(page.hasMore()).isTrue();

        // Tie breaking must be: rank_bucket DESC, similarity DESC, type_name ASC, vault_entry_id ASC
        for (int i = 0; i < page.items().size() - 1; i++) {
            GlobalSearchResult current = page.items().get(i);
            GlobalSearchResult next = page.items().get(i + 1);
            assertThat(current.vaultEntryId()).isLessThan(next.vaultEntryId());
        }
    }

    @Test
    @DisplayName("P12-1: Late qualifying required-tag candidates across bounded pages")
    void verifiesP121LateQualifyingRequiredTagCandidates() {
        long reqTagId = insertTag("RequiredTag");
        long otherTagId = insertTag("OtherTag");

        // Insert 50 candidates that match query text "CommonSearch" but DO NOT have RequiredTag
        for (int i = 1; i <= 50; i++) {
            createPerson("CommonSearch Person " + i, "Desc");
        }

        // Insert 5 candidates that match query text "CommonSearch" AND DO have RequiredTag
        long qual1 = createPerson("CommonSearch Qualified 1", "Desc");
        attachTag(qual1, reqTagId);
        long qual2 = createPerson("CommonSearch Qualified 2", "Desc");
        attachTag(qual2, reqTagId);

        // Search with query "CommonSearch" and requiredTagIds = {reqTagId}
        GlobalSearchPage page = searchOperations.search(new GlobalSearchQuery("CommonSearch", Set.of(), Set.of(), Set.of(reqTagId), 0, 10));

        // The 50 non-tagged candidates must be rejected, and the qualification loop must continue across pages to find the 2 qualified ones
        assertThat(page.items()).extracting(GlobalSearchResult::vaultEntryId)
                .containsExactlyInAnyOrder(qual1, qual2);
    }

    @Test
    @DisplayName("P12-1: Deduplication keeps stronger rank and prefers text over tag on exact tie")
    void verifiesP121DeduplicationAndOriginPreference() {
        long tagId = insertTag("SharedKeyword");

        // Person matches text at rank 500 (PRIMARY_SUBSTRING) and tag at rank 300 (TAG)
        long pId = createPerson("Title containing SharedKeyword", "Desc");
        attachTag(pId, tagId);

        GlobalSearchPage page = searchOperations.search(new GlobalSearchQuery("SharedKeyword", Set.of(), Set.of(), Set.of(), 0, 10));
        assertThat(page.items()).hasSize(1);
        GlobalSearchResult res = page.items().get(0);
        assertThat(res.vaultEntryId()).isEqualTo(pId);
        assertThat(res.matchKind()).isEqualTo(SearchMatchKind.PRIMARY_SUBSTRING); // Text wins over tag
    }

    @Test
    @DisplayName("P12-2: PostgreSQL ALBUM vs IMAGE native enum declaration order vs alphabetical name order")
    void verifiesP122NativeEnumVsAlphabeticalOrder() {
        // In PostgreSQL Schema v1:
        // CREATE TYPE vault_entry_type AS ENUM (..., 'IMAGE', 'ALBUM', ...);
        // Notice 'IMAGE' appears before 'ALBUM' in the enum declaration!
        // But in Java: VaultEntryType.ALBUM.name() ("ALBUM") comes before "IMAGE" alphabetically.
        long imageId = createImage("SharedMatch Title", "Location");
        long albumId = createAlbum("SharedMatch Title", "Desc");

        // Both match PRIMARY_EXACT (600), similarity = 0.0.
        // Alphabetical tie-break requires ALBUM before IMAGE!
        GlobalSearchPage page = searchOperations.search(new GlobalSearchQuery("SharedMatch Title", Set.of(), Set.of(), Set.of(), 0, 10));
        assertThat(page.items()).hasSize(2);
        assertThat(page.items().get(0).entryType()).isEqualTo(VaultEntryType.ALBUM);
        assertThat(page.items().get(1).entryType()).isEqualTo(VaultEntryType.IMAGE);
    }

    @Test
    @DisplayName("P12-2: More than K candidates ALBUM vs IMAGE proves pre-LIMIT SQL selection uses alphabetical type name")
    void verifiesP122AlbumVsImagePreLimitSqlSelection() {
        // Schema v1 native enum order has IMAGE before ALBUM.
        // We insert 5 Albums and 5 Images, all matching PRIMARY_EXACT ("TieBreaker").
        // Query limit = 5, offset = 0.
        // Pre-LIMIT SQL must sort by type_name ASC (collated C), selecting all 5 Albums!
        for (int i = 1; i <= 5; i++) {
            createImage("TieBreaker", "Location " + i);
            createAlbum("TieBreaker", "Desc " + i);
        }

        GlobalSearchPage page1 = searchOperations.search(new GlobalSearchQuery("TieBreaker", Set.of(SearchDomain.MEDIA), Set.of(), Set.of(), 0, 5));
        assertThat(page1.items()).hasSize(5);
        assertThat(page1.items()).allMatch(item -> item.entryType() == VaultEntryType.ALBUM);
        assertThat(page1.hasMore()).isTrue();

        GlobalSearchPage page2 = searchOperations.search(new GlobalSearchQuery("TieBreaker", Set.of(SearchDomain.MEDIA), Set.of(), Set.of(), 5, 5));
        assertThat(page2.items()).hasSize(5);
        assertThat(page2.items()).allMatch(item -> item.entryType() == VaultEntryType.IMAGE);
        assertThat(page2.hasMore()).isFalse();
    }

    @Test
    @DisplayName("P12-2: Tag-only ALBUM vs IMAGE limiting inside Vault orders by alphabetical type name")
    void verifiesP122TagOnlyAlbumVsImagePreLimitSqlLimiting() {
        long tagId = insertTag("TagTieLimit");
        for (int i = 1; i <= 5; i++) {
            long imgId = createImage("Img " + i, "Location");
            long albId = createAlbum("Alb " + i, "Desc");
            attachTag(imgId, tagId);
            attachTag(albId, tagId);
        }

        GlobalSearchPage page1 = searchOperations.search(new GlobalSearchQuery("TagTieLimit", Set.of(SearchDomain.MEDIA), Set.of(), Set.of(), 0, 5));
        assertThat(page1.items()).hasSize(5);
        assertThat(page1.items()).allMatch(item -> item.entryType() == VaultEntryType.ALBUM);
        assertThat(page1.hasMore()).isTrue();

        GlobalSearchPage page2 = searchOperations.search(new GlobalSearchQuery("TagTieLimit", Set.of(SearchDomain.MEDIA), Set.of(), Set.of(), 5, 5));
        assertThat(page2.items()).hasSize(5);
        assertThat(page2.items()).allMatch(item -> item.entryType() == VaultEntryType.IMAGE);
        assertThat(page2.hasMore()).isFalse();
    }

    @Test
    @DisplayName("P12-1: Multiple matching tags on a single entry are collapsed before limiting")
    void verifiesP121MultipleMatchingTagsCollapsedBeforeLimiting() {
        long tagExact = insertTag("multi_rock");
        long tagClassic = insertTag("multi_rock_classic");
        long tagModern = insertTag("multi_rock_modern");

        long pMulti = createPerson("MultiTag Person", "Notes");
        attachTag(pMulti, tagExact);
        attachTag(pMulti, tagClassic);
        attachTag(pMulti, tagModern);

        long pSecond = createPerson("Second Person", "Notes");
        attachTag(pSecond, insertTag("multi_rock_second"));

        long pThird = createPerson("Third Person", "Notes");
        attachTag(pThird, insertTag("multi_rock_third"));

        // Direct Vault tag candidate query with source limit 2:
        // If pMulti's 3 tags were NOT collapsed before LIMIT 2, pMulti would occupy multiple rows,
        // crowding out pSecond completely!
        List<VaultTagCandidateHit> tagHits = vaultSearchOperations.searchByTag(
                new VaultTagSearchQuery("multi_rock", Set.of(VaultEntryType.PERSON), Set.of(), 2));
        assertThat(tagHits).hasSize(2);
        assertThat(tagHits.get(0).vaultEntryId()).isEqualTo(pMulti);
        assertThat(tagHits.get(0).similarity()).isEqualTo(1.0); // Best tag similarity from multi_rock
        assertThat(tagHits.get(1).vaultEntryId()).isIn(pSecond, pThird);
        assertThat(tagHits).extracting(VaultTagCandidateHit::vaultEntryId).doesNotHaveDuplicates();

        // Global search with limit 1, offset 0 -> selects pMulti (exact ID and similarity 1.0, hasMore = true)
        GlobalSearchPage page1 = searchOperations.search(new GlobalSearchQuery("multi_rock", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(), 0, 1));
        assertThat(page1.items()).hasSize(1);
        assertThat(page1.items().get(0).vaultEntryId()).isEqualTo(pMulti);
        assertThat(page1.items().get(0).similarity()).isEqualTo(1.0);
        assertThat(page1.hasMore()).isTrue();

        // Global search page 2 with limit 1, offset 1 -> selects one of the other non-pMulti entries
        GlobalSearchPage page2 = searchOperations.search(new GlobalSearchQuery("multi_rock", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(), 1, 1));
        assertThat(page2.items()).hasSize(1);
        assertThat(page2.items().get(0).vaultEntryId()).isIn(pSecond, pThird);
        assertThat(page2.items().get(0).vaultEntryId()).isNotEqualTo(pMulti);
    }

    @Test
    @DisplayName("P12-1: Early tag source required-tag rejection continues across pages")
    void verifiesP121EarlyTagSourceRequiredTagRejection() {
        long searchTagId = insertTag("TagCandidate");
        long requiredTagId = insertTag("TagRequired");

        for (int i = 1; i <= 50; i++) {
            long p = createPerson("EarlyReject Person " + i, "Notes");
            attachTag(p, searchTagId);
        }

        long qual1 = createPerson("EarlyQual Person 1", "Notes");
        attachTag(qual1, searchTagId);
        attachTag(qual1, requiredTagId);

        long qual2 = createPerson("EarlyQual Person 2", "Notes");
        attachTag(qual2, searchTagId);
        attachTag(qual2, requiredTagId);

        GlobalSearchPage page = searchOperations.search(new GlobalSearchQuery("TagCandidate", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(requiredTagId), 0, 10));
        assertThat(page.items()).extracting(GlobalSearchResult::vaultEntryId)
                .containsExactlyInAnyOrder(qual1, qual2);
    }

    @Test
    @DisplayName("P12-1: Multi-tag AND filtering with two explicit required tags; FILM_CREDIT excluded from tag and feature search")
    void verifiesP121MultiTagAndFilteringAndFilmCreditExclusion() {
        long tagBeta = insertTag("BetaTagUnique");
        long tagGamma = insertTag("GammaTagUnique");
        long tagAlpha = insertTag("AlphaTagUnique");

        // 1. Feature-text search with two explicit required tags (tagBeta AND tagGamma):
        long pBoth = createPerson("DualRequiredHero", "Notes");
        attachTag(pBoth, tagBeta);
        attachTag(pBoth, tagGamma);

        long pBetaOnly = createPerson("DualRequiredHero BetaOnly", "Notes");
        attachTag(pBetaOnly, tagBeta);

        long pGammaOnly = createPerson("DualRequiredHero GammaOnly", "Notes");
        attachTag(pGammaOnly, tagGamma);

        long pNeither = createPerson("DualRequiredHero Neither", "Notes");

        // Film Credit matching text "DualRequiredHero" with BOTH required tags -> must still be excluded
        long filmId = createFilm("DualRequired Film", null, "Desc", "Rev");
        long filmCreditBoth = createFilmCredit(filmId, "DualRequiredHero", "Credit Notes");
        attachTag(filmCreditBoth, tagBeta);
        attachTag(filmCreditBoth, tagGamma);

        GlobalSearchPage textPage = searchOperations.search(new GlobalSearchQuery(
                "DualRequiredHero", Set.of(), Set.of(), Set.of(tagBeta, tagGamma), 0, 10));
        assertThat(textPage.items()).extracting(GlobalSearchResult::vaultEntryId)
                .contains(pBoth)
                .doesNotContain(pBetaOnly, pGammaOnly, pNeither, filmCreditBoth);

        // 2. Tag-origin search with two explicit required tags:
        // Query term "AlphaTagUnique" with required tags tagBeta AND tagGamma
        long pTagBoth = createPerson("Person TagAlpha Both", "Notes");
        attachTag(pTagBoth, tagAlpha);
        attachTag(pTagBoth, tagBeta);
        attachTag(pTagBoth, tagGamma);

        long pTagBetaOnly = createPerson("Person TagAlpha BetaOnly", "Notes");
        attachTag(pTagBetaOnly, tagAlpha);
        attachTag(pTagBetaOnly, tagBeta);

        long filmCreditTagBoth = createFilmCredit(filmId, "Credit TagAlpha", "Credit Notes");
        attachTag(filmCreditTagBoth, tagAlpha);
        attachTag(filmCreditTagBoth, tagBeta);
        attachTag(filmCreditTagBoth, tagGamma);

        GlobalSearchPage tagPage = searchOperations.search(new GlobalSearchQuery(
                "AlphaTagUnique", Set.of(), Set.of(), Set.of(tagBeta, tagGamma), 0, 10));
        assertThat(tagPage.items()).extracting(GlobalSearchResult::vaultEntryId)
                .contains(pTagBoth)
                .doesNotContain(pTagBetaOnly, filmCreditTagBoth);
    }

    @Test
    @DisplayName("P12-1: Cross-domain equal-score exact-ID membership across multiple offsets")
    void verifiesCrossDomainEqualScoreExactIdMembershipAcrossOffsets() {
        long albId = createAlbum("CrossDomainExactTie", "Desc");
        long fId = createFiction("CrossDomainExactTie", null, "Desc", "Rev");
        long filmId = createFilm("CrossDomainExactTie", null, "Desc", "Rev");
        long locId = createLocation("CrossDomainExactTie", "Desc", "Rev");
        long pId = createPerson("CrossDomainExactTie", "Notes");

        // Deterministic order:
        // Rank = 600 (all exact title/name match)
        // Similarity = 0.0 (exact match)
        // Type name ASC: ALBUM, FICTION, FILM, LOCATION, PERSON
        // Expected exact ID oracle: [albId, fId, filmId, locId, pId]

        GlobalSearchPage page1 = searchOperations.search(
                new GlobalSearchQuery("CrossDomainExactTie", Set.of(), Set.of(), Set.of(), 0, 2));
        assertThat(page1.items()).extracting(GlobalSearchResult::vaultEntryId)
                .containsExactly(albId, fId);
        assertThat(page1.hasMore()).isTrue();

        GlobalSearchPage page2 = searchOperations.search(
                new GlobalSearchQuery("CrossDomainExactTie", Set.of(), Set.of(), Set.of(), 2, 2));
        assertThat(page2.items()).extracting(GlobalSearchResult::vaultEntryId)
                .containsExactly(filmId, locId);
        assertThat(page2.hasMore()).isTrue();

        GlobalSearchPage page3 = searchOperations.search(
                new GlobalSearchQuery("CrossDomainExactTie", Set.of(), Set.of(), Set.of(), 4, 2));
        assertThat(page3.items()).extracting(GlobalSearchResult::vaultEntryId)
                .containsExactly(pId);
        assertThat(page3.hasMore()).isFalse();

        GlobalSearchPage page4 = searchOperations.search(
                new GlobalSearchQuery("CrossDomainExactTie", Set.of(), Set.of(), Set.of(), 6, 2));
        assertThat(page4.items()).isEmpty();
        assertThat(page4.hasMore()).isFalse();
    }

    @Test
    @DisplayName("FR12-1: Truthful hasMore derivation across terminal, single, overlap, and max bound pages")
    void verifiesFr121TruthfulHasMoreAndExhaustion() {
        // Case 1: Exactly 1 result total with offset 0 / limit 1 -> hasMore must be false! (Probe check)
        createPerson("UniqueOneResult", "Notes");
        GlobalSearchPage singlePage = searchOperations.search(new GlobalSearchQuery("UniqueOneResult", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(), 0, 1));
        assertThat(singlePage.items()).hasSize(1);
        assertThat(singlePage.hasMore()).isFalse();

        // Case 2: Exactly 10 results total with limit 10, offset 0 -> hasMore must be false!
        for (int i = 1; i <= 10; i++) {
            createPerson("TenResultsGroup " + i, "Notes");
        }
        GlobalSearchPage tenPage = searchOperations.search(new GlobalSearchQuery("TenResultsGroup", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(), 0, 10));
        assertThat(tenPage.items()).hasSize(10);
        assertThat(tenPage.hasMore()).isFalse();

        // Case 3: Exactly 15 results total, page 2 with offset 10, limit 5 -> hasMore must be false!
        for (int i = 1; i <= 15; i++) {
            createPerson("FifteenGroup " + String.format("%02d", i), "Notes");
        }
        GlobalSearchPage fifteenPage = searchOperations.search(new GlobalSearchQuery("FifteenGroup", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(), 10, 5));
        assertThat(fifteenPage.items()).hasSize(5);
        assertThat(fifteenPage.hasMore()).isFalse();

        // Case 4: Overlapping text and tag source (same entry matches both) -> hasMore must be false if only 1 item
        long overlapTag = insertTag("OverlapSingle");
        long overlapPerson = createPerson("OverlapSingle", "Notes");
        attachTag(overlapPerson, overlapTag);
        GlobalSearchPage overlapPage = searchOperations.search(new GlobalSearchQuery("OverlapSingle", Set.of(), Set.of(), Set.of(), 0, 1));
        assertThat(overlapPage.items()).hasSize(1);
        assertThat(overlapPage.hasMore()).isFalse();

        // Case 5: 11 results with limit 10 -> hasMore must be true
        for (int i = 1; i <= 11; i++) {
            createPerson("ElevenGroup " + i, "Notes");
        }
        GlobalSearchPage elevenPage = searchOperations.search(new GlobalSearchQuery("ElevenGroup", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(), 0, 10));
        assertThat(elevenPage.items()).hasSize(10);
        assertThat(elevenPage.hasMore()).isTrue();

        // Case 6: Real maximum lookahead dataset with at least 601 qualifying rows (602 rows seeded)
        List<Long> vaultIds = jdbcTemplate.query(
                "INSERT INTO vault_entries (entry_type, created_at, updated_at, deleted_at) " +
                "SELECT 'MUSIC'::vault_entry_type, now(), now(), null FROM generate_series(1, 600) RETURNING id",
                (rs, rowNum) -> rs.getLong(1)
        );
        List<Object[]> musicParams = new ArrayList<>(600);
        for (int i = 0; i < 600; i++) {
            musicParams.add(new Object[]{vaultIds.get(i), "MaxLookahead Song " + String.format("%04d", i + 1)});
        }
        jdbcTemplate.batchUpdate("INSERT INTO music_tracks (id, title) VALUES (?, ?)", musicParams);

        // Terminal slice at maximum valid bounds: offset 500, limit 100 on 600 rows -> exactly 600 candidates returned, candidate count (600) not > K (600) -> hasMore is false!
        GlobalSearchPage termPage = searchOperations.search(new GlobalSearchQuery("MaxLookahead Song", Set.of(SearchDomain.COLLECTION), Set.of(), Set.of(), 500, 100));
        assertThat(termPage.items()).hasSize(100);
        assertThat(termPage.hasMore()).isFalse();
        assertThat(termPage.items().getFirst().primaryText()).isEqualTo("MaxLookahead Song 0501");
        assertThat(termPage.items().getLast().primaryText()).isEqualTo("MaxLookahead Song 0600");

        // Non-terminal slice at maximum valid bounds: add 2 more rows (602 total matching) -> candidate fetch capped at targetK=601, candidate count (601) > K (600) -> hasMore is true!
        List<Long> extraVaultIds = jdbcTemplate.query(
                "INSERT INTO vault_entries (entry_type, created_at, updated_at, deleted_at) " +
                "SELECT 'MUSIC'::vault_entry_type, now(), now(), null FROM generate_series(1, 2) RETURNING id",
                (rs, rowNum) -> rs.getLong(1)
        );
        jdbcTemplate.update("INSERT INTO music_tracks (id, title) VALUES (?, 'MaxLookahead Song 0601'), (?, 'MaxLookahead Song 0602')",
                extraVaultIds.get(0), extraVaultIds.get(1));

        GlobalSearchPage maxPage = searchOperations.search(new GlobalSearchQuery("MaxLookahead Song", Set.of(SearchDomain.COLLECTION), Set.of(), Set.of(), 500, 100));
        assertThat(maxPage.items()).hasSize(100);
        assertThat(maxPage.hasMore()).isTrue();
        assertThat(maxPage.items().getFirst().primaryText()).isEqualTo("MaxLookahead Song 0501");
        assertThat(maxPage.items().getLast().primaryText()).isEqualTo("MaxLookahead Song 0600");
    }

    @Test
    @DisplayName("FR12-2: Structural validation rejects invalid bounds before database execution across all layers")
    void verifiesFr122StructuralValidationRejection() {
        // GlobalSearchQuery validation
        assertThatThrownBy(() -> new GlobalSearchQuery(null, Set.of(), Set.of(), Set.of(), 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GlobalSearchQuery("   ", Set.of(), Set.of(), Set.of(), 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GlobalSearchQuery("a".repeat(201), Set.of(), Set.of(), Set.of(), 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GlobalSearchQuery("valid", Set.of(), Set.of(), Set.of(-1L), 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GlobalSearchQuery("valid", Set.of(), Set.of(), Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L), 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GlobalSearchQuery("valid", Set.of(), Set.of(), Set.of(), -1, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GlobalSearchQuery("valid", Set.of(), Set.of(), Set.of(), 501, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GlobalSearchQuery("valid", Set.of(), Set.of(), Set.of(), 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GlobalSearchQuery("valid", Set.of(), Set.of(), Set.of(), 0, 101))
                .isInstanceOf(IllegalArgumentException.class);

        // Owner queries validation
        assertThatThrownBy(() -> new PeopleSearchQuery("a".repeat(201), Set.of(), Set.of(), 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PeopleSearchQuery("valid", Set.of(), Set.of(-1L), 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PeopleSearchQuery("valid", Set.of(), Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L), 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PeopleSearchQuery("valid", Set.of(), Set.of(), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PeopleSearchQuery("valid", Set.of(), Set.of(), 602))
                .isInstanceOf(IllegalArgumentException.class);

        // Nested primitive search methods validation
        assertThatThrownBy(() -> studySearchOperations.search("term", Set.of(), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> studySearchOperations.search("term", Set.of(), 602))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> musicSearchOperations.search("term", Set.of(), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> musicSearchOperations.search("term", Set.of(), 602))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> shoppingSearchOperations.search("term", Set.of(), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> shoppingSearchOperations.search("term", Set.of(), 602))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> softwareSearchOperations.search("term", Set.of(), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> softwareSearchOperations.search("term", Set.of(), 602))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> noteSearchOperations.search("term", Set.of(), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> noteSearchOperations.search("term", Set.of(), 602))
                .isInstanceOf(IllegalArgumentException.class);

        // Owner service lookupDocuments boundary validation
        assertThatThrownBy(() -> peopleSearchOperations.lookupDocuments(Set.of(-1L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> peopleSearchOperations.lookupDocuments(Set.of(0L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(peopleSearchOperations.lookupDocuments(null)).isEmpty();
        assertThat(peopleSearchOperations.lookupDocuments(Set.of())).isEmpty();

        // Oversized batch (602 IDs) and null element rejection
        Set<Long> oversizedBatch = LongStream.rangeClosed(1, 602).boxed().collect(Collectors.toSet());
        assertThatThrownBy(() -> peopleSearchOperations.lookupDocuments(oversizedBatch))
                .isInstanceOf(IllegalArgumentException.class);
        Set<Long> nullElementBatch = Collections.singleton(null);
        assertThatThrownBy(() -> peopleSearchOperations.lookupDocuments(nullElementBatch))
                .isInstanceOf(IllegalArgumentException.class);

        // Valid maximum batch (601 IDs) passes without exception
        Set<Long> maxValidBatch = LongStream.rangeClosed(1, 601).boxed().collect(Collectors.toSet());
        assertThat(peopleSearchOperations.lookupDocuments(maxValidBatch)).isNotNull();

        // Vault qualification boundary validation
        assertThatThrownBy(() -> vaultSearchOperations.filterQualifyingActiveEntries(Set.of(-1L), Set.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultSearchOperations.filterQualifyingActiveEntries(Set.of(1L), Set.of(-1L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultSearchOperations.filterQualifyingActiveEntries(Set.of(1L), Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L)))
                .isInstanceOf(IllegalArgumentException.class);
        // Invalid tags on empty candidate set: rejected before DB execution
        assertThatThrownBy(() -> vaultSearchOperations.filterQualifyingActiveEntries(Set.of(), Set.of(-1L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultSearchOperations.filterQualifyingActiveEntries(Set.of(), Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultSearchOperations.filterQualifyingActiveEntries(oversizedBatch, Set.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> vaultSearchOperations.filterQualifyingActiveEntries(nullElementBatch, Set.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(vaultSearchOperations.filterQualifyingActiveEntries(maxValidBatch, Set.of())).isNotNull();
    }

    @Test
    @DisplayName("FR12-3: Snippets extract matching body field, strip markdown/HTML, cap ellipses within 240 chars, and preserve Unicode")
    void verifiesFr123SnippetPlaintextAndEllipsesCap() {
        // 1. Later body field matching: Note summary does not match, but content_markdown matches
        long noteId = insertVaultEntry(VaultEntryType.NOTE, null);
        String markdownWithMarkup = "# Title Header\nThis is an unrelated opening. <div class=\"secret\">"
                + "Inside the HTML and **bold markdown** lies the secretneedle keyword that is deeply nested.</div>"
                + " [Documentation](https://example.com) with `code` block. " + "A".repeat(300);
        jdbcTemplate.update("INSERT INTO notes (id, title, content_markdown, summary) VALUES (?, 'Note Title', ?, 'Unrelated summary that does not contain search term')",
                noteId, markdownWithMarkup);

        GlobalSearchPage page = searchOperations.search(new GlobalSearchQuery("secretneedle", Set.of(SearchDomain.KNOWLEDGE), Set.of(), Set.of(), 0, 10));
        assertThat(page.items()).hasSize(1);
        GlobalSearchResult result = page.items().get(0);
        assertThat(result.snippet()).isNotNull();
        assertThat(result.snippet()).contains("secretneedle");
        assertThat(result.snippet()).doesNotContain("Unrelated summary");
        assertThat(result.snippet()).doesNotContain("#").doesNotContain("<div").doesNotContain("</div>").doesNotContain("**");
        assertThat(result.snippet().length()).isLessThanOrEqualTo(240);

        // 2. Near-end two-ellipsis probe fixture: "A".repeat(377) + "needle" + "B".repeat(117)
        // Strictly bounds total snippet length <= 240 chars and asserts stored Markdown is unchanged
        long probeNoteId = insertVaultEntry(VaultEntryType.NOTE, null);
        String probeContent = "A".repeat(377) + "probbeneedle" + "B".repeat(117);
        jdbcTemplate.update("INSERT INTO notes (id, title, content_markdown, summary) VALUES (?, 'Probe Note', ?, 'Summary')",
                probeNoteId, probeContent);

        GlobalSearchPage probePage = searchOperations.search(new GlobalSearchQuery("probbeneedle", Set.of(SearchDomain.KNOWLEDGE), Set.of(), Set.of(), 0, 10));
        assertThat(probePage.items()).hasSize(1);
        GlobalSearchResult probeResult = probePage.items().get(0);
        assertThat(probeResult.snippet()).isNotNull();
        assertThat(probeResult.snippet().length()).isLessThanOrEqualTo(240);
        assertThat(probeResult.snippet()).startsWith("...");
        assertThat(probeResult.snippet()).contains("probbeneedle");

        // Two-ellipsis fixture where needle is deeply surrounded on both sides by 300 characters
        long twoEllipsisNoteId = insertVaultEntry(VaultEntryType.NOTE, null);
        String twoEllipsisContent = "A".repeat(300) + "twoellipsisneedle" + "B".repeat(300);
        jdbcTemplate.update("INSERT INTO notes (id, title, content_markdown, summary) VALUES (?, 'Unrelated Title', ?, 'Summary')",
                twoEllipsisNoteId, twoEllipsisContent);

        GlobalSearchPage twoPage = searchOperations.search(new GlobalSearchQuery("twoellipsisneedle", Set.of(SearchDomain.KNOWLEDGE), Set.of(), Set.of(), 0, 10));
        assertThat(twoPage.items()).hasSize(1);
        GlobalSearchResult twoResult = twoPage.items().get(0);
        assertThat(twoResult.snippet()).isNotNull();
        assertThat(twoResult.snippet().length()).isLessThanOrEqualTo(240);
        assertThat(twoResult.snippet()).startsWith("...").endsWith("...");
        assertThat(twoResult.snippet()).contains("twoellipsisneedle");

        // Assert original stored Markdown in database table notes remains untouched
        String storedMarkdown = jdbcTemplate.queryForObject("SELECT content_markdown FROM notes WHERE id = ?", String.class, probeNoteId);
        assertThat(storedMarkdown).isEqualTo(probeContent);

        // 3. Supplementary Unicode code points (emoji surrogate pairs) at snippet boundaries
        long emojiNoteId = insertVaultEntry(VaultEntryType.NOTE, null);
        String emojiContent = "Intro prefix " + "\uD83D\uDE00".repeat(40) + " surrogateneedle " + "\uD83D\uDE00".repeat(40) + " end suffix";
        jdbcTemplate.update("INSERT INTO notes (id, title, content_markdown, summary) VALUES (?, 'Emoji Note', ?, 'Summary')",
                emojiNoteId, emojiContent);

        GlobalSearchPage emojiPage = searchOperations.search(new GlobalSearchQuery("surrogateneedle", Set.of(SearchDomain.KNOWLEDGE), Set.of(), Set.of(), 0, 10));
        assertThat(emojiPage.items()).hasSize(1);
        String emojiSnippet = emojiPage.items().get(0).snippet();
        assertThat(emojiSnippet).isNotNull();
        assertThat(emojiSnippet.length()).isLessThanOrEqualTo(240);
        assertThat(emojiSnippet).contains("surrogateneedle");
        // Verify no unpaired surrogates exist in snippet
        for (int i = 0; i < emojiSnippet.length(); i++) {
            char c = emojiSnippet.charAt(i);
            if (Character.isHighSurrogate(c)) {
                assertThat(i + 1).isLessThan(emojiSnippet.length());
                assertThat(Character.isLowSurrogate(emojiSnippet.charAt(i + 1))).isTrue();
                i++;
            } else {
                assertThat(Character.isLowSurrogate(c)).isFalse();
            }
        }

        // 4. Literal punctuation in identifiers: invoice_2026 and account_id must preserve underscores
        long literalNoteId = insertVaultEntry(VaultEntryType.NOTE, null);
        jdbcTemplate.update("INSERT INTO notes (id, title, content_markdown, summary) VALUES (?, 'Billing Note', 'Processed invoice_2026 for account_id 98765 successfully.', 'Summary')",
                literalNoteId);

        GlobalSearchPage literalPage = searchOperations.search(new GlobalSearchQuery("invoice_2026", Set.of(SearchDomain.KNOWLEDGE), Set.of(), Set.of(), 0, 10));
        assertThat(literalPage.items()).hasSize(1);
        assertThat(literalPage.items().get(0).snippet()).contains("invoice_2026");

        // 5. Unicode BMP text preservation
        long uniNoteId = insertVaultEntry(VaultEntryType.NOTE, null);
        jdbcTemplate.update("INSERT INTO notes (id, title, content_markdown, summary) VALUES (?, 'Unicode Note', 'Nội dung tìm kiếm bằng tiếng Việt có dấu rất chuẩn xác.', 'Summary')",
                uniNoteId);
        GlobalSearchPage uniPage = searchOperations.search(new GlobalSearchQuery("tiếng Việt", Set.of(SearchDomain.KNOWLEDGE), Set.of(), Set.of(), 0, 10));
        assertThat(uniPage.items()).hasSize(1);
        assertThat(uniPage.items().get(0).snippet()).contains("tiếng Việt");
    }

    @Test
    @DisplayName("FR12-4: Flyway V2 migration index inventory contains all 23 required GIN trigram indexes with valid definitions")
    void verifiesFr124V2MigrationIndexInventory() {
        List<String> extensions = jdbcTemplate.queryForList(
                "SELECT extname FROM pg_extension WHERE extname = 'pg_trgm'", String.class);
        assertThat(extensions).containsExactly("pg_trgm");

        List<Map<String, Object>> indexRows = jdbcTemplate.queryForList("""
                SELECT indexname, indexdef FROM pg_indexes
                WHERE schemaname = 'public' AND indexname LIKE 'idx_%_trgm'
                ORDER BY indexname
                """);

        assertThat(indexRows).hasSize(23);
        List<String> indexNames = indexRows.stream().map(r -> (String) r.get("indexname")).toList();
        assertThat(indexNames).containsExactlyInAnyOrder(
                "idx_tags_name_trgm",
                "idx_persons_name_trgm",
                "idx_fictions_title_trgm",
                "idx_fictions_original_title_trgm",
                "idx_films_title_trgm",
                "idx_films_original_title_trgm",
                "idx_film_credits_character_name_trgm",
                "idx_albums_title_trgm",
                "idx_images_title_trgm",
                "idx_images_location_text_trgm",
                "idx_brands_name_trgm",
                "idx_locations_name_trgm",
                "idx_study_items_title_trgm",
                "idx_information_items_title_trgm",
                "idx_vocabulary_items_word_trgm",
                "idx_notes_title_trgm",
                "idx_music_tracks_title_trgm",
                "idx_shopping_items_name_trgm",
                "idx_software_items_name_trgm",
                "idx_external_accounts_display_name_trgm",
                "idx_external_accounts_username_trgm",
                "idx_external_accounts_owner_name_trgm",
                "idx_saved_resources_title_trgm"
        );

        record ExpectedIndex(String tableName, String columnName) {}
        Map<String, ExpectedIndex> expectedIndexes = Map.ofEntries(
                Map.entry("idx_tags_name_trgm", new ExpectedIndex("tags", "name")),
                Map.entry("idx_persons_name_trgm", new ExpectedIndex("persons", "name")),
                Map.entry("idx_fictions_title_trgm", new ExpectedIndex("fictions", "title")),
                Map.entry("idx_fictions_original_title_trgm", new ExpectedIndex("fictions", "original_title")),
                Map.entry("idx_films_title_trgm", new ExpectedIndex("films", "title")),
                Map.entry("idx_films_original_title_trgm", new ExpectedIndex("films", "original_title")),
                Map.entry("idx_film_credits_character_name_trgm", new ExpectedIndex("film_credits", "character_name")),
                Map.entry("idx_albums_title_trgm", new ExpectedIndex("albums", "title")),
                Map.entry("idx_images_title_trgm", new ExpectedIndex("images", "title")),
                Map.entry("idx_images_location_text_trgm", new ExpectedIndex("images", "location_text")),
                Map.entry("idx_brands_name_trgm", new ExpectedIndex("brands", "name")),
                Map.entry("idx_locations_name_trgm", new ExpectedIndex("locations", "name")),
                Map.entry("idx_study_items_title_trgm", new ExpectedIndex("study_items", "title")),
                Map.entry("idx_information_items_title_trgm", new ExpectedIndex("information_items", "title")),
                Map.entry("idx_vocabulary_items_word_trgm", new ExpectedIndex("vocabulary_items", "word")),
                Map.entry("idx_notes_title_trgm", new ExpectedIndex("notes", "title")),
                Map.entry("idx_music_tracks_title_trgm", new ExpectedIndex("music_tracks", "title")),
                Map.entry("idx_shopping_items_name_trgm", new ExpectedIndex("shopping_items", "name")),
                Map.entry("idx_software_items_name_trgm", new ExpectedIndex("software_items", "name")),
                Map.entry("idx_external_accounts_display_name_trgm", new ExpectedIndex("external_accounts", "display_name")),
                Map.entry("idx_external_accounts_username_trgm", new ExpectedIndex("external_accounts", "username")),
                Map.entry("idx_external_accounts_owner_name_trgm", new ExpectedIndex("external_accounts", "owner_name")),
                Map.entry("idx_saved_resources_title_trgm", new ExpectedIndex("saved_resources", "title"))
        );

        // Verify all 23 indexes map to exact table, column, lower expression, and gin_trgm_ops
        for (Map<String, Object> row : indexRows) {
            String indexName = (String) row.get("indexname");
            String indexDef = (String) row.get("indexdef");
            ExpectedIndex expected = expectedIndexes.get(indexName);
            assertThat(expected).as("Unexpected index name: " + indexName).isNotNull();

            assertThat(indexDef).as("Index %s on table %s", indexName, expected.tableName())
                    .contains("ON public." + expected.tableName());
            assertThat(indexDef).as("Index %s USING gin", indexName)
                    .contains("USING gin");
            assertThat(indexDef).as("Index %s lower(%s)", indexName, expected.columnName())
                    .contains("lower(").contains(expected.columnName());
            assertThat(indexDef).as("Index %s gin_trgm_ops", indexName)
                    .contains("gin_trgm_ops");
        }
    }

    @Test
    @DisplayName("FR12-4: Transaction-bound actual query plans, threshold isolation, and unindexed body scan limitations")
    void verifiesFr124ActualQueryPlansAndTransactionThresholdIsolation() throws Exception {
        // 1. Seed realistic synthetic rows (150 tracks, 50 tagged entries, 50 notes) and ANALYZE
        for (int i = 1; i <= 150; i++) {
            long tId = insertVaultEntry(VaultEntryType.MUSIC, null);
            jdbcTemplate.update("INSERT INTO music_tracks (id, title) VALUES (?, ?)",
                    tId, "Synthetic Symphony No " + i);
        }
        long symTag = insertTag("symphonictag");
        for (int i = 1; i <= 50; i++) {
            long pId = createPerson("Synthetic Tagged " + i, "Notes");
            attachTag(pId, symTag);
        }
        for (int i = 1; i <= 50; i++) {
            long nId = insertVaultEntry(VaultEntryType.NOTE, null);
            jdbcTemplate.update("INSERT INTO notes (id, title, content_markdown, summary) VALUES (?, ?, ?, ?)",
                    nId, "Synthetic Note Title " + i, "Long body text discussing symphonic movement " + i, "Summary " + i);
        }

        jdbcTemplate.execute("ANALYZE music_tracks; ANALYZE tags; ANALYZE vault_entry_tags; ANALYZE vault_entries; ANALYZE notes;");

        // 2. Capture emitted production SQL and parameters from MusicSearchService and VaultSearchService
        java.util.concurrent.atomic.AtomicReference<String> capturedMusicSql = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<org.springframework.jdbc.core.namedparam.SqlParameterSource> capturedMusicParams = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<String> capturedVaultSql = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<org.springframework.jdbc.core.namedparam.SqlParameterSource> capturedVaultParams = new java.util.concurrent.atomic.AtomicReference<>();

        org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate capturingJdbc =
                new org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate(dataSource) {
            @Override
            public <T> List<T> query(String sql, org.springframework.jdbc.core.namedparam.SqlParameterSource paramSource, org.springframework.jdbc.core.RowMapper<T> rowMapper) {
                if (sql.contains("FROM music_tracks m")) {
                    capturedMusicSql.set(sql);
                    capturedMusicParams.set(paramSource);
                } else if (sql.contains("FROM tags t")) {
                    capturedVaultSql.set(sql);
                    capturedVaultParams.set(paramSource);
                }
                return super.query(sql, paramSource, rowMapper);
            }
        };

        com.vhvkhangg.personalprivatevault.collection.music.internal.application.search.MusicSearchService capturingMusicService =
                new com.vhvkhangg.personalprivatevault.collection.music.internal.application.search.MusicSearchService(capturingJdbc, vaultSearchOperations);
        com.vhvkhangg.personalprivatevault.vault.internal.application.search.VaultSearchService capturingVaultService =
                new com.vhvkhangg.personalprivatevault.vault.internal.application.search.VaultSearchService(capturingJdbc);

        capturingMusicService.search("symphony", Set.of(), 50);
        capturingVaultService.searchByTag(new VaultTagSearchQuery("symphonictag", Set.of(VaultEntryType.PERSON), Set.of(symTag), 50));

        assertThat(capturedMusicSql.get()).as("Captured production Music SQL").isNotNull();
        assertThat(capturedMusicParams.get()).as("Captured production Music parameters").isNotNull();
        assertThat(capturedVaultSql.get()).as("Captured production Vault SQL").isNotNull();
        assertThat(capturedVaultParams.get()).as("Captured production Vault parameters").isNotNull();

        // 3. Dedicated connection: EXPLAIN captured production SQL under transaction and test capability
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET LOCAL pg_trgm.similarity_threshold = 0.3;");
                stmt.execute("SET enable_seqscan = off;");
            }

            NamedParameterJdbcTemplate connJdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));

            // A. Explain captured production Music query
            List<String> musicPlan = connJdbc.query("EXPLAIN (COSTS OFF) " + capturedMusicSql.get(), capturedMusicParams.get(), (rs, rn) -> rs.getString(1));
            String musicPlanStr = String.join("\n", musicPlan);
            assertThat(musicPlanStr).contains("idx_music_tracks_title_trgm");
            assertThat(musicPlanStr).contains("Bitmap Index Scan");

            // B. Explain captured production Vault tag candidate query with required tags subquery
            List<String> vaultPlan = connJdbc.query("EXPLAIN (COSTS OFF) " + capturedVaultSql.get(), capturedVaultParams.get(), (rs, rn) -> rs.getString(1));
            String vaultPlanStr = String.join("\n", vaultPlan);
            assertThat(vaultPlanStr).contains("tags");
            assertThat(vaultPlanStr).contains("Filter:");

            // C. Direct tags GIN trigram index utilization
            List<String> tagTrgmPlan = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "EXPLAIN (COSTS OFF) SELECT t.id FROM tags t WHERE (lower(t.name) LIKE ? OR (lower(t.name) % ? AND similarity(lower(t.name), ?) >= 0.30))")) {
                ps.setString(1, "%symphonictag%");
                ps.setString(2, "symphonictag");
                ps.setString(3, "symphonictag");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tagTrgmPlan.add(rs.getString(1));
                    }
                }
            }
            String tagTrgmPlanStr = String.join("\n", tagTrgmPlan);
            assertThat(tagTrgmPlanStr).contains("idx_tags_name_trgm");
            assertThat(tagTrgmPlanStr).contains("Bitmap Index Scan");

            // D. Unindexed body search limitation: content_markdown has no GIN index
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET enable_seqscan = on;");
            }
            List<String> bodyPlan = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "EXPLAIN (COSTS OFF) SELECT n.id FROM notes n WHERE (lower(n.content_markdown) LIKE ? OR lower(n.summary) LIKE ?)")) {
                ps.setString(1, "%symphonic%");
                ps.setString(2, "%symphonic%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        bodyPlan.add(rs.getString(1));
                    }
                }
            }
            String bodyPlanStr = String.join("\n", bodyPlan);
            assertThat(bodyPlanStr).contains("Seq Scan on notes");

            conn.rollback();
            conn.setAutoCommit(true);
        }

        // 4. Prove >= 0.30 fuzzy eligibility from a differing prior session threshold (0.8) and restoration after transaction
        long testTrackId = insertVaultEntry(VaultEntryType.MUSIC, null);
        jdbcTemplate.update("INSERT INTO music_tracks (id, title) VALUES (?, ?)", testTrackId, "Symphony");

        try (Connection conn = dataSource.getConnection()) {
            try {
                // A. Set session threshold to 0.8 on this physical connection
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SET pg_trgm.similarity_threshold = 0.8;");
                }

                long pinnedConnPid;
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT pg_backend_pid()")) {
                    rs.next();
                    pinnedConnPid = rs.getLong(1);
                }

                // B. Measure exact similarity of "symphoni" vs "Symphony" and prove fixture is fuzzy-only
                assertThat("symphony".startsWith("symphoni")).isFalse();
                assertThat("symphony".contains("symphoni")).isFalse();
                double measuredSimilarity;
                try (PreparedStatement ps = conn.prepareStatement("SELECT similarity(lower(title), 'symphoni') FROM music_tracks WHERE id = ?")) {
                    ps.setLong(1, testTrackId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        measuredSimilarity = rs.getDouble(1);
                    }
                }
                assertThat(measuredSimilarity)
                        .as("Measured similarity is in [0.30, 0.80)")
                        .isGreaterThanOrEqualTo(0.30)
                        .isLessThan(0.80);

                // C. Prove exclusion at prior session threshold 0.8 without service override
                try (PreparedStatement ps = conn.prepareStatement("SELECT lower(title) % 'symphoni' FROM music_tracks WHERE id = ?")) {
                    ps.setLong(1, testTrackId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        boolean matchesAt08 = rs.getBoolean(1);
                        assertThat(matchesAt08).as("Fixture is excluded at prior threshold 0.8").isFalse();
                    }
                }

                // D. Pin this exact physical connection into a transaction-aware SingleConnectionDataSource
                SingleConnectionDataSource pinnedDataSource = new SingleConnectionDataSource(conn, true);
                java.util.concurrent.atomic.AtomicLong serviceObservedPid = new java.util.concurrent.atomic.AtomicLong();
                NamedParameterJdbcTemplate pinnedJdbc = new NamedParameterJdbcTemplate(pinnedDataSource) {
                    @Override
                    public <T> List<T> query(String sql, org.springframework.jdbc.core.namedparam.SqlParameterSource paramSource, org.springframework.jdbc.core.RowMapper<T> rowMapper) {
                        Long pid = getJdbcOperations().queryForObject("SELECT pg_backend_pid()", Long.class);
                        if (pid != null) {
                            serviceObservedPid.set(pid);
                        }
                        return super.query(sql, paramSource, rowMapper);
                    }
                };

                // Create real transaction proxy for MusicSearchService using DataSourceTransactionManager on the pinned connection
                org.springframework.aop.framework.ProxyFactory pf = new org.springframework.aop.framework.ProxyFactory();
                pf.setTarget(new com.vhvkhangg.personalprivatevault.collection.music.internal.application.search.MusicSearchService(
                        pinnedJdbc, vaultSearchOperations));
                pf.setInterfaces(MusicSearchOperations.class);
                pf.addAdvice(new org.springframework.transaction.interceptor.TransactionInterceptor(
                        new org.springframework.jdbc.datasource.DataSourceTransactionManager(pinnedDataSource),
                        new org.springframework.transaction.annotation.AnnotationTransactionAttributeSource()
                ));
                MusicSearchOperations proxiedMusicOps = (MusicSearchOperations) pf.getProxy();

                // E. Real proxied service transaction executes on the pinned connection
                var hits = proxiedMusicOps.search("symphoni", Set.of(), 10);

                // Assert connection identity: the SQL query executed on the EXACT SAME physical connection!
                assertThat(serviceObservedPid.get())
                        .as("Observed service query pg_backend_pid matches pinned connection PID")
                        .isEqualTo(pinnedConnPid);

                // Proves the fixture is returned via the service's SET LOCAL pg_trgm.similarity_threshold = 0.3 override
                assertThat(hits).extracting(com.vhvkhangg.personalprivatevault.collection.music.search.MusicSearchHit::vaultEntryId)
                        .contains(testTrackId);
                var hit = hits.stream().filter(h -> h.vaultEntryId() == testTrackId).findFirst().orElseThrow();
                assertThat(hit.matchKind()).isEqualTo("SHORT_FUZZY");
                assertThat(hit.similarity()).isCloseTo(measuredSimilarity, org.assertj.core.data.Offset.offset(0.001));

                // F. Prove that upon transaction completion, session threshold is verified restored to 0.8
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SHOW pg_trgm.similarity_threshold")) {
                    rs.next();
                    assertThat(rs.getString(1))
                            .as("Session threshold restored to prior 0.8 after transaction completion")
                            .isEqualTo("0.8");
                }
            } finally {
                // G. Always restore connection session threshold back to baseline 0.3 in finally
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SET pg_trgm.similarity_threshold = 0.3;");
                }
            }
        }
    }

    @Test
    @DisplayName("FR12-5: Query-count and batch instrumentation proves bounded lookups and absence of per-hit calls")
    void verifiesFr125QueryCountInstrumentationAndBoundedBatches() {
        class CountingNamedParameterJdbcTemplate extends org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate {
            final AtomicInteger tagQueryCount = new AtomicInteger(0);
            final AtomicInteger candidateQueryCount = new AtomicInteger(0);
            final AtomicInteger qualificationQueryCount = new AtomicInteger(0);
            final AtomicInteger materializationQueryCount = new AtomicInteger(0);
            final List<Integer> qualificationBatchSizes = new java.util.concurrent.CopyOnWriteArrayList<>();
            final List<Integer> materializationBatchSizes = new java.util.concurrent.CopyOnWriteArrayList<>();

            public CountingNamedParameterJdbcTemplate(DataSource dataSource) {
                super(dataSource);
            }

            @Override
            public <T> List<T> query(String sql, org.springframework.jdbc.core.namedparam.SqlParameterSource paramSource, org.springframework.jdbc.core.RowMapper<T> rowMapper) {
                if (sql.contains("FROM tags t")) {
                    tagQueryCount.incrementAndGet();
                } else if (sql.contains("FROM persons p") && sql.contains("LIMIT :limit")) {
                    candidateQueryCount.incrementAndGet();
                } else if (sql.contains("FROM vault_entries ve") && sql.contains("vet.tag_id IN")) {
                    qualificationQueryCount.incrementAndGet();
                    if (paramSource.hasValue("ids")) {
                        Object ids = paramSource.getValue("ids");
                        if (ids instanceof java.util.Collection<?> col) {
                            qualificationBatchSizes.add(col.size());
                        }
                    }
                } else if (sql.contains("FROM persons p") && sql.contains("IN (:ids)")) {
                    materializationQueryCount.incrementAndGet();
                    if (paramSource.hasValue("ids")) {
                        Object ids = paramSource.getValue("ids");
                        if (ids instanceof java.util.Collection<?> col) {
                            materializationBatchSizes.add(col.size());
                        }
                    }
                }
                return super.query(sql, paramSource, rowMapper);
            }

            public void reset() {
                tagQueryCount.set(0);
                candidateQueryCount.set(0);
                qualificationQueryCount.set(0);
                materializationQueryCount.set(0);
                qualificationBatchSizes.clear();
                materializationBatchSizes.clear();
            }
        }

        CountingNamedParameterJdbcTemplate countingJdbc = new CountingNamedParameterJdbcTemplate(dataSource);
        com.vhvkhangg.personalprivatevault.vault.internal.application.search.VaultSearchService instrumentedVaultOps =
                new com.vhvkhangg.personalprivatevault.vault.internal.application.search.VaultSearchService(countingJdbc);
        com.vhvkhangg.personalprivatevault.people.internal.application.search.PeopleSearchService instrumentedPeopleOps =
                new com.vhvkhangg.personalprivatevault.people.internal.application.search.PeopleSearchService(countingJdbc, instrumentedVaultOps);

        GlobalSearchService instrumentedService = new GlobalSearchService(
                instrumentedVaultOps,
                instrumentedPeopleOps,
                fictionSearchOperations,
                filmSearchOperations,
                mediaSearchOperations,
                locationSearchOperations,
                knowledgeSearchOperations,
                collectionSearchOperations,
                accountSearchOperations,
                feedSearchOperations
        );

        // Case 1: Exactly 1 tagged result in database -> 1 tag query, 1 bulk lookup with size 1
        long singleTag = insertTag("quantum_tag_unique");
        long singlePerson = createPerson("Alice Smith", "Notes");
        attachTag(singlePerson, singleTag);

        countingJdbc.reset();
        GlobalSearchPage pageSingle = instrumentedService.search(new GlobalSearchQuery(
                "quantum_tag_unique", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(), 0, 10));
        assertThat(pageSingle.items()).hasSize(1);
        assertThat(countingJdbc.tagQueryCount.get()).isEqualTo(1);
        assertThat(countingJdbc.materializationQueryCount.get()).isEqualTo(1);
        assertThat(countingJdbc.materializationBatchSizes).containsExactly(1);

        // Case 2: 20 tagged results in database, requested limit 10 -> 1 tag query, 1 bulk lookup bounded to 10
        long twentyTag = insertTag("astrophysics_tag_unique");
        for (int i = 1; i <= 20; i++) {
            long p = createPerson("Bob Citizen " + String.format("%02d", i), "Notes");
            attachTag(p, twentyTag);
        }
        countingJdbc.reset();
        GlobalSearchPage pageTwenty = instrumentedService.search(new GlobalSearchQuery(
                "astrophysics_tag_unique", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(), 0, 10));
        assertThat(pageTwenty.items()).hasSize(10);
        assertThat(pageTwenty.hasMore()).isTrue();

        // Exactly 1 tag search query and exactly 1 bulk lookup query (NOT 10 per-hit calls!)
        assertThat(countingJdbc.tagQueryCount.get()).isEqualTo(1);
        assertThat(countingJdbc.materializationQueryCount.get()).isEqualTo(1);
        // Materialization batch size is strictly bounded to the 10 selected items, NOT all 20 candidates in DB
        assertThat(countingJdbc.materializationBatchSizes).containsExactly(10);

        // Case 3: Feature text search with 20 results in database -> exactly 1 domain candidate query
        for (int i = 1; i <= 20; i++) {
            createPerson("TwentyTextPerson " + String.format("%02d", i), "Notes");
        }
        countingJdbc.reset();
        GlobalSearchPage pageText = instrumentedService.search(new GlobalSearchQuery(
                "TwentyTextPerson", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(), 0, 10));
        assertThat(pageText.items()).hasSize(10);
        assertThat(countingJdbc.candidateQueryCount.get()).isEqualTo(1);
        // Domain text search already projects primary_text in candidate query; 0 redundant materialization calls
        assertThat(countingJdbc.materializationQueryCount.get()).isEqualTo(0);
        assertThat(countingJdbc.materializationBatchSizes).isEmpty();

        // Case 4: Late-qualifying dataset exceeding candidate page size (50)
        // 50 non-qualifying candidates precede 1 qualifying candidate (Person 51)
        long reqTag = insertTag("InstrumentedReqTag");
        for (int i = 1; i <= 50; i++) {
            createPerson("LateQualCandidate " + String.format("%02d", i), "Notes");
        }
        long qualPerson = createPerson("LateQualCandidate 51", "Notes");
        attachTag(qualPerson, reqTag);
        for (int i = 52; i <= 55; i++) {
            createPerson("LateQualCandidate " + String.format("%02d", i), "Notes");
        }

        countingJdbc.reset();
        GlobalSearchPage pageQual = instrumentedService.search(new GlobalSearchQuery(
                "LateQualCandidate", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(reqTag), 0, 1));
        assertThat(pageQual.items()).extracting(GlobalSearchResult::vaultEntryId).containsExactly(qualPerson);
        // Paging continued past page size 50: exactly 2 candidate SQL queries
        assertThat(countingJdbc.candidateQueryCount.get()).isEqualTo(2);
        // Exactly 2 batch qualification SQL queries to Vault (NOT 55 per-ID queries!)
        assertThat(countingJdbc.qualificationQueryCount.get()).isEqualTo(2);
        assertThat(countingJdbc.qualificationBatchSizes).containsExactly(50, 5);
        // Domain text candidate already has primary_text; 0 materialization queries
        assertThat(countingJdbc.materializationQueryCount.get()).isEqualTo(0);

        // Case 5: Mixed result containing 1 text-origin candidate and 1 tag-origin candidate
        long mixedTag = insertTag("MixedTagTarget");
        long textPerson = createPerson("MixedMatch Person", "Notes");
        long tagPerson = createPerson("TagOnly Person", "Notes");
        attachTag(tagPerson, mixedTag);

        countingJdbc.reset();
        GlobalSearchPage mixedPage = instrumentedService.search(new GlobalSearchQuery(
                "MixedMatch", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(), 0, 10));
        assertThat(mixedPage.items()).hasSize(1);
        // Text search returned 1 hit (already projected) -> 0 materialization calls
        assertThat(countingJdbc.materializationQueryCount.get()).isEqualTo(0);

        countingJdbc.reset();
        GlobalSearchPage mixedTagPage = instrumentedService.search(new GlobalSearchQuery(
                "MixedTagTarget", Set.of(SearchDomain.PEOPLE), Set.of(), Set.of(), 0, 10));
        assertThat(mixedTagPage.items()).hasSize(1);
        // Tag-only search returned 1 hit needing text -> exactly 1 materialization call with batch size 1
        assertThat(countingJdbc.materializationQueryCount.get()).isEqualTo(1);
        assertThat(countingJdbc.materializationBatchSizes).containsExactly(1);
    }

    @Test
    @DisplayName("Recycle bin exclusion: soft-deleted entries are excluded from text and tag search")
    void verifiesRecycleBinExclusion() {
        // Active person
        long activeId = createPerson("RecycleCandidate Active", "Active notes");

        // Trashed person (deleted_at IS NOT NULL)
        long trashedVaultId = insertVaultEntry(VaultEntryType.PERSON, Instant.now());
        jdbcTemplate.update("INSERT INTO persons (id, name, notes) VALUES (?, 'RecycleCandidate Trashed', 'Notes')", trashedVaultId);

        // Tagged active and trashed
        long tagId = insertTag("TrashTagTest");
        attachTag(activeId, tagId);
        attachTag(trashedVaultId, tagId);

        // Text search
        GlobalSearchPage textPage = searchOperations.search(new GlobalSearchQuery("RecycleCandidate", Set.of(), Set.of(), Set.of(), 0, 10));
        assertThat(textPage.items()).extracting(GlobalSearchResult::vaultEntryId)
                .contains(activeId)
                .doesNotContain(trashedVaultId);

        // Tag search
        GlobalSearchPage tagPage = searchOperations.search(new GlobalSearchQuery("TrashTagTest", Set.of(), Set.of(), Set.of(), 0, 10));
        assertThat(tagPage.items()).extracting(GlobalSearchResult::vaultEntryId)
                .contains(activeId)
                .doesNotContain(trashedVaultId);
    }

    @Test
    @DisplayName("Pagination: respects offset, limit, and determinable hasMore")
    void verifiesPagination() {
        for (int i = 1; i <= 25; i++) {
            createPerson(String.format("PagingCandidate %02d", i), "Notes");
        }

        // Page 1: limit 10, offset 0 -> 10 results, hasMore = true
        GlobalSearchPage page1 = searchOperations.search(new GlobalSearchQuery("PagingCandidate", Set.of(), Set.of(), Set.of(), 0, 10));
        assertThat(page1.items()).hasSize(10);
        assertThat(page1.offset()).isEqualTo(0);
        assertThat(page1.limit()).isEqualTo(10);
        assertThat(page1.hasMore()).isTrue();

        // Page 2: limit 10, offset 10 -> 10 results, hasMore = true
        GlobalSearchPage page2 = searchOperations.search(new GlobalSearchQuery("PagingCandidate", Set.of(), Set.of(), Set.of(), 10, 10));
        assertThat(page2.items()).hasSize(10);
        assertThat(page2.offset()).isEqualTo(10);
        assertThat(page2.limit()).isEqualTo(10);
        assertThat(page2.hasMore()).isTrue();

        // Page 3: limit 10, offset 20 -> 5 results, hasMore = false
        GlobalSearchPage page3 = searchOperations.search(new GlobalSearchQuery("PagingCandidate", Set.of(), Set.of(), Set.of(), 20, 10));
        assertThat(page3.items()).hasSize(5);
        assertThat(page3.offset()).isEqualTo(20);
        assertThat(page3.limit()).isEqualTo(10);
        assertThat(page3.hasMore()).isFalse();

        // No overlap between page 1, 2, 3
        assertThat(page1.items()).extracting(GlobalSearchResult::vaultEntryId)
                .doesNotContainAnyElementsOf(page2.items().stream().map(GlobalSearchResult::vaultEntryId).toList());
        assertThat(page2.items()).extracting(GlobalSearchResult::vaultEntryId)
                .doesNotContainAnyElementsOf(page3.items().stream().map(GlobalSearchResult::vaultEntryId).toList());
    }

    @Test
    @DisplayName("Domain and type filter intersection: disjoint filters return empty page")
    void verifiesFilterIntersection() {
        createPerson("Disjoint Test Person", "Notes");
        createFiction("Disjoint Test Fiction", null, "Desc", "Rev");

        // Domain PEOPLE, but requestedEntryTypes FICTION -> intersection is empty
        GlobalSearchPage page = searchOperations.search(new GlobalSearchQuery(
                "Disjoint",
                Set.of(SearchDomain.PEOPLE),
                Set.of(VaultEntryType.FICTION),
                Set.of(),
                0,
                10
        ));

        assertThat(page.items()).isEmpty();
        assertThat(page.hasMore()).isFalse();
    }

    @Test
    @DisplayName("Verifies GIN trigram indexes are active and usable via EXPLAIN")
    void verifiesGinTrigramIndexPlan() {
        jdbcTemplate.execute("SET enable_seqscan = off");
        try {
            List<String> planLines = jdbcTemplate.queryForList(
                    "EXPLAIN SELECT id FROM persons WHERE lower(name) LIKE '%candidate%'",
                    String.class);
            String fullPlan = String.join("\n", planLines);
            assertThat(fullPlan).contains("idx_persons_name_trgm");
        } finally {
            jdbcTemplate.execute("SET enable_seqscan = on");
        }
    }
}
