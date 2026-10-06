package com.vhvkhangg.personalprivatevault.vault.internal.web.dto;

import java.time.Instant;

public record TagResponse(Long id, String name, Instant createdAt) {
}
