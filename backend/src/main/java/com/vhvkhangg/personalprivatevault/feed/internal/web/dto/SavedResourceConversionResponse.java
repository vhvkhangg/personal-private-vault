package com.vhvkhangg.personalprivatevault.feed.internal.web.dto;

import java.time.Instant;

public record SavedResourceConversionResponse(
        Long savedResourceId,
        Long targetVaultEntryId,
        Instant createdAt
) {}
