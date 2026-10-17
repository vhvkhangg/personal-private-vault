package com.vhvkhangg.personalprivatevault.vault.entry;

import java.util.Map;

/** Immutable, exact source-set snapshot. No JPA state crosses the module boundary. */
public record SourceVersions(Map<Long, SourceVersion> sources) {
    public SourceVersions {
        sources = Map.copyOf(sources);
    }

    public SourceVersion require(long id) {
        var source = sources.get(id);
        if (source == null) throw new IllegalStateException("Source history unavailable");
        return source;
    }
}
