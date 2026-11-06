package com.vhvkhangg.personalprivatevault.drive.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after disconnecting a Google Drive connection slot")
public record DriveDisconnectResponse(
        @Schema(description = "Disconnected slot index (1 or 2)", example = "1")
        int slot,

        @Schema(description = "Local status after disconnection, always DISCONNECTED", example = "DISCONNECTED")
        String status,

        @Schema(description = "Best-effort remote revocation outcome: REQUEST_ACCEPTED, FAILED, or UNCERTAIN", example = "REQUEST_ACCEPTED", nullable = true)
        String remoteRevocation
) {}
