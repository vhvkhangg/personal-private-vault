package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagGenerationState;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagQueryEmbeddingSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalService;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagRetrievalRequestDto;
import com.vhvkhangg.personalprivatevault.rag.internal.web.dto.RagSourceSelectionEntryDto;
import com.vhvkhangg.personalprivatevault.rag.source.RagAccessState;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RagRetrievalSelectionTest {

    @Mock
    private RagSourceRepository ragSourceRepository;
    @Mock
    private RagExternalFileSourceRepository ragExternalFileSourceRepository;
    @Mock
    private RagIndexGenerationRepository ragIndexGenerationRepository;
    @Mock
    private RagDocumentChunkRepository ragDocumentChunkRepository;
    @Mock
    private RagRetrievalReaderRepository ragRetrievalReaderRepository;
    @Mock
    private RagRetrievalInspectionRepository ragRetrievalInspectionRepository;
    @Mock
    private RagDependencyRepository ragDependencyRepository;
    @Mock
    private RagGenerationState ragGenerationState;
    @Mock
    private RagRetrievalAuthority ragRetrievalAuthority;
    @Mock
    private RagQueryEmbeddingSupervisor ragQueryEmbeddingSupervisor;
    @Mock
    private RagOriginalBytesStore ragOriginalBytesStore;
    @Mock
    private RagRuntimeIdentity ragRuntimeIdentity;
    @Mock
    private com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalCurrentAuthority currentAuthority;

    private ObjectMapper objectMapper;
    private RagRetrievalService retrievalService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        lenient().when(ragRuntimeIdentity.instance()).thenReturn(UUID.randomUUID());
        retrievalService = new RagRetrievalService(
                ragSourceRepository,
                ragExternalFileSourceRepository,
                ragIndexGenerationRepository,
                ragDocumentChunkRepository,
                ragRetrievalReaderRepository,
                ragRetrievalInspectionRepository,
                ragDependencyRepository,
                ragRetrievalAuthority,
                ragQueryEmbeddingSupervisor,
                ragOriginalBytesStore,
                ragRuntimeIdentity,
                currentAuthority,
                true,
                true
        );
    }

    @Test
    void wireValidationCountsUnicodeCodePoints() {
        try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var allowed = new RagRetrievalRequestDto(UUID.randomUUID(), "😀".repeat(3000),
                    RagRetrievalMode.GENERIC, null, null, null, 5);
            assertThat(validator.validate(allowed)).isEmpty();
            var tooLong = new RagRetrievalRequestDto(UUID.randomUUID(), "😀".repeat(4097),
                    RagRetrievalMode.GENERIC, null, null, null, 5);
            assertThat(validator.validate(tooLong)).isNotEmpty();
        }
    }

    @Test
    void oversizedIngressUsesRootEnvelopeForKnownAndChunkedBodies() throws Exception {
        for (boolean knownLength : List.of(true, false)) {
            var request = new org.springframework.mock.web.MockHttpServletRequest("POST", "/api/v1/rag/retrievals");
            request.setContent(new byte[65_537]);
            jakarta.servlet.http.HttpServletRequest wrapped = knownLength ? request :
                    new jakarta.servlet.http.HttpServletRequestWrapper(request) {
                        @Override public long getContentLengthLong() { return -1; }
                        @Override public int getContentLength() { return -1; }
                    };
            var response = new org.springframework.mock.web.MockHttpServletResponse();
            new com.vhvkhangg.personalprivatevault.rag.internal.web.RagRetrievalIngressFilter().doFilter(wrapped,response,
                    (r,s) -> r.getInputStream().readAllBytes());
            assertThat(response.getStatus()).isEqualTo(400);
            var root = objectMapper.readTree(response.getContentAsByteArray());
            assertThat(root.path("error").path("code").asText()).isEqualTo("RAG_QUERY_SCOPE_LIMIT");
            assertThat(root.has("body")).isFalse();
            assertThat(root.path("data").isNull()).isTrue();
        }
    }

    @Test
    @DisplayName("Blank question is rejected with IllegalArgumentException")
    void blankQuestionRejected() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalCommand cmd = new RagRetrievalCommand(
                reqId,
                "   ",
                RagRetrievalMode.GENERIC,
                List.of(),
                null,
                null,
                5
        );

        assertThatThrownBy(() -> retrievalService.retrieve(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("question must not be blank");
    }

    @Test
    @DisplayName("Question exceeding 4096 code points is rejected")
    void oversizedQuestionRejected() {
        UUID reqId = UUID.randomUUID();
        String oversized = "a".repeat(4097);
        RagRetrievalCommand cmd = new RagRetrievalCommand(
                reqId,
                oversized,
                RagRetrievalMode.GENERIC,
                List.of(),
                null,
                null,
                5
        );

        assertThatThrownBy(() -> retrievalService.retrieve(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("question exceeds 4096 Unicode code points");
    }

    @Test
    @DisplayName("topK out of bounds (<1 or >10) is rejected")
    void invalidTopKRejected() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalCommand zeroTopK = new RagRetrievalCommand(
                reqId,
                "Valid question",
                RagRetrievalMode.GENERIC,
                List.of(),
                null,
                null,
                0
        );
        assertThatThrownBy(() -> retrievalService.retrieve(zeroTopK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("topK must be between 1 and 10");

        RagRetrievalCommand elevenTopK = new RagRetrievalCommand(
                reqId,
                "Valid question",
                RagRetrievalMode.GENERIC,
                List.of(),
                null,
                null,
                11
        );
        assertThatThrownBy(() -> retrievalService.retrieve(elevenTopK))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("topK must be between 1 and 10");
    }

    @Test
    @DisplayName("Ideaverse canon mode requires collectionId, rosterVersion, and non-empty roster")
    void ideaverseCanonMissingMetadataRejected() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalCommand noCollection = new RagRetrievalCommand(
                reqId,
                "Valid question",
                RagRetrievalMode.IDEAVERSE_CANON,
                List.of(new RagSourceSelectionEntry(1L, UUID.randomUUID(), "IDEAVERSE_CORE", "notes/doc.md")),
                "v1.0",
                null,
                5
        );
        assertThatThrownBy(() -> retrievalService.retrieve(noCollection))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ideaverse mode requires collectionId and rosterVersion");

        RagRetrievalCommand emptyRoster = new RagRetrievalCommand(
                reqId,
                "Valid question",
                RagRetrievalMode.IDEAVERSE_CANON,
                List.of(),
                "v1.0",
                UUID.randomUUID().toString(),
                5
        );
        assertThatThrownBy(() -> retrievalService.retrieve(emptyRoster))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ideaverse mode requires explicit selection roster");
    }

    @Test
    @DisplayName("Ideaverse canon mode rejects non-core entries")
    void ideaverseCanonNonCoreRejected() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalCommand nonCore = new RagRetrievalCommand(
                reqId,
                "Valid question",
                RagRetrievalMode.IDEAVERSE_CANON,
                List.of(new RagSourceSelectionEntry(1L, UUID.randomUUID(), "IDEAVERSE_NONCORE", "notes/doc.md")),
                "v1.0",
                UUID.randomUUID().toString(),
                5
        );
        assertThatThrownBy(() -> retrievalService.retrieve(nonCore))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Canon mode permits only IDEAVERSE_CORE entries");
    }

    @Test
    @DisplayName("Traversal or absolute display paths are rejected")
    void relativePathTraversalRejected() {
        UUID reqId = UUID.randomUUID();
        String collId = UUID.randomUUID().toString();

        RagRetrievalCommand dotDot = new RagRetrievalCommand(
                reqId,
                "Valid question",
                RagRetrievalMode.IDEAVERSE_CANON,
                List.of(new RagSourceSelectionEntry(1L, UUID.randomUUID(), "IDEAVERSE_CORE", "../secret.md")),
                "v1.0",
                collId,
                5
        );
        assertThatThrownBy(() -> retrievalService.retrieve(dotDot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("relative traversal-free path");

        RagRetrievalCommand leadingSlash = new RagRetrievalCommand(
                reqId,
                "Valid question",
                RagRetrievalMode.IDEAVERSE_CANON,
                List.of(new RagSourceSelectionEntry(1L, UUID.randomUUID(), "IDEAVERSE_CORE", "/etc/passwd")),
                "v1.0",
                collId,
                5
        );
        assertThatThrownBy(() -> retrievalService.retrieve(leadingSlash))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("relative traversal-free path");

        RagRetrievalCommand driveLetter = new RagRetrievalCommand(
                reqId,
                "Valid question",
                RagRetrievalMode.IDEAVERSE_CANON,
                List.of(new RagSourceSelectionEntry(1L, UUID.randomUUID(), "IDEAVERSE_CORE", "C:\\notes\\doc.md")),
                "v1.0",
                collId,
                5
        );
        assertThatThrownBy(() -> retrievalService.retrieve(driveLetter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("relative traversal-free path");
    }

    @Test
    @DisplayName("Duplicate source IDs in selection roster are rejected")
    void duplicateSourceIdsRejected() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalCommand dup = new RagRetrievalCommand(
                reqId,
                "Valid question",
                RagRetrievalMode.GENERIC,
                List.of(
                        new RagSourceSelectionEntry(1L, UUID.randomUUID(), null, null),
                        new RagSourceSelectionEntry(1L, UUID.randomUUID(), null, null)
                ),
                null,
                null,
                5
        );

        assertThatThrownBy(() -> retrievalService.retrieve(dup))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate source ID in selection roster");
    }

    @Test
    @DisplayName("Complete union exceeding 100 sources throws RagQueryScopeLimitException")
    void unionScopeExceedingBoundRejected() {
        UUID reqId = UUID.randomUUID();
        List<RagSourceSelectionEntry> overLimit = new ArrayList<>();
        for (long i = 1; i <= 101; i++) {
            overLimit.add(new RagSourceSelectionEntry(i, UUID.randomUUID(), null, null));
        }

        RagRetrievalCommand cmd = new RagRetrievalCommand(
                reqId,
                "Valid question",
                RagRetrievalMode.GENERIC,
                overLimit,
                null,
                null,
                5
        );

        // Mock each source
        String canonDigest = "0".repeat(64);
        when(ragGenerationState.state(any())).thenReturn("CURRENT");
        when(ragDependencyRepository.findByParentSourceId(any())).thenReturn(List.of());

        for (long i = 1; i <= 101; i++) {
            UUID fileSourceId = UUID.randomUUID();
            UUID revId = overLimit.get((int) i - 1).selectedRevisionId();

            RagSource src = mock(RagSource.class);
            when(src.getId()).thenReturn(i);
            when(src.getSourceType()).thenReturn(RagSourceType.EXTERNAL_FILE);
            when(src.getAccessState()).thenReturn(RagAccessState.APPROVED);
            when(src.getSourceId()).thenReturn(fileSourceId.toString());
            when(src.getFileIdentity()).thenReturn(canonDigest + "1".repeat(64));
            when(ragSourceRepository.findById(i)).thenReturn(Optional.of(src));

            RagExternalFileSource fileSrc = mock(RagExternalFileSource.class);
            when(fileSrc.getFileSourceId()).thenReturn(fileSourceId);
            when(fileSrc.getCanonicalIdentityDigest()).thenReturn(canonDigest);
            when(fileSrc.isEligible()).thenReturn(true);
            when(fileSrc.isSnapshotIntegrityFailed()).thenReturn(false);
            when(fileSrc.getSelectedRevisionId()).thenReturn(revId);
            when(ragExternalFileSourceRepository.findByFileSourceId(fileSourceId)).thenReturn(Optional.of(fileSrc));

            RagIndexGeneration gen = mock(RagIndexGeneration.class);
            when(gen.getId()).thenReturn(i);
            when(gen.getGenerationId()).thenReturn(UUID.randomUUID());
            when(gen.isActive()).thenReturn(true);
            when(gen.getStatus()).thenReturn("PUBLISHED");
            when(ragIndexGenerationRepository.findBySourceId(i)).thenReturn(List.of(gen));
        }

        assertThatThrownBy(() -> retrievalService.retrieve(cmd))
                .isInstanceOf(RagQueryScopeLimitException.class)
                .hasMessageContaining("Complete query scope exceeds the maximum allowable document limit");
    }

    @Test
    @DisplayName("F12: RagRetrievalCommand and RagRetrievalRequestDto toString redact question")
    void toStringRedactsQuestion() {
        UUID reqId = UUID.randomUUID();
        String secret = "SENTINEL_SECRET_QUESTION_TEXT";
        RagRetrievalCommand cmd = new RagRetrievalCommand(
                reqId,
                secret,
                RagRetrievalMode.GENERIC,
                List.of(),
                null,
                null,
                5
        );
        assertThat(cmd.toString()).doesNotContain(secret);
        assertThat(cmd.toString()).contains("[REDACTED");

        RagRetrievalRequestDto dto = new RagRetrievalRequestDto(
                reqId,
                secret,
                RagRetrievalMode.GENERIC,
                List.of(),
                null,
                null,
                5
        );
        assertThat(dto.toString()).doesNotContain(secret);
        assertThat(dto.toString()).contains("[REDACTED");
    }

    @Test
    @DisplayName("F05: Contextless facade rejects without starting an unowned consumer")
    void contextlessFacadeRejectsWithoutAdmission() {
        UUID reqId = UUID.randomUUID();
        RagRetrievalCommand cmd = new RagRetrievalCommand(
                reqId,
                "Valid question with no sources",
                RagRetrievalMode.GENERIC,
                List.of(),
                null,
                null,
                5
        );
        when(ragSourceRepository.findBySourceTypeAndAccessState(RagSourceType.EXTERNAL_FILE, RagAccessState.APPROVED))
                .thenReturn(List.of());

        assertThatThrownBy(() -> retrievalService.retrieve(cmd)).isInstanceOf(RagQueryUnavailableException.class);
        verifyNoInteractions(ragRetrievalAuthority,ragQueryEmbeddingSupervisor);
    }

    @Test
    @DisplayName("F09: Disabled retrieval rejects before any source resolution")
    void disabledRetrievalRejectsEarly() {
        RagRetrievalService disabledService = new RagRetrievalService(
                ragSourceRepository,
                ragExternalFileSourceRepository,
                ragIndexGenerationRepository,
                ragDocumentChunkRepository,
                ragRetrievalReaderRepository,
                ragRetrievalInspectionRepository,
                ragDependencyRepository,
                ragRetrievalAuthority,
                ragQueryEmbeddingSupervisor,
                ragOriginalBytesStore,
                ragRuntimeIdentity,
                currentAuthority,
                false,
                false
        );
        UUID reqId = UUID.randomUUID();
        RagRetrievalCommand cmd = new RagRetrievalCommand(
                reqId,
                "Valid question",
                RagRetrievalMode.GENERIC,
                List.of(),
                null,
                null,
                5
        );
        assertThatThrownBy(() -> disabledService.retrieve(cmd))
                .isInstanceOf(RagQueryUnavailableException.class)
                .hasMessageContaining("retrieval is disabled");
        verifyNoInteractions(ragSourceRepository);
    }
}
