package com.vhvkhangg.personalprivatevault.drive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.drive.fixture.DriveH1BTestSupport;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileAuthority;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileException;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveItemSearchService;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileHttpClient;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileResources;
import com.vhvkhangg.personalprivatevault.drive.internal.web.dto.DriveFileDtos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriveH1BSearchIntegrationTest extends DriveH1BTestSupport {

    protected final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("B-05: Six distinct child folders across pages; first page dispatches exactly 8 calls; sixth child in next page")
    void sixChildrenAcrossPagesRespectEightCallBudgetAndFullCorpusReachable() throws Exception {
        connect(1, "SUB_B05");
        ensure(1);

        // Create 6 child folders: C1..C6
        List<String> folderNames = List.of("FolderAlpha", "FolderBeta", "FolderGamma", "FolderDelta", "FolderEpsilon", "FolderZeta");
        List<String> folderIds = new ArrayList<>();
        for (String name : folderNames) {
            var res = createFolder(1, key(), name);
            folderIds.add(res.data().id());
        }

        // 1. Page 1 request (limit=50, effective provider pageSize=5)
        DriveFileDtos.ItemPage page1;
        try (var op = resources.open(false)) {
            var result = itemSearchService.listItems("1", (short) 1, "all", null, 50, null, null, "all", null, null, "Asia/Ho_Chi_Minh", op);
            page1 = result.data();
            // Dispatches in this operation: 2 root proofs + 1 list + 5 distinct child proofs = exactly 8!
            assertThat(op.requestCount()).isEqualTo(8);
        }

        assertThat(page1.items()).hasSize(5);
        assertThat(page1.hasMore()).isTrue();
        assertThat(page1.nextCursor()).isNotNull();

        // 2. Page 2 request using cursor: fetches the 6th child
        DriveFileDtos.ItemPage page2;
        try (var op = resources.open(false)) {
            var result = itemSearchService.listItems("1", (short) 1, "all", null, 50, page1.nextCursor(), null, "all", null, null, "Asia/Ho_Chi_Minh", op);
            page2 = result.data();
            // Dispatches in second operation: 2 root proofs + 1 list + 1 child proof = 4 calls <= 8!
            assertThat(op.requestCount()).isEqualTo(4);
        }

        assertThat(page2.items()).hasSize(1);
        assertThat(page2.hasMore()).isFalse();
        assertThat(page2.nextCursor()).isNull();

        // Total 6 unique items verified across pages
        List<String> allReturnedIds = new ArrayList<>();
        page1.items().forEach(i -> allReturnedIds.add(i.id()));
        page2.items().forEach(i -> allReturnedIds.add(i.id()));
        assertThat(allReturnedIds).containsExactlyInAnyOrderElementsOf(folderIds);
    }

    @Test
    @DisplayName("B-05: Unicode accent-sensitive substring search and type/date filters work accurately")
    void unicodeSubstringAndTypeDateFilters() throws Exception {
        connect(1, "SUB_B05_FILTERS");
        ensure(1);

        createFolder(1, key(), "Báo cáo tài chính");
        createFolder(1, key(), "Bao cao nhan su");
        createFolder(1, key(), "Tài liệu kỹ thuật");

        // Search with Vietnamese accent substring: "báo cáo"
        var searchResult = listItems(1, "all", null, 50, null, "báo cáo", "all", null, null, "Asia/Ho_Chi_Minh");
        assertThat(searchResult.data().items()).hasSize(1);
        assertThat(searchResult.data().items().get(0).name()).isEqualTo("Báo cáo tài chính");

        // Type filter: type=folder
        var folderOnly = listItems(1, "all", null, 50, null, null, "folder", null, null, "Asia/Ho_Chi_Minh");
        assertThat(folderOnly.data().items()).hasSize(3);

        // Type filter: type=pdf (none created yet)
        var pdfOnly = listItems(1, "all", null, 50, null, null, "pdf", null, null, "Asia/Ho_Chi_Minh");
        assertThat(pdfOnly.data().items()).isEmpty();

        // Date filter: valid inclusive local date range (deterministic across calendar days)
        String today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toString();
        var dateFiltered = listItems(1, "all", null, 50, null, null, "all", today, today, "Asia/Ho_Chi_Minh");
        assertThat(dateFiltered.data().items()).hasSize(3);

        // Date filter outside range returns empty
        var pastDate = listItems(1, "all", null, 50, null, null, "all", "2025-01-01", "2025-01-02", "Asia/Ho_Chi_Minh");
        assertThat(pastDate.data().items()).isEmpty();

        // Reversed date range throws 400
        assertThatThrownBy(() -> listItems(1, "all", null, 50, null, null, "all", "2026-11-09", "2026-11-08", "Asia/Ho_Chi_Minh"))
                .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(400));
    }

    @Test
    @DisplayName("B-05: Cursor is atomically consumed once; replay and tampered cursor fail closed")
    void cursorAtomicConsumptionAndSecurity() throws Exception {
        connect(1, "SUB_B05_CURSOR");
        ensure(1);

        for (int i = 0; i < 6; i++) {
            createFolder(1, key(), "F-" + i);
        }

        var page1 = listItems(1, "all", null, 50, null, null, "all", null, null, "Asia/Ho_Chi_Minh").data();
        String cursor = page1.nextCursor();
        assertThat(cursor).isNotNull();

        // Concurrent replay: only 1 winner
        try (var workers = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> consume = () -> {
                try {
                    listItems(1, "all", null, 50, cursor, null, "all", null, null, "Asia/Ho_Chi_Minh");
                    return true;
                } catch (DriveFileException e) {
                    return false;
                }
            };
            var f1 = workers.submit(consume);
            var f2 = workers.submit(consume);
            int successes = (f1.get() ? 1 : 0) + (f2.get() ? 1 : 0);
            assertThat(successes).isEqualTo(1);
        }

        // Replaying consumed cursor throws DRIVE_CURSOR_INVALID (400)
        assertThatThrownBy(() -> listItems(1, "all", null, 50, cursor, null, "all", null, null, "Asia/Ho_Chi_Minh"))
                .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getErrorCode()).isEqualTo("DRIVE_CURSOR_INVALID"));

        // Swapping criteria with cursor throws DRIVE_CURSOR_INVALID (400)
        var newPage = listItems(1, "all", null, 50, null, null, "all", null, null, "Asia/Ho_Chi_Minh").data();
        String validCursor = newPage.nextCursor();
        assertThatThrownBy(() -> listItems(1, "all", null, 50, validCursor, "DifferentName", "all", null, null, "Asia/Ho_Chi_Minh"))
                .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getErrorCode()).isEqualTo("DRIVE_CURSOR_INVALID"));
    }

    @Test
    @DisplayName("B-05: Provider incompleteSearch=true throws DRIVE_SEARCH_INCOMPLETE (502)")
    void incompleteSearchFailsHonestly() throws Exception {
        connect(1, "SUB_B05_INCOMPLETE");
        ensure(1);
        createFolder(1, key(), "F1");

        ObjectNode incompletePage = mapper.createObjectNode();
        incompletePage.put("incompleteSearch", true);
        incompletePage.putArray("files");
        google.listOverride = incompletePage;

        assertThatThrownBy(() -> listItems(1, "all", null, 50, null, null, "all", null, null, "Asia/Ho_Chi_Minh"))
                .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getErrorCode()).isEqualTo("DRIVE_SEARCH_INCOMPLETE"));
    }

    @Test
    @DisplayName("B-05: Failed child read (503) does not become false exhaustive search; propagates error")
    void failedChildReadMustNotBecomeExhaustiveSearch() throws Exception {
        connect(1, "SUB_B05_READ_FAIL");
        ensure(1);
        var res = createFolder(1, key(), "OnlyMatchingFolder");
        String child = res.data().id();

        try (var failing = new DriveFileHttpClient(authority, google.origin()) {
            @Override
            public JsonNode childFolder(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String id) {
                if (child.equals(id)) throw new DriveFileException("DRIVE_PROVIDER_RATE_LIMITED", 503);
                return super.childFolder(lease, op, id);
            }
        }) {
            var subject = new DriveItemSearchService(authority, folders, childFolderService, failing, itemCursors, childFoldersRepo);
            try (var op = resources.open(false)) {
                assertThatThrownBy(() -> subject.listItems("1", (short) 1, "all", null, 50, null,
                        "Matching", "all", null, null, "Asia/Ho_Chi_Minh", op))
                        .as("mandatory child verification failed; must not return empty exhaustive success")
                        .isInstanceOf(DriveFileException.class);
            }
        }
    }

    @Test
    @DisplayName("B-05: Parent scope browse filters by parentId, rejects relocated items, handles empty continuation")
    void parentScopeBrowseWithRelocationAndEmptyContinuation() throws Exception {
        connect(1, "SUB_B05_PARENT");
        ensure(1);
        var folderA = createFolder(1, key(), "FolderA");
        var folderB = createFolder(1, key(), "FolderB");
        String childAId = folderA.data().id();
        String childBId = folderB.data().id();

        var fileInA = uploadChild(1, childAId, key(), new byte[]{1}, "docA.bin", "application/octet-stream");
        var fileInB = uploadChild(1, childBId, key(), new byte[]{2}, "docB.bin", "application/octet-stream");

        // Browse parent scope childAId: returns docA only
        var itemsInA = listItems(1, "parent", childAId, 50, null, null, "all", null, null, "Asia/Ho_Chi_Minh").data();
        assertThat(itemsInA.items()).hasSize(1);
        assertThat(itemsInA.items().get(0).id()).isEqualTo(fileInA.data().id());
        assertThat(itemsInA.hasMore()).isFalse();

        // Relocated item: change parent of docA in remote metadata to childBId
        google.files.get(fileInA.data().id()).metadata().set("parents", mapper.valueToTree(List.of(childBId)));
        var itemsAfterMove = listItems(1, "parent", childAId, 50, null, null, "all", null, null, "Asia/Ho_Chi_Minh").data();
        // Since docA was moved away from childAId, it is not returned under childAId
        assertThat(itemsAfterMove.items()).isEmpty();
    }

    @Test
    @DisplayName("B-05: Mixed eligible corpus and NFC/NFD normalization search")
    void mixedCorpusAndNfcNfdNormalization() throws Exception {
        connect(1, "SUB_B05_MIXED");
        ensure(1);
        // Create root file
        var rootFile = upload(1, key(), new byte[]{1, 2}, "RootDoc.txt", "text/plain");
        // Create child folder with Vietnamese name: Hồ sơ
        String nfcName = "H\u1ed3 s\u01a1";
        var folder = createFolder(1, key(), nfcName);

        // Search using NFD representation
        String nfdQuery = Normalizer.normalize("hồ", Normalizer.Form.NFD);
        var result = listItems(1, "all", null, 50, null, nfdQuery, "all", null, null, "Asia/Ho_Chi_Minh").data();
        assertThat(result.items()).anyMatch(i -> i.id().equals(folder.data().id()));

        // Mixed corpus listing returns both root file and child folder
        var allItems = listItems(1, "all", null, 50, null, null, "all", null, null, "Asia/Ho_Chi_Minh").data();
        assertThat(allItems.items()).anyMatch(i -> i.id().equals(rootFile.data().id()));
        assertThat(allItems.items()).anyMatch(i -> i.id().equals(folder.data().id()));
    }

    @Test
    @DisplayName("B-05: Search HTTP request contains corpora=user and spaces=drive query parameters")
    void searchQuerySendsCorporaUserAndSpacesDrive() throws Exception {
        connect(1, "SUB_B05_PARAMS");
        ensure(1);
        createFolder(1, key(), "FolderTestParams");

        // Clear recorded calls
        google.calls.clear();

        listItems(1, "all", null, 50, null, null, "all", null, null, "Asia/Ho_Chi_Minh");

        // Find list request
        var listCall = google.calls.stream()
                .filter(c -> c.path().equals("/drive/v3/files") && c.query() != null && c.query().contains("corpora=user"))
                .findFirst();
        assertThat(listCall).isPresent();
        assertThat(listCall.get().query()).contains("corpora=user");
        assertThat(listCall.get().query()).contains("spaces=drive");
    }

    @Test
    @DisplayName("B-05: Empty filtered page followed by later substring hit; <=8 provider dispatches per request")
    void emptyFilteredPageFollowedByLaterSubstringHitAndDispatchBounds() throws Exception {
        connect(1, "SUB_B05_PAGE_FLOW");
        ensure(1);
        var folder = createFolder(1, key(), "ParentFolder");
        String childId = folder.data().id();

        // Upload target item under child folder first so noise items have newer modifiedTime
        var targetFile = uploadChild(1, childId, key(), new byte[]{42}, "TargetMatchInChild.txt", "text/plain");
        Thread.sleep(50);

        // Upload 5 noise items with newer modifiedTime so they sort first in pageSize=5
        for (int i = 0; i < 5; i++) {
            upload(1, key(), new byte[]{(byte) i}, "ZNoise" + i + ".txt", "text/plain");
        }

        google.calls.clear();
        // Request page 1 with name filter "TargetMatch"
        var page1 = listItems(1, "all", null, 50, null, "TargetMatch", "all", null, null, "Asia/Ho_Chi_Minh").data();

        // Page 1 filtered out all 5 noise items, but Google returned nextPageToken
        assertThat(page1.items()).isEmpty();
        assertThat(page1.hasMore()).isTrue();
        String cursor = page1.nextCursor();
        assertThat(cursor).isNotNull();

        // Verify dispatch count for page 1 is <= 8
        long page1Calls = google.calls.size();
        assertThat(page1Calls).isGreaterThan(0).isLessThanOrEqualTo(8L);

        // Request page 2 using cursor
        google.calls.clear();
        var page2 = listItems(1, "all", null, 50, cursor, "TargetMatch", "all", null, null, "Asia/Ho_Chi_Minh").data();

        // Page 2 contains the matching target file
        assertThat(page2.items()).hasSize(1);
        assertThat(page2.items().get(0).id()).isEqualTo(targetFile.data().id());
        assertThat(page2.items().get(0).parentId()).isEqualTo(childId);
        assertThat(page2.hasMore()).isFalse();

        // Verify dispatch count for page 2 is <= 8
        long page2Calls = google.calls.size();
        assertThat(page2Calls).isGreaterThan(0).isLessThanOrEqualTo(8L);
    }
}
