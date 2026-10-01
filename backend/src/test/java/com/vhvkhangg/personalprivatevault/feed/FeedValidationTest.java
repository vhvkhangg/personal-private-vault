package com.vhvkhangg.personalprivatevault.feed;

import com.vhvkhangg.personalprivatevault.feed.enums.FeedSourceType;
import com.vhvkhangg.personalprivatevault.feed.enums.SavedResourceKind;
import com.vhvkhangg.personalprivatevault.feed.internal.application.FeedSourceService;
import com.vhvkhangg.personalprivatevault.feed.internal.infrastructure.persistence.FeedSourceRepository;
import com.vhvkhangg.personalprivatevault.feed.item.command.NormalizedFeedItemInput;
import com.vhvkhangg.personalprivatevault.feed.resource.command.CreateManualSavedResourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.command.CreateFeedSourceCommand;
import com.vhvkhangg.personalprivatevault.feed.source.exception.InvalidFeedSourceException;
import com.vhvkhangg.personalprivatevault.feed.view.FeedJsonSnapshot;
import com.vhvkhangg.personalprivatevault.feed.view.InvalidFeedJsonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class FeedValidationTest {

    private FeedSourceRepository feedSourceRepository;
    private FeedSourceService feedSourceService;

    @BeforeEach
    void setUp() {
        feedSourceRepository = mock(FeedSourceRepository.class);
        feedSourceService = new FeedSourceService(feedSourceRepository);
    }

    // =========================================================================
    // 1. FeedSource Command Validation
    // =========================================================================
    @Nested
    @DisplayName("FeedSource validation tests")
    class FeedSourceValidationTests {

        @Test
        @DisplayName("Rejects null or blank name")
        void rejectsBlankName() {
            assertThatThrownBy(() -> feedSourceService.createSource(new CreateFeedSourceCommand(
                    null, FeedSourceType.RSS, null, null, null, null, null, null
            )))
                    .isInstanceOf(InvalidFeedSourceException.class)
                    .hasMessageContaining("name must not be blank");

            assertThatThrownBy(() -> feedSourceService.createSource(new CreateFeedSourceCommand(
                    "   ", FeedSourceType.RSS, null, null, null, null, null, null
            )))
                    .isInstanceOf(InvalidFeedSourceException.class)
                    .hasMessageContaining("name must not be blank");
        }

        @Test
        @DisplayName("Rejects name exceeding 255 chars")
        void rejectsOverlongName() {
            String longName = "a".repeat(256);
            assertThatThrownBy(() -> feedSourceService.createSource(new CreateFeedSourceCommand(
                    longName, FeedSourceType.RSS, null, null, null, null, null, null
            )))
                    .isInstanceOf(InvalidFeedSourceException.class)
                    .hasMessageContaining("name must not exceed 255 characters");
        }

        @Test
        @DisplayName("Rejects null type")
        void rejectsNullType() {
            assertThatThrownBy(() -> feedSourceService.createSource(new CreateFeedSourceCommand(
                    "Valid Name", null, null, null, null, null, null, null
            )))
                    .isInstanceOf(InvalidFeedSourceException.class)
                    .hasMessageContaining("type must not be null");
        }
    }

    // =========================================================================
    // 2. FeedItem Command Validation
    // =========================================================================
    @Nested
    @DisplayName("FeedItem validation tests")
    class FeedItemValidationTests {

        @Test
        @DisplayName("Constructs normalized feed item input defensively")
        void constructsNormalizedInputDefensively() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("views", 100);

            NormalizedFeedItemInput input = new NormalizedFeedItemInput(
                    "ext-1", "Title", "https://example.com", "Author", "Summary", null, metadata
            );

            metadata.put("views", 999);
            assertThat(input.rawMetadata()).containsEntry("views", 100);
        }
    }

    // =========================================================================
    // 3. SavedResource Command Validation
    // =========================================================================
    @Nested
    @DisplayName("SavedResource validation tests")
    class SavedResourceValidationTests {

        @Test
        @DisplayName("Constructs manual saved resource command defensively")
        void constructsManualResourceCommandDefensively() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("starred", true);

            CreateManualSavedResourceCommand cmd = new CreateManualSavedResourceCommand(
                    SavedResourceKind.ARTICLE, "Title", "https://example.com", null, null, null, null, null, null, metadata
            );

            metadata.put("starred", false);
            assertThat(cmd.rawMetadata()).containsEntry("starred", true);
        }
    }

    // =========================================================================
    // 4. FeedJsonSnapshot Validation
    // =========================================================================
    @Nested
    @DisplayName("FeedJsonSnapshot validation tests")
    class FeedJsonSnapshotValidationTests {

        @Test
        @DisplayName("Handles null and empty map gracefully")
        void handlesNullAndEmpty() {
            assertThat(FeedJsonSnapshot.toUnmodifiableSnapshot(null)).isNull();
            assertThat(FeedJsonSnapshot.toUnmodifiableSnapshot(Map.of())).isEmpty();
        }

        @Test
        @DisplayName("Round trips complex nested maps and lists with primitives")
        void roundTripsComplexStructure() {
            Map<String, Object> original = Map.of(
                    "str", "hello",
                    "num", 42,
                    "dec", 3.14,
                    "bool", true,
                    "list", List.of("a", 1, true),
                    "nested", Map.of("inner", "value")
            );

            Map<String, Object> snapshot = FeedJsonSnapshot.toUnmodifiableSnapshot(original);
            assertThat(snapshot).isEqualTo(original);
        }

        @Test
        @DisplayName("Rejects null keys")
        void rejectsNullKeys() {
            Map<String, Object> mapWithNullKey = new HashMap<>();
            mapWithNullKey.put(null, "val");

            assertThatThrownBy(() -> FeedJsonSnapshot.toUnmodifiableSnapshot(mapWithNullKey))
                    .isInstanceOf(InvalidFeedJsonException.class)
                    .hasMessageContaining("Feed JSON map contains null key");
        }

        @Test
        @DisplayName("Rejects unsupported object values")
        void rejectsUnsupportedValues() {
            Map<String, Object> mapWithObject = Map.of("bad", new Object());

            assertThatThrownBy(() -> FeedJsonSnapshot.toUnmodifiableSnapshot(mapWithObject))
                    .isInstanceOf(InvalidFeedJsonException.class)
                    .hasMessageContaining("Unsupported value type in feed JSON");
        }

        @Test
        @DisplayName("Rejects cyclic map structures")
        void rejectsCyclicStructures() {
            Map<String, Object> map = new HashMap<>();
            map.put("self", map);

            assertThatThrownBy(() -> FeedJsonSnapshot.toUnmodifiableSnapshot(map))
                    .isInstanceOf(InvalidFeedJsonException.class)
                    .hasMessageContaining("Cyclic reference detected");
        }
    }
}
