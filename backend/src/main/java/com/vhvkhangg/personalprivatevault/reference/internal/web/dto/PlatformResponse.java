package com.vhvkhangg.personalprivatevault.reference.internal.web.dto;

import com.vhvkhangg.personalprivatevault.reference.enums.PlatformKind;

import java.time.Instant;

public record PlatformResponse(Long id, String name, PlatformKind kind, String url, Instant createdAt) {
}
