package com.vhvkhangg.personalprivatevault.drive.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/** Explicit S2 wire projections; no persistence, lease or private provider proof is serialized. */
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
}
