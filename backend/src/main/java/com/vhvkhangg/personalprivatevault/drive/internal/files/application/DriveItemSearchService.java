package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderIntent;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderIntentRepository;
import com.vhvkhangg.personalprivatevault.drive.internal.web.dto.DriveFileDtos;

import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Scoped search and browsing over PPV account subtree with bounded provider requests (<=8).
 */
public class DriveItemSearchService {
    public static final Set<String> DOCUMENT_MIMES = Set.of(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.presentation"
    );

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "all", "folder", "image", "pdf", "document", "text", "other"
    );

    private final DriveFileAuthority authority;
    private final DriveFolderService folders;
    private final DriveChildFolderService childFolders;
    private final DriveFileHttpClient provider;
    private final DriveItemCursors cursors;
    private final DriveChildFolderIntentRepository childFolderRepository;

    public DriveItemSearchService(DriveFileAuthority authority, DriveFolderService folders,
                                  DriveChildFolderService childFolders, DriveFileHttpClient provider,
                                  DriveItemCursors cursors,
                                  DriveChildFolderIntentRepository childFolderRepository) {
        this.authority = authority;
        this.folders = folders;
        this.childFolders = childFolders;
        this.provider = provider;
        this.cursors = cursors;
        this.childFolderRepository = childFolderRepository;
    }

    public DriveFileService.Result<DriveFileDtos.ItemPage> listItems(
            String owner, short slot, String rawScope, String rawParentId,
            int limit, String cursor, String rawName, String rawType,
            String rawFrom, String rawTo, String rawTimeZone,
            DriveFileResources.Operation op) {
        DriveFileAuthority.requireOwner(owner);
        if (limit < 1 || limit > 50) {
            throw new DriveFileException("DRIVE_INVALID_LIMIT", 400);
        }
        int effectivePageSize = Math.min(limit, 5);

        String scope = rawScope != null ? rawScope.strip() : "parent";
        if (!"parent".equals(scope) && !"all".equals(scope)) {
            throw DriveFileException.input();
        }

        String timeZone = rawTimeZone != null ? rawTimeZone.strip() : "Asia/Ho_Chi_Minh";
        if (!"Asia/Ho_Chi_Minh".equals(timeZone)) {
            throw DriveFileException.input();
        }
        ZoneId zone = ZoneId.of(timeZone);

        LocalDate fromDate = null;
        LocalDate toDate = null;
        if (rawFrom != null && !rawFrom.isBlank()) {
            try {
                fromDate = LocalDate.parse(rawFrom.strip(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                throw DriveFileException.input();
            }
        }
        if (rawTo != null && !rawTo.isBlank()) {
            try {
                toDate = LocalDate.parse(rawTo.strip(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                throw DriveFileException.input();
            }
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw DriveFileException.input();
        }
        Instant fromInstant = fromDate != null ? fromDate.atStartOfDay(zone).toInstant() : null;
        Instant toInstant = toDate != null ? toDate.plusDays(1).atStartOfDay(zone).toInstant() : null;

        String type = rawType != null ? rawType.strip().toLowerCase(Locale.ROOT) : "all";
        if (!ALLOWED_TYPES.contains(type)) {
            throw DriveFileException.input();
        }

        String normalizedName = null;
        if (rawName != null) {
            String stripped = rawName.strip();
            if (stripped.isEmpty() || stripped.codePointCount(0, stripped.length()) > 128) {
                throw DriveFileException.input();
            }
            normalizedName = Normalizer.normalize(stripped, Normalizer.Form.NFC);
        }

        boolean hasActiveCriteria = (normalizedName != null) || (fromDate != null)
                || (toDate != null) || (!"all".equals(type));

        String parentId = rawParentId != null && !rawParentId.isBlank() ? rawParentId.strip() : null;
        if ("parent".equals(scope)) {
            if (hasActiveCriteria) {
                throw DriveFileException.input();
            }
            if (parentId == null) {
                parentId = "root";
            }
        } else {
            if (parentId != null) {
                throw DriveFileException.input();
            }
        }

        var lease = folders.ready(authority.acquire(owner, slot, op), op);

        Map<String, DriveFileProof.FolderItem> provenChildrenInOp = new HashMap<>();
        if ("parent".equals(scope) && !"root".equals(parentId)) {
            var provenParent = childFolders.verifyChildFolder(lease, op, parentId);
            provenChildrenInOp.put(parentId, provenParent);
        }

        var criteria = new DriveItemCursors.Criteria(
                scope, parentId, normalizedName, type,
                rawFrom != null ? rawFrom.strip() : null,
                rawTo != null ? rawTo.strip() : null,
                timeZone, limit, effectivePageSize
        );

        var consumed = cursors.consume(cursor, owner, lease, criteria);
        String providerToken = consumed.providerToken();
        Set<String> seenTokens = consumed.seenTokens();

        String query = "trashed = false and appProperties has { key='ppvRootBinding' and value='"
                + lease.root().getRootBindingId() + "' }";
        if ("parent".equals(scope)) {
            String targetParent = "root".equals(parentId) ? lease.root().getRootFileId() : parentId;
            query += " and '" + targetParent + "' in parents";
        }

        var pageNode = provider.listItems(lease, op, query, effectivePageSize, providerToken);
        JsonNode incompleteNode = pageNode.get("incompleteSearch");
        if (incompleteNode != null && !incompleteNode.isNull()) {
            if (!incompleteNode.isBoolean() || incompleteNode.asBoolean()) {
                throw new DriveFileException("DRIVE_SEARCH_INCOMPLETE", 502);
            }
        }

        var filesArray = pageNode.get("files");
        if (filesArray == null || !filesArray.isArray() || filesArray.size() > effectivePageSize) {
            throw new DriveFileException("DRIVE_PROVIDER_FAILURE", 502);
        }

        String targetParentScope = "parent".equals(scope)
                ? ("root".equals(parentId) ? lease.root().getRootFileId() : parentId)
                : null;

        List<DriveFileDtos.Item> qualifiedItems = new ArrayList<>();
        for (JsonNode candidate : filesArray) {
            String candidateId = candidate.path("id").asText();
            if (candidateId.equals(lease.root().getRootFileId())) {
                continue;
            }
            String mime = candidate.path("mimeType").asText("");
            if ("application/vnd.google-apps.folder".equals(mime)) {
                var parentsNode = candidate.get("parents");
                if (parentsNode == null || !parentsNode.isArray() || parentsNode.size() != 1
                        || !lease.root().getRootFileId().equals(parentsNode.get(0).asText())) {
                    continue;
                }
                if (targetParentScope != null && !targetParentScope.equals(lease.root().getRootFileId())) {
                    continue;
                }
                Optional<DriveChildFolderIntent> intentOpt = childFolderRepository
                        .findBySubAndRootAndProviderFolderId(lease.sub(), lease.root().getRootBindingId(), candidateId)
                        .filter(i -> "COMPLETED".equals(i.getStatus()));
                if (intentOpt.isEmpty()) {
                    continue;
                }
                DriveFileProof.FolderItem folderItem;
                try {
                    if (!provenChildrenInOp.containsKey(candidateId)) {
                        var remoteChild = provider.childFolder(lease, op, candidateId);
                        folderItem = DriveFileProof.childFolder(remoteChild, lease.root(), candidateId, intentOpt.get());
                        provenChildrenInOp.put(candidateId, folderItem);
                    } else {
                        folderItem = provenChildrenInOp.get(candidateId);
                    }
                } catch (DriveFileException denied) {
                    if (denied.getHttpStatus() >= 500 || denied.getHttpStatus() == 409) {
                        throw denied;
                    }
                    continue;
                }

                if (matchesFilters(folderItem.name(), mime, folderItem.modifiedAt(), normalizedName, type, fromInstant, toInstant)) {
                    qualifiedItems.add(new DriveFileDtos.Item(
                            folderItem.id(), folderItem.name(), "folder", mime, null,
                            folderItem.modifiedAt(), lease.root().getRootFileId()
                    ));
                }
            } else {
                if (mime.startsWith("application/vnd.google-apps.")) {
                    continue;
                }
                var parentsNode = candidate.get("parents");
                if (parentsNode == null || !parentsNode.isArray() || parentsNode.size() != 1) {
                    continue;
                }
                String candidateParent = parentsNode.get(0).asText();
                if (targetParentScope != null && !targetParentScope.equals(candidateParent)) {
                    continue;
                }
                if (candidateParent.equals(lease.root().getRootFileId())) {
                    try {
                        var fileItem = DriveFileProof.rootFile(candidate, lease.root(), candidateId, null);
                        if (matchesFilters(fileItem.name(), fileItem.mimeType(), fileItem.modifiedAt(), normalizedName, type, fromInstant, toInstant)) {
                            qualifiedItems.add(new DriveFileDtos.Item(
                                    fileItem.id(), fileItem.name(), "file", fileItem.mimeType(),
                                    fileItem.sizeBytes(), fileItem.modifiedAt(), lease.root().getRootFileId()
                            ));
                        }
                    } catch (DriveFileException denied) {
                        if (denied.getHttpStatus() >= 500 || denied.getHttpStatus() == 409) {
                            throw denied;
                        }
                    }
                } else {
                    Optional<DriveChildFolderIntent> intentOpt = childFolderRepository
                            .findBySubAndRootAndProviderFolderId(lease.sub(), lease.root().getRootBindingId(), candidateParent)
                            .filter(i -> "COMPLETED".equals(i.getStatus()));
                    if (intentOpt.isEmpty()) {
                        continue;
                    }
                    try {
                        if (!provenChildrenInOp.containsKey(candidateParent)) {
                            var remoteChild = provider.childFolder(lease, op, candidateParent);
                            var folderItem = DriveFileProof.childFolder(remoteChild, lease.root(), candidateParent, intentOpt.get());
                            provenChildrenInOp.put(candidateParent, folderItem);
                        }
                        var fileItem = DriveFileProof.childFile(candidate, lease.root(), candidateParent, candidateId, null);
                        if (matchesFilters(fileItem.name(), fileItem.mimeType(), fileItem.modifiedAt(), normalizedName, type, fromInstant, toInstant)) {
                            qualifiedItems.add(new DriveFileDtos.Item(
                                    fileItem.id(), fileItem.name(), "file", fileItem.mimeType(),
                                    fileItem.sizeBytes(), fileItem.modifiedAt(), candidateParent
                            ));
                        }
                    } catch (DriveFileException denied) {
                        if (denied.getHttpStatus() >= 500 || denied.getHttpStatus() == 409) {
                            throw denied;
                        }
                    }
                }
            }
        }

        String nextProviderToken = null;
        if (pageNode.hasNonNull("nextPageToken")) {
            try {
                nextProviderToken = DriveFileProof.text(pageNode, "nextPageToken", 2048);
            } catch (DriveFileException invalid) {
                throw new DriveFileException("DRIVE_PROVIDER_FAILURE", 502);
            }
        }

        String nextCursor = null;
        if (nextProviderToken != null) {
            final String capturedToken = nextProviderToken;
            nextCursor = authority.finalWrite(lease, op, () -> cursors.issue(capturedToken, owner, lease, criteria, seenTokens));
        }

        boolean hasMore = (nextCursor != null);
        return new DriveFileService.Result<>(
                new DriveFileDtos.ItemPage(List.copyOf(qualifiedItems), nextCursor, hasMore),
                lease, false
        );
    }

    private boolean matchesFilters(String name, String mime, Instant modifiedAt,
                                   String normalizedQuery, String type,
                                   Instant from, Instant to) {
        if (normalizedQuery != null) {
            String lowerName = name.toLowerCase(Locale.ROOT);
            String lowerQuery = normalizedQuery.toLowerCase(Locale.ROOT);
            if (!lowerName.contains(lowerQuery)) return false;
        }
        if (type != null && !"all".equals(type)) {
            if (!matchesType(mime, type)) return false;
        }
        if (from != null && (modifiedAt == null || modifiedAt.isBefore(from))) return false;
        if (to != null && (modifiedAt == null || !modifiedAt.isBefore(to))) return false;
        return true;
    }

    private boolean matchesType(String mime, String type) {
        return switch (type) {
            case "folder" -> "application/vnd.google-apps.folder".equals(mime);
            case "image" -> mime.startsWith("image/");
            case "pdf" -> "application/pdf".equals(mime);
            case "document" -> DOCUMENT_MIMES.contains(mime);
            case "text" -> mime.startsWith("text/");
            case "other" -> !mime.startsWith("image/")
                    && !"application/pdf".equals(mime)
                    && !DOCUMENT_MIMES.contains(mime)
                    && !mime.startsWith("text/")
                    && !"application/vnd.google-apps.folder".equals(mime)
                    && !mime.startsWith("application/vnd.google-apps.");
            default -> false;
        };
    }
}
