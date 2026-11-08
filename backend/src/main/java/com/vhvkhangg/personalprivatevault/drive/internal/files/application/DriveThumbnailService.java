package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderIntent;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderIntentRepository;

import java.util.Optional;

/** Strict private thumbnail proxy with fallback on missing/unsupported/untrusted provider preview. */
public class DriveThumbnailService {
    public record ThumbnailImage(byte[] bytes, String contentType) {
        @Override public String toString(){return "DriveThumbnailImage[REDACTED]";}
    }

    private final DriveFileAuthority authority;
    private final DriveFolderService folders;
    private final DriveFileHttpClient provider;
    private final DriveChildFolderIntentRepository childFolderRepository;

    public DriveThumbnailService(DriveFileAuthority authority, DriveFolderService folders,
                                 DriveFileHttpClient provider,
                                 DriveChildFolderIntentRepository childFolderRepository) {
        this.authority = authority;
        this.folders = folders;
        this.provider = provider;
        this.childFolderRepository = childFolderRepository;
    }

    public DriveFileService.Result<ThumbnailImage> thumbnail(
            String owner, short slot, String fileId, DriveFileResources.Operation op) {
        DriveFileAuthority.requireOwner(owner);
        DriveFileInputs.id(fileId);
        var lease = folders.ready(authority.acquire(owner, slot, op), op);

        var meta = provider.fileWithThumbnail(lease, op, fileId);
        DriveFileProof.FileItem fileItem;
        try {
            fileItem = DriveFileProof.rootFile(meta, lease.root(), fileId, null);
        } catch (DriveFileException notRoot) {
            var parents = meta.get("parents");
            if (parents == null || !parents.isArray() || parents.size() != 1 || !parents.get(0).isTextual()) {
                throw notRoot;
            }
            String candidateParentId = parents.get(0).textValue();
            Optional<DriveChildFolderIntent> intentOpt = childFolderRepository
                    .findBySubAndRootAndProviderFolderId(lease.sub(), lease.root().getRootBindingId(), candidateParentId)
                    .filter(i -> "COMPLETED".equals(i.getStatus()));
            if (intentOpt.isEmpty()) {
                throw notRoot;
            }
            var remoteChild = provider.childFolder(lease, op, candidateParentId);
            DriveFileProof.childFolder(remoteChild, lease.root(), candidateParentId, intentOpt.get());
            fileItem = DriveFileProof.childFile(meta, lease.root(), candidateParentId, fileId, null);
        }

        String thumbnailLink = meta.path("thumbnailLink").isTextual() ? meta.path("thumbnailLink").textValue() : null;
        if (thumbnailLink == null || thumbnailLink.isBlank()) {
            throw new DriveFileException("THUMBNAIL_UNAVAILABLE", 404);
        }

        var image = provider.fetchThumbnail(lease, op, thumbnailLink);
        authority.verify(lease, op);
        return new DriveFileService.Result<>(image, lease, false);
    }
}
