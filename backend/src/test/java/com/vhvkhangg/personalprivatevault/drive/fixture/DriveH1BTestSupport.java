package com.vhvkhangg.personalprivatevault.drive.fixture;

import com.vhvkhangg.personalprivatevault.drive.internal.files.application.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.web.dto.DriveFileDtos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public abstract class DriveH1BTestSupport extends DriveS2TestSupport {
    protected DriveChildFolderIntentStore folderStore;
    protected DriveChildFolderService childFolderService;
    protected DriveItemCursors itemCursors;
    protected DriveItemSearchService itemSearchService;
    protected DriveThumbnailService thumbnailService;
    protected DriveExplorerService explorerService;
    protected FakeGoogleThumbnailServer thumbnailServer;

    @BeforeEach
    @Override
    void fixture() throws Exception {
        super.fixture();
        thumbnailServer = new FakeGoogleThumbnailServer();
        rebuildH1BBeans(DriveFileResources.Limits.production());
    }

    protected void rebuildH1BBeans(DriveFileResources.Limits limits) {
        if (provider != null) provider.close();
        if (resources != null) resources.close();
        resources = new DriveFileResources(spool, limits);
        authority = new DriveFileAuthority(connections, roots, crypto, lifecycle, oauth, clock, transactions);
        provider = new DriveFileHttpClient(authority, google.origin(), thumbnailServer.origin());
        folders = new DriveFolderService(authority, provider, roots, clock);
        store = new DriveUploadIntentStore(authority, roots, intents, clock);
        service = new DriveFileService(authority, folders, provider, new DriveFileCursors(clock), store, childFoldersRepo);

        folderStore = new DriveChildFolderIntentStore(authority, roots, childFoldersRepo, clock);
        childFolderService = new DriveChildFolderService(authority, folders, provider, folderStore, childFoldersRepo);
        itemCursors = new DriveItemCursors(clock);
        itemSearchService = new DriveItemSearchService(authority, folders, childFolderService, provider, itemCursors, childFoldersRepo);
        thumbnailService = new DriveThumbnailService(authority, folders, provider, childFolderRepo());
        explorerService = new DriveExplorerService(authority, provider, clock);
    }

    protected com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderIntentRepository childFolderRepo() {
        return childFoldersRepo;
    }

    protected DriveFileService.Result<DriveFileProof.FolderItem> createFolder(int slot, String key, String name) {
        try (var op = resources.open(false)) {
            return childFolderService.create("1", (short) slot, key, name, op);
        }
    }

    protected DriveFileService.Result<DriveFileProof.FileItem> uploadChild(int slot, String childId, String key, byte[] bytes, String name, String mime) throws IOException {
        try (var op = resources.open(true)) {
            return service.uploadChild("1", (short) slot, childId, key, raw(bytes, name, mime, op), op);
        }
    }

    protected DriveFileService.Result<DriveFileProof.FileItem> uploadChild(int slot, String childId, String key, byte[] bytes) throws IOException {
        return uploadChild(slot, childId, key, bytes, "child-file.bin", "application/octet-stream");
    }

    protected DriveFileService.Result<DriveFileDtos.ItemPage> listItems(
            int slot, String scope, String parentId, int limit, String cursor,
            String name, String type, String from, String to, String timeZone) {
        try (var op = resources.open(false)) {
            return itemSearchService.listItems("1", (short) slot, scope, parentId, limit, cursor, name, type, from, to, timeZone, op);
        }
    }

    @AfterEach
    @Override
    void release() throws Exception {
        if (thumbnailServer != null) thumbnailServer.close();
        super.release();
    }
}
