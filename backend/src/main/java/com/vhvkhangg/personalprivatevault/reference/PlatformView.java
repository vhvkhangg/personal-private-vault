package com.vhvkhangg.personalprivatevault.reference;

import java.time.Instant;

/** Public read model for an external platform. */
public record PlatformView(Long id, String name, PlatformKind kind, String url, Instant createdAt) {
}
