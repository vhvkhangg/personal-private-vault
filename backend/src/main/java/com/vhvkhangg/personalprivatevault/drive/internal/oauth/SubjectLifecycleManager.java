package com.vhvkhangg.personalprivatevault.drive.internal.oauth;

import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveCapacityExhaustedException;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveDuplicateSubjectException;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveRevocationPendingException;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveRevokeUncertainException;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.DriveStaleAttemptException;
import com.vhvkhangg.personalprivatevault.drive.internal.persistence.DriveConnectionEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/** Single-process project/subject guard. No guard is held across provider I/O. */
public class SubjectLifecycleManager {
    public static final int MAX_SUBJECT_RECORDS = 2;

    public enum LifecycleState { CONNECTED, REVOKE_IN_FLIGHT, REVOKE_UNCERTAIN, REVOKE_ACCEPTED }

    /** CONNECTED reserves capacity for either durable CONNECTED or RECONNECT_REQUIRED metadata. */
    public record SubjectRecord(String googleSub, short slot, LifecycleState state, long cutoffOrder) {}
    public record RevokeReservation(SubjectRecord prior, RemoteRevocationStatus priorRemote) {}

    private final OAuthAttemptManager attempts;
    private final Map<String, SubjectRecord> subjects = new HashMap<>();
    private final Map<Short, RemoteRevocationStatus> slotRemoteStatus = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public SubjectLifecycleManager(OAuthAttemptManager attempts) { this.attempts = attempts; }

    public void initializeFromDatabase(Collection<DriveConnectionEntity> entities) {
        lock.lock();
        try {
            if (entities.size() > MAX_SUBJECT_RECORDS) throw new IllegalStateException("Invalid Drive slot inventory");
            subjects.clear();
            slotRemoteStatus.clear();
            for (var entity : entities) recordConnected(entity.getSlot(), entity.getGoogleSub());
        } finally { lock.unlock(); }
    }

    public void checkCanAuthorize(short slot) {
        lock.lock();
        try {
            retireCompletedCutoffs();
            var status = slotRemoteStatus.get(slot);
            if (status == RemoteRevocationStatus.PENDING) {
                throw new DriveRevocationPendingException("Revocation is currently in progress for this slot");
            }
            if (status == RemoteRevocationStatus.FAILED || status == RemoteRevocationStatus.UNCERTAIN) {
                throw new DriveRevokeUncertainException("Slot is under revocation quarantine; Owner inspection required");
            }
        } finally { lock.unlock(); }
    }

    public void checkCanCommit(short slot, String sub, long initiationOrder) {
        lock.lock();
        try {
            retireCompletedCutoffs();
            SubjectRecord rec = subjects.get(sub);
            if (rec == null) { checkCapacity(); return; }
            if (rec.state() == LifecycleState.REVOKE_IN_FLIGHT) {
                throw new DriveRevocationPendingException("Remote revocation is currently in flight");
            }
            if (rec.state() == LifecycleState.REVOKE_UNCERTAIN) {
                throw new DriveRevokeUncertainException("Subject is under revocation quarantine; Owner inspection required");
            }
            if (rec.cutoffOrder() > 0 && initiationOrder <= rec.cutoffOrder()) {
                throw new DriveStaleAttemptException("OAuth attempt began before the revocation completed");
            }
            if (rec.state() == LifecycleState.CONNECTED && rec.slot() != slot) {
                throw new DriveDuplicateSubjectException("Verified subject already occupies the other slot");
            }
        } finally { lock.unlock(); }
    }

    public RevokeReservation reserveRevokeFence(short slot, String sub) {
        lock.lock();
        try {
            retireCompletedCutoffs();
            SubjectRecord prior = subjects.get(sub);
            if (prior == null) checkCapacity();
            var reservation = new RevokeReservation(prior, slotRemoteStatus.get(slot));
            subjects.put(sub, new SubjectRecord(sub, slot, LifecycleState.REVOKE_IN_FLIGHT,
                    attempts.currentInitiationOrder()));
            slotRemoteStatus.put(slot, RemoteRevocationStatus.PENDING);
            return reservation;
        } finally { lock.unlock(); }
    }

    public void rollbackRevokeFence(short slot, String sub, RevokeReservation reservation) {
        lock.lock();
        try {
            if (reservation.prior() == null) subjects.remove(sub);
            else subjects.put(sub, reservation.prior());
            if (reservation.priorRemote() == null) slotRemoteStatus.remove(slot);
            else slotRemoteStatus.put(slot, reservation.priorRemote());
        } finally { lock.unlock(); }
    }

    public void completeRevoke(short slot, String sub, RemoteRevocationStatus outcome) {
        lock.lock();
        try {
            // Completion order covers every start during the entire old remote operation, even at equal clock times.
            var state = outcome == RemoteRevocationStatus.REQUEST_ACCEPTED
                    ? LifecycleState.REVOKE_ACCEPTED : LifecycleState.REVOKE_UNCERTAIN;
            subjects.put(sub, new SubjectRecord(sub, slot, state, attempts.currentInitiationOrder()));
            slotRemoteStatus.put(slot, outcome);
            retireCompletedCutoffs();
        } finally { lock.unlock(); }
    }

    public void recordConnected(short slot, String sub) {
        lock.lock();
        try {
            SubjectRecord prior = subjects.get(sub);
            if (prior == null) checkCapacity();
            subjects.put(sub, new SubjectRecord(sub, slot, LifecycleState.CONNECTED,
                    prior == null ? 0 : prior.cutoffOrder()));
            slotRemoteStatus.remove(slot);
        } finally { lock.unlock(); }
    }

    /** Retains uncertain fences indefinitely; only safely drained successful cutoffs can be retired. */
    public void retireCompletedCutoffs() {
        lock.lock();
        try {
            subjects.entrySet().removeIf(e -> e.getValue().state() == LifecycleState.REVOKE_ACCEPTED
                    && !attempts.hasAttemptsAtOrBefore(e.getValue().cutoffOrder()));
            subjects.replaceAll((sub, rec) -> rec.state() == LifecycleState.CONNECTED && rec.cutoffOrder() > 0
                    && !attempts.hasAttemptsAtOrBefore(rec.cutoffOrder())
                    ? new SubjectRecord(sub, rec.slot(), rec.state(), 0) : rec);
        } finally { lock.unlock(); }
    }

    private void checkCapacity() {
        if (subjects.size() >= MAX_SUBJECT_RECORDS) {
            throw new DriveCapacityExhaustedException("Subject lifecycle capacity exhausted");
        }
    }

    public RemoteRevocationStatus getSlotRemoteRevocation(short slot) {
        lock.lock();
        try { return slotRemoteStatus.get(slot); } finally { lock.unlock(); }
    }

    public ReentrantLock getLock() { return lock; }

    /** Narrow current-connection gate reused by file leases, without OAuth initiation/cutoff semantics. */
    public void checkCurrentConnection(short slot, String sub) {
        lock.lock();
        try {
            var record = subjects.get(sub);
            if (record != null && record.state() == LifecycleState.REVOKE_IN_FLIGHT) {
                throw new DriveRevocationPendingException("Remote revocation is currently in flight");
            }
            if (record != null && record.state() == LifecycleState.REVOKE_UNCERTAIN) {
                throw new DriveRevokeUncertainException("Subject is under revocation quarantine; Owner inspection required");
            }
            if (record == null || record.state() != LifecycleState.CONNECTED || record.slot() != slot) {
                throw new DriveStaleAttemptException("Connection no longer holds current subject authority");
            }
        } finally { lock.unlock(); }
    }
}
