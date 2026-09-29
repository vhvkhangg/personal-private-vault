package com.vhvkhangg.personalprivatevault.account.internal.application;

import com.vhvkhangg.personalprivatevault.account.internal.domain.FollowerSnapshot;
import com.vhvkhangg.personalprivatevault.account.internal.domain.FollowerSnapshotEntry;
import com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence.ExternalAccountRepository;
import com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence.FollowerSnapshotEntryRepository;
import com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence.FollowerSnapshotRepository;
import com.vhvkhangg.personalprivatevault.account.snapshot.CreateFollowerSnapshotCommand;
import com.vhvkhangg.personalprivatevault.account.snapshot.CreateFollowerSnapshotEntryCommand;
import com.vhvkhangg.personalprivatevault.account.snapshot.FollowerSnapshotOperations;
import com.vhvkhangg.personalprivatevault.account.snapshot.InvalidFollowerSnapshotException;
import com.vhvkhangg.personalprivatevault.account.view.FollowerSnapshotEntryView;
import com.vhvkhangg.personalprivatevault.account.view.FollowerSnapshotView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowerSnapshotService implements FollowerSnapshotOperations {

    private final ExternalAccountRepository externalAccountRepository;
    private final FollowerSnapshotRepository followerSnapshotRepository;
    private final FollowerSnapshotEntryRepository followerSnapshotEntryRepository;

    @Override
    @Transactional
    public FollowerSnapshotView createSnapshot(CreateFollowerSnapshotCommand command) {
        if (command == null) {
            throw new InvalidFollowerSnapshotException("CreateFollowerSnapshotCommand must not be null");
        }
        Long ownerId = command.ownerAccountId();
        if (ownerId == null) {
            throw new InvalidFollowerSnapshotException("Owner account ID must not be null");
        }
        if (externalAccountRepository.findById(ownerId).isEmpty()) {
            throw new InvalidFollowerSnapshotException("Owner account with id " + ownerId + " does not exist");
        }
        if (command.capturedAt() == null) {
            throw new InvalidFollowerSnapshotException("Captured at must not be null");
        }
        if (command.source() == null) {
            throw new InvalidFollowerSnapshotException("Source must not be null");
        }
        if (command.reportedTotalCount() != null && command.reportedTotalCount() < 0) {
            throw new InvalidFollowerSnapshotException("Reported total count must be non-negative");
        }
        String trimmedFileName = trimOrNull(command.importedFileName());
        if (trimmedFileName != null && trimmedFileName.length() > 500) {
            throw new InvalidFollowerSnapshotException("Imported file name must not exceed 500 characters");
        }

        Map<Long, NormalizedEntry> collapsedEntries = new LinkedHashMap<>();
        if (command.entries() != null) {
            for (CreateFollowerSnapshotEntryCommand entry : command.entries()) {
                if (entry == null) {
                    throw new InvalidFollowerSnapshotException("Snapshot entry must not be null");
                }
                Long targetId = entry.targetAccountId();
                if (targetId == null) {
                    throw new InvalidFollowerSnapshotException("Target account ID must not be null");
                }
                if (targetId.equals(ownerId)) {
                    throw new InvalidFollowerSnapshotException("Target account ID must not equal owner account ID");
                }

                String username = trimOrNull(entry.usernameSnapshot());
                String displayName = trimOrNull(entry.displayNameSnapshot());
                String externalId = trimOrNull(entry.externalIdSnapshot());
                String profileUrl = trimOrNull(entry.profileUrlSnapshot());

                if (username != null && username.length() > 255) {
                    throw new InvalidFollowerSnapshotException("Username snapshot must not exceed 255 characters");
                }
                if (displayName != null && displayName.length() > 500) {
                    throw new InvalidFollowerSnapshotException("Display name snapshot must not exceed 500 characters");
                }
                if (externalId != null && externalId.length() > 255) {
                    throw new InvalidFollowerSnapshotException("External ID snapshot must not exceed 255 characters");
                }
                if (profileUrl != null && profileUrl.length() > 2048) {
                    throw new InvalidFollowerSnapshotException("Profile URL snapshot must not exceed 2048 characters");
                }

                NormalizedEntry normalized = new NormalizedEntry(targetId, username, displayName, externalId, profileUrl);
                if (collapsedEntries.containsKey(targetId)) {
                    NormalizedEntry existing = collapsedEntries.get(targetId);
                    if (!existing.matches(normalized)) {
                        throw new InvalidFollowerSnapshotException(
                                "Conflicting historical snapshot entries submitted for target account ID " + targetId
                        );
                    }
                } else {
                    collapsedEntries.put(targetId, normalized);
                }
            }
        }

        // Validate target accounts existence in a single bulk query (eliminates N+1 reads)
        if (!collapsedEntries.isEmpty()) {
            Set<Long> targetIds = collapsedEntries.keySet();
            Set<Long> existingIds = new HashSet<>(externalAccountRepository.findExistingIds(targetIds));
            if (existingIds.size() != targetIds.size()) {
                for (Long targetId : targetIds) {
                    if (!existingIds.contains(targetId)) {
                        throw new InvalidFollowerSnapshotException("Target account with id " + targetId + " does not exist");
                    }
                }
            }
        }

        FollowerSnapshot snapshot = new FollowerSnapshot(
                ownerId,
                command.capturedAt(),
                command.source(),
                command.reportedTotalCount(),
                trimmedFileName,
                Instant.now()
        );
        followerSnapshotRepository.saveAndFlush(snapshot);

        for (NormalizedEntry norm : collapsedEntries.values()) {
            FollowerSnapshotEntry entry = new FollowerSnapshotEntry(
                    snapshot.getId(),
                    norm.targetAccountId(),
                    norm.username(),
                    norm.displayName(),
                    norm.externalId(),
                    norm.profileUrl()
            );
            followerSnapshotEntryRepository.save(entry);
        }
        followerSnapshotEntryRepository.flush();

        return new FollowerSnapshotView(
                snapshot.getId(),
                snapshot.getOwnerAccountId(),
                snapshot.getCapturedAt(),
                snapshot.getSource(),
                snapshot.getReportedTotalCount(),
                snapshot.getImportedFileName(),
                snapshot.getCreatedAt(),
                collapsedEntries.size()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FollowerSnapshotView> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return followerSnapshotRepository.findById(id).map(snapshot -> {
            int entryCount = followerSnapshotEntryRepository.countBySnapshotId(snapshot.getId());
            return toView(snapshot, entryCount);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowerSnapshotView> findRecentByOwner(Long ownerAccountId, int limit) {
        if (ownerAccountId == null) {
            return List.of();
        }
        int clampedLimit = Math.clamp(limit, 1, 100);
        List<FollowerSnapshot> snapshots = followerSnapshotRepository.findRecentByOwnerAccountId(
                ownerAccountId, PageRequest.of(0, clampedLimit)
        );
        if (snapshots.isEmpty()) {
            return List.of();
        }

        List<Long> snapshotIds = snapshots.stream().map(FollowerSnapshot::getId).toList();
        Map<Long, Integer> countMap = followerSnapshotEntryRepository.countGroupedBySnapshotIds(snapshotIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        return snapshots.stream()
                .map(s -> toView(s, countMap.getOrDefault(s.getId(), 0)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowerSnapshotEntryView> findEntriesBySnapshotId(Long snapshotId, int limit) {
        if (snapshotId == null) {
            return List.of();
        }
        int clampedLimit = Math.clamp(limit, 1, 100);
        return followerSnapshotEntryRepository.findBySnapshotId(snapshotId, PageRequest.of(0, clampedLimit))
                .stream()
                .map(this::toEntryView)
                .toList();
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private FollowerSnapshotView toView(FollowerSnapshot snapshot, int entryCount) {
        return new FollowerSnapshotView(
                snapshot.getId(),
                snapshot.getOwnerAccountId(),
                snapshot.getCapturedAt(),
                snapshot.getSource(),
                snapshot.getReportedTotalCount(),
                snapshot.getImportedFileName(),
                snapshot.getCreatedAt(),
                entryCount
        );
    }

    private FollowerSnapshotEntryView toEntryView(FollowerSnapshotEntry entry) {
        return new FollowerSnapshotEntryView(
                entry.getSnapshotId(),
                entry.getTargetAccountId(),
                entry.getUsernameSnapshot(),
                entry.getDisplayNameSnapshot(),
                entry.getExternalIdSnapshot(),
                entry.getProfileUrlSnapshot()
        );
    }

    private record NormalizedEntry(
            Long targetAccountId,
            String username,
            String displayName,
            String externalId,
            String profileUrl
    ) {
        boolean matches(NormalizedEntry other) {
            return Objects.equals(this.username, other.username)
                    && Objects.equals(this.displayName, other.displayName)
                    && Objects.equals(this.externalId, other.externalId)
                    && Objects.equals(this.profileUrl, other.profileUrl);
        }
    }
}
