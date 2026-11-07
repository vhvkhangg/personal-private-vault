package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Bounded durable action identity; the live set owns claims only, never provider outcome knowledge. */
public class DriveUploadIntentStore {
    private final DriveFileAuthority authority;
    private final DriveFolderRootRepository roots;
    private final DriveUploadIntentRepository intents;
    private final Clock clock;
    private final Set<DriveUploadKey> live=ConcurrentHashMap.newKeySet();
    public DriveUploadIntentStore(DriveFileAuthority authority,DriveFolderRootRepository roots,DriveUploadIntentRepository intents,Clock clock) {
        this.authority=authority;this.roots=roots;this.intents=intents;this.clock=clock;
    }
    public final class Claim implements AutoCloseable {
        private DriveUploadIntent intent;
        private Claim(DriveUploadIntent intent){this.intent=intent;}
        public DriveUploadIntent intent(){return intent;}
        @Override public void close(){live.remove(intent.getId());}
        @Override public String toString(){return "DriveUploadClaim[REDACTED]";}
    }
    public Claim take(DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String key,DriveMultipartUpload.Spool spool) {
        DriveFileInputs.keyTime(key);
        var id=new DriveUploadKey(lease.sub(),lease.root().getRootBindingId(),key);
        var acquired=new java.util.concurrent.atomic.AtomicBoolean();
        try {
            return authority.finalWrite(lease,op,() -> {
                roots.lockBySubject(lease.sub()).orElseThrow(DriveFileException::stale);
                var prior=intents.findById(id);
                if(prior.isPresent() && !prior.get().getRequestFingerprint().equals(spool.fingerprint())) throw new DriveFileException("UPLOAD_KEY_CONFLICT",409);
                if(live.contains(id)) throw new DriveFileException("UPLOAD_IN_PROGRESS",409);
                if(prior.isEmpty()) {
                    DriveFileInputs.freshKey(key,clock.instant());
                    var protectedKeys=live.stream().map(DriveUploadKey::getIdempotencyKey).toList();
                    intents.pruneTerminal(lease.sub(),id.getRootBindingId(),clock.instant().minus(Duration.ofDays(30)),protectedKeys.isEmpty() ? List.of("") : protectedKeys);
                    if(intents.countNonterminal(lease.sub(),id.getRootBindingId())>=8 || intents.countRetained(lease.sub(),id.getRootBindingId())>=1024) throw DriveFileException.capacity();
                    if(intents.insertPreparing(lease.sub(),id.getRootBindingId(),key,spool.fingerprint(),spool.md5(),spool.size(),spool.name(),spool.mime(),UUID.randomUUID(),lease.root().getBindingEpoch(),clock.instant())!=1) throw new DriveFileException("UPLOAD_KEY_CONFLICT",409);
                } else {
                    if(prior.get().getStatus().equals("FAILED_DEFINITIVE")) throw new DriveFileException("UPLOAD_FAILED",409);
                    if(intents.claim(id,prior.get().getIntentEpoch(),UUID.randomUUID(),lease.root().getBindingEpoch(),clock.instant())!=1) throw DriveFileException.stale();
                }
                live.add(id);acquired.set(true);
                return new Claim(intents.findById(id).orElseThrow(DriveFileException::stale));
            });
        } catch(RuntimeException e) { if(acquired.get())live.remove(id); throw e; }
    }
    public void reserve(Claim claim,DriveFileAuthority.Lease lease,DriveFileResources.Operation op,String providerId) {
        DriveFileInputs.generatedId(providerId);
        claim.intent=authority.finalWrite(lease,op,() -> {
            var old=claim.intent;
            if(intents.reserve(old.getId(),old.getIntentEpoch(),UUID.randomUUID(),providerId,clock.instant())!=1) throw DriveFileException.stale();
            return intents.findById(old.getId()).orElseThrow(DriveFileException::stale);
        });
    }
    public void dispatched(Claim claim,DriveFileAuthority.Lease lease,DriveFileResources.Operation op) {
        claim.intent=authority.finalWrite(lease,op,() -> {
            if(!claim.intent.getStatus().equals("RESERVED")) throw DriveFileException.stale();
            return transition(claim.intent,"DISPATCHED",null);
        });
    }
    public void complete(Claim claim,DriveFileAuthority.Lease lease,DriveFileResources.Operation op) {
        claim.intent=authority.finalWrite(lease,op,() -> claim.intent.getStatus().equals("COMPLETED") ? claim.intent : transition(claim.intent,"COMPLETED",clock.instant()));
    }
    public void unknown(Claim claim) {
        if(!Set.of("DISPATCHED","RECONCILIATION_REQUIRED").contains(claim.intent.getStatus())) return;
        // This records uncertainty only, never usable authority; it may survive local disconnect.
        authority.transaction(() -> {
            var old=claim.intent;
            intents.transition(old.getId(),old.getIntentEpoch(),UUID.randomUUID(),"RECONCILIATION_REQUIRED",clock.instant(),null);
            return null;
        });
    }
    private DriveUploadIntent transition(DriveUploadIntent old,String status,Instant completed) {
        if(intents.transition(old.getId(),old.getIntentEpoch(),UUID.randomUUID(),status,clock.instant(),completed)!=1) throw DriveFileException.stale();
        return intents.findById(old.getId()).orElseThrow(DriveFileException::stale);
    }
}
