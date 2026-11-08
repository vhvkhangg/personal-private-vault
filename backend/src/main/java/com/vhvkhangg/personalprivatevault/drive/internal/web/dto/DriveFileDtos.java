package com.vhvkhangg.personalprivatevault.drive.internal.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/** Explicit S2 and H1-B wire projections; no persistence, lease or private provider proof is serialized. */
public final class DriveFileDtos {
    private DriveFileDtos(){}

    public record Root(int slot,String status){}

    public record File(String id,String name,String mimeType,long sizeBytes,Instant modifiedAt){
        @Override public String toString(){return "DriveFileResponse[REDACTED]";}
    }

    public record Page(List<File> files,String nextCursor,boolean hasMore){}

    public static class Upload {
        @Schema(type="string",format="binary",requiredMode=Schema.RequiredMode.REQUIRED)
        public String file;
    }

    public record Folder(String id, String name, String parentId, String kind, Instant modifiedAt) {
        @Override public String toString(){return "DriveFolderResponse[REDACTED]";}
    }

    public record FolderCreateRequest(
            @Schema(description = "Safe child folder name (1-255 UTF-8 bytes, no slash/control characters)", requiredMode = Schema.RequiredMode.REQUIRED)
            String name
    ) {}

    public record StorageQuota(
            Long usageBytes,
            Long usageInDriveBytes,
            Long limitBytes,
            boolean limitKnown,
            Instant observedAt
    ) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Item(
            String id,
            String name,
            String kind,
            String mimeType,
            Long sizeBytes,
            Instant modifiedAt,
            String parentId
    ) {
        @Override public String toString(){return "DriveItemResponse[REDACTED]";}
    }

    public record ItemPage(List<Item> items, String nextCursor, boolean hasMore) {}
}
