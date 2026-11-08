package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileHttpClient;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileResources;
import com.vhvkhangg.personalprivatevault.drive.internal.web.dto.DriveFileDtos;

import java.time.Clock;

/** Quota and explorer projections over active S1 lease. */
public class DriveExplorerService {
    private final DriveFileAuthority authority;
    private final DriveFileHttpClient provider;
    private final Clock clock;

    public DriveExplorerService(DriveFileAuthority authority, DriveFileHttpClient provider, Clock clock) {
        this.authority = authority;
        this.provider = provider;
        this.clock = clock;
    }

    public DriveFileService.Result<DriveFileDtos.StorageQuota> storageQuota(
            String owner, short slot, DriveFileResources.Operation op) {
        DriveFileAuthority.requireOwner(owner);
        var lease = authority.acquire(owner, slot, op);
        JsonNode aboutNode = provider.storageQuota(lease, op);
        JsonNode quotaNode = aboutNode.path("storageQuota");
        if (!quotaNode.isObject()) {
            throw new DriveFileException("DRIVE_PROVIDER_FAILURE", 502);
        }

        Long usage = parseOptionalLong(quotaNode, "usage");
        Long usageInDrive = parseOptionalLong(quotaNode, "usageInDrive");
        Long limit = parseOptionalLong(quotaNode, "limit");
        boolean limitKnown = (limit != null && limit >= 0);

        var quota = new DriveFileDtos.StorageQuota(
                usage,
                usageInDrive,
                limitKnown ? limit : null,
                limitKnown,
                clock.instant()
        );

        authority.verify(lease, op);
        return new DriveFileService.Result<>(quota, lease, false);
    }

    private Long parseOptionalLong(JsonNode node, String field) {
        if (!node.has(field)) return null;
        JsonNode valNode = node.get(field);
        if (valNode.isNull()) return null;
        if (!valNode.isTextual() && !valNode.isIntegralNumber()) {
            throw new DriveFileException("DRIVE_PROVIDER_FAILURE", 502);
        }
        String text;
        if (valNode.isTextual()) {
            text = valNode.textValue();
            if (text == null || text.isBlank() || !text.matches("0|[1-9][0-9]{0,18}")) {
                throw new DriveFileException("DRIVE_PROVIDER_FAILURE", 502);
            }
        } else {
            text = valNode.asText();
        }
        try {
            long val = Long.parseLong(text);
            if (val < 0) throw new DriveFileException("DRIVE_PROVIDER_FAILURE", 502);
            return val;
        } catch (NumberFormatException e) {
            throw new DriveFileException("DRIVE_PROVIDER_FAILURE", 502);
        }
    }
}
