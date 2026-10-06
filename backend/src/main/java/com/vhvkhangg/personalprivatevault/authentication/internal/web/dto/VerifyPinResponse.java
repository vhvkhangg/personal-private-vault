package com.vhvkhangg.personalprivatevault.authentication.internal.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Private PIN verification response")
public record VerifyPinResponse(
        @Schema(description = "Whether the submitted PIN was valid", example = "true")
        boolean verified
) {}
