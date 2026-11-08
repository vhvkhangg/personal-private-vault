package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileResources;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.*;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Bounded durable child folder action identity; live set owns claims only. */
public class DriveChildFolderIntentStore {
    private final DriveFileAuthority authority;
    private final DriveFolderRootRepository roots;
    private final DriveChildFolderIntentRepository intents;
    private final Clock clock;
    private final Set<DriveChildFolderKey> live = ConcurrentHashMap.newKeySet();

    public DriveChildFolderIntentStore(DriveFileAuthority authority, DriveFolderRootRepository roots,
                                       DriveChildFolderIntentRepository intents, Clock clock) {
        this.authority = authority;
        this.roots = roots;
        this.intents = intents;
        this.clock = clock;
    }

    public final class Claim implements AutoCloseable {
        private DriveChildFolderIntent intent;

        private Claim(DriveChildFolderIntent intent) {
            this.intent = intent;
        }

        public DriveChildFolderIntent intent() {
            return intent;
        }

        @Override
        public void close() {
            live.remove(intent.getId());
        }

        @Override
        public String toString() {
            return "DriveChildFolderClaim[REDACTED]";
        }
    }

    public Claim take(DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String key, String safeName) {
        DriveFileInputs.folderKeyTime(key);
        String expectedFingerprint = DriveFileInputs.folderFingerprint(
                lease.sub(), lease.root().getRootBindingId(), lease.root().getRootFileId(), safeName, key);
        var id = new DriveChildFolderKey(lease.sub(), lease.root().getRootBindingId(), key);
        var acquired = new AtomicBoolean();
        try {
            return authority.finalWrite(lease, op, () -> {
                roots.lockBySubject(lease.sub()).orElseThrow(DriveFileException::stale);
                var prior = intents.findById(id);
                if (prior.isPresent()) {
                    if (!prior.get().getRequestFingerprint().equals(expectedFingerprint)
                            || !prior.get().getSafeName().equals(safeName)) {
                        throw new DriveFileException("FOLDER_KEY_CONFLICT", 409);
                    }
                    if (live.contains(id)) {
                        throw new DriveFileException("FOLDER_IN_PROGRESS", 409);
                    }
                    if (prior.get().getStatus().equals("FAILED_DEFINITIVE")) {
                        throw new DriveFileException("FOLDER_FAILED", 409);
                    }
                } else {
                    DriveFileInputs.freshFolderKey(key, clock.instant());
                    if (intents.countNonterminal(lease.sub(), id.getRootBindingId()) >= 8
                            || intents.countRetained(lease.sub(), id.getRootBindingId()) >= 256) {
                        throw DriveFileException.capacity();
                    }
                    UUID intentEpoch = UUID.randomUUID();
                    if (intents.insertPreparing(lease.sub(), id.getRootBindingId(), key,
                            lease.root().getBindingEpoch(), intentEpoch, safeName, expectedFingerprint,
                            clock.instant()) != 1) {
                        throw new DriveFileException("FOLDER_KEY_CONFLICT", 409);
                    }
                }
                live.add(id);
                acquired.set(true);
                return new Claim(intents.findById(id).orElseThrow(DriveFileException::stale));
            });
        } catch (RuntimeException e) {
            if (acquired.get()) live.remove(id);
            throw e;
        }
    }

    public void reserve(Claim claim, DriveFileAuthority.Lease lease, DriveFileResources.Operation op, String providerId) {
        DriveFileInputs.generatedId(providerId);
        claim.intent = authority.finalWrite(lease, op, () -> {
            var old = claim.intent;
            if (intents.reserve(old.getId(), old.getIntentEpoch(), providerId, clock.instant()) != 1) {
                throw DriveFileException.stale();
            }
            return intents.findById(old.getId()).orElseThrow(DriveFileException::stale);
        });
    }

    public void dispatched(Claim claim, DriveFileAuthority.Lease lease, DriveFileResources.Operation op) {
        claim.intent = authority.finalWrite(lease, op, () -> {
            var old = claim.intent;
            if (!old.getStatus().equals("RESERVED")) throw DriveFileException.stale();
            if (intents.transitionFrom(old.getId(), old.getIntentEpoch(), "RESERVED", "DISPATCHED", clock.instant(), null) != 1) {
                throw DriveFileException.stale();
            }
            return intents.findById(old.getId()).orElseThrow(DriveFileException::stale);
        });
    }

    public void complete(Claim claim, DriveFileAuthority.Lease lease, DriveFileResources.Operation op) {
        claim.intent = authority.finalWrite(lease, op, () -> {
            var old = claim.intent;
            if (old.getStatus().equals("COMPLETED")) return old;
            if (intents.transitionFromAny(old.getId(), old.getIntentEpoch(), List.of("DISPATCHED", "RECONCILIATION_REQUIRED"), "COMPLETED", clock.instant(), clock.instant()) != 1) {
                throw DriveFileException.stale();
            }
            return intents.findById(old.getId()).orElseThrow(DriveFileException::stale);
        });
    }

    public void unknown(Claim claim) {
        if (!Set.of("DISPATCHED", "RECONCILIATION_REQUIRED").contains(claim.intent.getStatus())) return;
        authority.transaction(() -> {
            var old = claim.intent;
            intents.transitionFromAny(old.getId(), old.getIntentEpoch(), List.of("DISPATCHED", "RECONCILIATION_REQUIRED"), "RECONCILIATION_REQUIRED", clock.instant(), null);
            return null;
        });
    }
}
