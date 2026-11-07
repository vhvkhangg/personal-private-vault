package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.*;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** One durable root creation identity per subject. Existing pending roots are GET-only recovery. */
public class DriveFolderService {
    private final DriveFileAuthority authority;
    private final DriveFileHttpClient provider;
    private final DriveFolderRootRepository roots;
    private final Clock clock;
    private final Set<String> live=ConcurrentHashMap.newKeySet();
    public DriveFolderService(DriveFileAuthority authority,DriveFileHttpClient provider,DriveFolderRootRepository roots,Clock clock) {
        this.authority=authority;this.provider=provider;this.roots=roots;this.clock=clock;
    }
    public DriveFileAuthority.Lease ensure(DriveFileAuthority.Lease lease,DriveFileResources.Operation op) {
        final String sub=lease.sub();
        if(!live.add(lease.sub())) throw new DriveFileException("DRIVE_ROOT_PENDING",409);
        boolean mutating=false;
        try {
            var existing=authority.finalWrite(lease,op,() -> roots.findById(sub));
            String accountRoot=provider.accountRoot(lease,op);
            DriveFolderRoot root;
            if(existing.isEmpty()) {
                String id=provider.generateId(lease,op);
                UUID binding=UUID.randomUUID();
                root=authority.finalWrite(lease,op,() -> {
                    if(roots.insertPending(sub,id,binding,UUID.randomUUID(),clock.instant())!=1) throw new DriveFileException("DRIVE_ROOT_PENDING",409);
                    return roots.findById(sub).orElseThrow(DriveFileException::stale);
                });
                lease=lease.withRoot(root); mutating=true;
                var metadata=provider.createRoot(lease,op,id,binding);
                DriveFileProof.root(metadata,root,accountRoot);
            } else {
                root=existing.get(); lease=lease.withRoot(root);
                var metadata=provider.root(lease,op,root.getRootFileId());
                DriveFileProof.root(metadata,root,accountRoot);
            }
            if(!root.getStatus().equals("READY")) {
                var captured=lease;
                root=authority.finalWrite(captured,op,() -> {
                    if(roots.transition(captured.sub(),captured.root().getBindingEpoch(),UUID.randomUUID(),"READY",clock.instant())!=1) throw DriveFileException.stale();
                    return roots.findById(captured.sub()).orElseThrow(DriveFileException::stale);
                });
                lease=lease.withRoot(root);
            }
            authority.verify(lease,op); return lease;
        } catch(DriveFileException e) {
            if(mutating) throw new DriveFileException("DRIVE_ROOT_OUTCOME_UNKNOWN",409);
            var retained=roots.findById(lease.sub());
            if(retained.isPresent() && !retained.get().getStatus().equals("READY")) throw new DriveFileException("DRIVE_ROOT_OUTCOME_UNKNOWN",409);
            if(retained.isPresent() && e.getErrorCode().equals("DRIVE_FILE_UNAVAILABLE")) throw new DriveFileException("DRIVE_ROOT_UNAVAILABLE",409);
            throw e;
        } finally {live.remove(lease.sub());}
    }
    public DriveFileAuthority.Lease ready(DriveFileAuthority.Lease lease,DriveFileResources.Operation op) {
        var root=authority.finalWrite(lease,op,() -> roots.findById(lease.sub()).filter(r -> r.getStatus().equals("READY"))
                .orElseThrow(() -> new DriveFileException("DRIVE_ROOT_NOT_READY",409)));
        var bound=lease.withRoot(root);
        String accountRoot=provider.accountRoot(bound,op);
        try { DriveFileProof.root(provider.root(bound,op,root.getRootFileId()),root,accountRoot); }
        catch(DriveFileException e) {
            if(e.getErrorCode().equals("DRIVE_FILE_UNAVAILABLE")) throw new DriveFileException("DRIVE_ROOT_UNAVAILABLE",409);
            throw e;
        }
        authority.verify(bound,op); return bound;
    }
}
