package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

/**
 * Process-local opaque 43-character handles for Drive search/items pagination;
 * 300s expiry, <=128 outstanding, atomically consumed once per operation.
 */
public class DriveItemCursors {
    public record Criteria(
            String scope,
            String parentId,
            String name,
            String type,
            String from,
            String to,
            String timeZone,
            int callerLimit,
            int effectivePageSize
    ) {}

    private record Entry(
            String owner,
            String sub,
            short slot,
            UUID credentialEpoch,
            UUID rootBindingId,
            UUID rootBindingEpoch,
            Criteria criteria,
            String providerToken,
            Instant expiry,
            Set<String> seenTokens
    ) {
        @Override
        public String toString() {
            return "DriveItemCursor[REDACTED]";
        }
    }

    public record ConsumedCursor(String providerToken, Set<String> seenTokens) {}

    private final Clock clock;
    private final Map<String, Entry> entries = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    public DriveItemCursors(Clock clock) {
        this.clock = clock;
    }

    public synchronized ConsumedCursor consume(
            String cursorHandle,
            String owner,
            DriveFileAuthority.Lease lease,
            Criteria expectedCriteria
    ) {
        purge();
        if (cursorHandle == null) return new ConsumedCursor(null, Set.of());
        if (cursorHandle.length() != 43 || !cursorHandle.matches("[A-Za-z0-9_-]{43}")) throw invalid();
        Entry entry = entries.remove(cursorHandle);
        if (entry == null) throw invalid();
        if (!Objects.equals(entry.owner(), owner)
                || !Objects.equals(entry.sub(), lease.sub())
                || entry.slot() != lease.slot()
                || !Objects.equals(entry.credentialEpoch(), lease.epoch())
                || !Objects.equals(entry.rootBindingId(), lease.root().getRootBindingId())
                || !Objects.equals(entry.rootBindingEpoch(), lease.root().getBindingEpoch())
                || !Objects.equals(entry.criteria(), expectedCriteria)) {
            throw invalid();
        }
        return new ConsumedCursor(entry.providerToken(), entry.seenTokens());
    }

    public synchronized String issue(
            String providerToken,
            String owner,
            DriveFileAuthority.Lease lease,
            Criteria criteria,
            Set<String> previousSeenTokens
    ) {
        if (providerToken == null) return null;
        if (providerToken.isBlank() || providerToken.getBytes(StandardCharsets.UTF_8).length > 2048) {
            throw new DriveFileException("DRIVE_SEARCH_INCOMPLETE", 502);
        }
        purge();
        if (entries.size() >= 128) throw DriveFileException.capacity();

        Set<String> updatedTokens = new HashSet<>(previousSeenTokens != null ? previousSeenTokens : Set.of());
        if (updatedTokens.contains(providerToken) || updatedTokens.size() >= 64) {
            throw new DriveFileException("DRIVE_SEARCH_INCOMPLETE", 502);
        }
        updatedTokens.add(providerToken);

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String handle = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        entries.put(handle, new Entry(
                owner,
                lease.sub(),
                lease.slot(),
                lease.epoch(),
                lease.root().getRootBindingId(),
                lease.root().getBindingEpoch(),
                criteria,
                providerToken,
                clock.instant().plusSeconds(300),
                Collections.unmodifiableSet(updatedTokens)
        ));
        return handle;
    }

    private void purge() {
        Instant now = clock.instant();
        entries.values().removeIf(e -> !now.isBefore(e.expiry()));
    }

    private DriveFileException invalid() {
        return new DriveFileException("DRIVE_CURSOR_INVALID", 400);
    }
}
