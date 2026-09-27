package com.vhvkhangg.personalprivatevault.vault;

import java.time.Instant;

/** Public read model for a global tag. */
public record TagView(Long id, String name, Instant createdAt) {
}
