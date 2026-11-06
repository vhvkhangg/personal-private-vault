package com.vhvkhangg.personalprivatevault.drive.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Listing of both Google Drive connection slots")
public record DriveConnectionListResponse(
        @Schema(description = "Ordered list of exactly two connection slots")
        List<DriveConnectionItemResponse> connections
) {}
