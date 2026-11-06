package com.vhvkhangg.personalprivatevault.drive.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of a Google Drive connection slot")
public record DriveConnectionItemResponse(
        @Schema(description = "Slot index (1 or 2)", example = "1")
        int slot,

        @Schema(description = "Connection status: DISCONNECTED, CONNECTED, or RECONNECT_REQUIRED", example = "CONNECTED")
        String status,

        @Schema(description = "Display-only email address associated with the connection, null if disconnected", example = "owner@example.com", nullable = true)
        String displayEmail,

        @Schema(description = "Transient remote revocation status: PENDING, REQUEST_ACCEPTED, FAILED, or UNCERTAIN", example = "REQUEST_ACCEPTED", nullable = true)
        String remoteRevocation
) {}
