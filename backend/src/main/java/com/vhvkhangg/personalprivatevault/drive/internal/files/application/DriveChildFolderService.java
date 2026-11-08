package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderIntent;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderIntentRepository;

import java.util.Optional;
import java.util.Set;

/** App-owned first-level child folder operations over durable intent ledger. */
public class DriveChildFolderService {
    private final DriveFileAuthority authority;
    private final DriveFolderService folders;
    private final DriveFileHttpClient provider;
    private final DriveChildFolderIntentStore store;
    private final DriveChildFolderIntentRepository repository;

    public DriveChildFolderService(DriveFileAuthority authority, DriveFolderService folders,
                                   DriveFileHttpClient provider, DriveChildFolderIntentStore store,
                                   DriveChildFolderIntentRepository repository) {
        this.authority = authority;
        this.folders = folders;
        this.provider = provider;
        this.store = store;
        this.repository = repository;
    }

    public DriveFileService.Result<DriveFileProof.FolderItem> create(
            String owner, short slot, String key, String rawName, DriveFileResources.Operation op) {
        DriveFileAuthority.requireOwner(owner);
        String safeName = DriveFileInputs.name(rawName);
        DriveFileInputs.folderKeyTime(key);
        var lease = folders.ready(authority.acquire(owner, slot, op), op);

        try (var claim = store.take(lease, op, key, safeName)) {
            boolean dispatched = false;
            try {
                var state = claim.intent().getStatus();
                if (Set.of("DISPATCHED", "RECONCILIATION_REQUIRED", "COMPLETED").contains(state)) {
                    var remote = provider.childFolder(lease, op, claim.intent().getProviderFolderId());
                    var item = DriveFileProof.childFolder(remote, lease.root(), claim.intent().getProviderFolderId(), claim.intent());
                    store.complete(claim, lease, op);
                    return new DriveFileService.Result<>(item, lease, false);
                }
                if (state.equals("PREPARING")) {
                    String generatedId = provider.generateId(lease, op);
                    store.reserve(claim, lease, op, generatedId);
                }
                store.dispatched(claim, lease, op);
                dispatched = true;

                var remote = provider.createChildFolder(
                        lease, op, claim.intent().getProviderFolderId(), safeName,
                        lease.root().getRootBindingId(), DriveFileInputs.action(key));
                var item = DriveFileProof.childFolder(remote, lease.root(), claim.intent().getProviderFolderId(), claim.intent());
                store.complete(claim, lease, op);
                return new DriveFileService.Result<>(item, lease, true);
            } catch (RuntimeException error) {
                if (dispatched || Set.of("DISPATCHED", "RECONCILIATION_REQUIRED", "COMPLETED").contains(claim.intent().getStatus())) {
                    try { store.unknown(claim); } catch (DriveFileException ignored) {}
                    throw new DriveFileException("FOLDER_OUTCOME_UNKNOWN", 409);
                }
                throw error;
            }
        }
    }

    public DriveFileProof.FolderItem verifyChildFolder(
            DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String childId) {
        DriveFileInputs.id(childId);
        Optional<DriveChildFolderIntent> intent = repository.findBySubAndRootAndProviderFolderId(
                lease.sub(), lease.root().getRootBindingId(), childId)
                .filter(i -> "COMPLETED".equals(i.getStatus()));
        if (intent.isEmpty()) {
            throw DriveFileProof.unavailable();
        }
        var remote = provider.childFolder(lease, op, childId);
        return DriveFileProof.childFolder(remote, lease.root(), childId, intent.get());
    }

    public Optional<DriveChildFolderIntent> findCompletedIntent(
            DriveFileAuthority.Lease lease, String childId) {
        return repository.findBySubAndRootAndProviderFolderId(
                lease.sub(), lease.root().getRootBindingId(), childId)
                .filter(i -> "COMPLETED".equals(i.getStatus()));
    }
}
