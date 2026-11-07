package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import java.util.*;

/** Four S2 use cases over S1 leases, one durable root and one bounded upload-action ledger. */
public class DriveFileService {
    public record RootStatus(int slot,String status) {}
    public record FilePage(List<DriveFileProof.FileItem> files,String nextCursor,boolean hasMore) {}
    public record Result<T>(T data,DriveFileAuthority.Lease lease,boolean created) {
        @Override public String toString(){return "DriveFileResult[REDACTED]";}
    }
    private final DriveFileAuthority authority;
    private final DriveFolderService folders;
    private final DriveFileHttpClient provider;
    private final DriveFileCursors cursors;
    private final DriveUploadIntentStore uploads;
    public DriveFileService(DriveFileAuthority authority,DriveFolderService folders,DriveFileHttpClient provider,DriveFileCursors cursors,DriveUploadIntentStore uploads) {
        this.authority=authority;this.folders=folders;this.provider=provider;this.cursors=cursors;this.uploads=uploads;
    }
    public Result<RootStatus> ensure(String owner,short slot,DriveFileResources.Operation op) {
        var lease=folders.ensure(authority.acquire(owner,slot,op),op);
        return new Result<>(new RootStatus(slot,"READY"),lease,false);
    }
    public Result<FilePage> list(String owner,short slot,int limit,String cursor,DriveFileResources.Operation op) {
        if(limit<1 || limit>50) throw new DriveFileException("DRIVE_INVALID_LIMIT",400);
        if(cursor!=null && (cursor.length()!=43 || !cursor.matches("[A-Za-z0-9_-]{43}"))) throw new DriveFileException("DRIVE_CURSOR_INVALID",400);
        var lease=folders.ready(authority.acquire(owner,slot,op),op);
        String token=cursors.providerToken(cursor,lease,limit);
        var page=provider.list(lease,op,lease.root().getRootFileId(),limit,token);
        var files=page.get("files");
        if(files==null || !files.isArray() || files.size()>limit) throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
        var projected=new ArrayList<DriveFileProof.FileItem>();
        for(var file:files) {
            try { projected.add(DriveFileProof.file(file,lease.root(),DriveFileProof.text(file,"id",255),null)); }
            catch(DriveFileException denied) { /* an unapproved candidate is omitted, never individually fetched */ }
        }
        String next=null;
        if(page.has("nextPageToken")) {
            try {next=DriveFileProof.text(page,"nextPageToken",2048);}
            catch(DriveFileException invalid){throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);}
        }
        String continuation=next;
        String handle=authority.finalWrite(lease,op,() -> cursors.issue(continuation,lease,limit));
        return new Result<>(new FilePage(List.copyOf(projected),handle,handle!=null),lease,false);
    }
    public Result<DriveFileProof.FileItem> upload(String owner,short slot,String key,DriveMultipartUpload.Spool spool,DriveFileResources.Operation op) {
        DriveFileInputs.keyTime(key);
        var lease=folders.ready(authority.acquire(owner,slot,op),op);
        try(var claim=uploads.take(lease,op,key,spool)) {
            boolean dispatched=false;
            try {
                var state=claim.intent().getStatus();
                if(Set.of("DISPATCHED","RECONCILIATION_REQUIRED","COMPLETED").contains(state)) {
                    var result=DriveFileProof.file(provider.file(lease,op,claim.intent().getProviderFileId(),true),lease.root(),claim.intent().getProviderFileId(),claim.intent());
                    uploads.complete(claim,lease,op); return new Result<>(result,lease,false);
                }
                if(state.equals("PREPARING")) uploads.reserve(claim,lease,op,provider.generateId(lease,op));
                uploads.dispatched(claim,lease,op); dispatched=true;
                var result=DriveFileProof.file(provider.upload(lease,op,spool,claim.intent().getProviderFileId(),lease.root().getRootBindingId(),DriveFileInputs.action(key)),
                        lease.root(),claim.intent().getProviderFileId(),claim.intent());
                uploads.complete(claim,lease,op); return new Result<>(result,lease,true);
            } catch(RuntimeException error) {
                if(dispatched || Set.of("DISPATCHED","RECONCILIATION_REQUIRED","COMPLETED").contains(claim.intent().getStatus())) {
                    try {uploads.unknown(claim);}catch(DriveFileException persistence){/* durable DISPATCHED already retains the original identity */}
                    throw new DriveFileException("UPLOAD_OUTCOME_UNKNOWN",409);
                }
                throw error;
            }
        }
    }
    public Result<DriveFileProof.FileItem> download(String owner,short slot,String fileId,DriveFileResources.Operation op) {
        DriveFileInputs.id(fileId);
        var lease=folders.ready(authority.acquire(owner,slot,op),op);
        var result=DriveFileProof.file(provider.file(lease,op,fileId,false),lease.root(),fileId,null);
        authority.verify(lease,op); return new Result<>(result,lease,false);
    }
    public DriveFileHttpClient.Media media(Result<DriveFileProof.FileItem> result,DriveFileResources.Operation op) {
        var media=provider.media(result.lease(),op,result.data().id());
        if(media.declaredLength()!=-1 && media.declaredLength()!=result.data().sizeBytes()) throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
        return media;
    }
    public void verify(Result<?> result,DriveFileResources.Operation op){authority.verify(result.lease(),op);}
}
