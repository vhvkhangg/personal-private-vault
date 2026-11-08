package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.vhvkhangg.personalprivatevault.drive.internal.files.http.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderIntentRepository;
import java.util.*;

/** S2 and H1-B file operations over S1 leases, durable root, and bounded upload ledgers. */
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
    private final DriveChildFolderIntentRepository childFolderRepository;

    public DriveFileService(DriveFileAuthority authority,DriveFolderService folders,DriveFileHttpClient provider,DriveFileCursors cursors,DriveUploadIntentStore uploads) {
        this(authority,folders,provider,cursors,uploads,null);
    }

    public DriveFileService(DriveFileAuthority authority,DriveFolderService folders,DriveFileHttpClient provider,DriveFileCursors cursors,DriveUploadIntentStore uploads,DriveChildFolderIntentRepository childFolderRepository) {
        this.authority=authority;this.folders=folders;this.provider=provider;this.cursors=cursors;this.uploads=uploads;this.childFolderRepository=childFolderRepository;
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
            try { projected.add(DriveFileProof.rootFile(file,lease.root(),DriveFileProof.text(file,"id",255),null)); }
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
                    var result=DriveFileProof.rootFile(provider.file(lease,op,claim.intent().getProviderFileId(),true),lease.root(),claim.intent().getProviderFileId(),claim.intent());
                    uploads.complete(claim,lease,op); return new Result<>(result,lease,false);
                }
                if(state.equals("PREPARING")) uploads.reserve(claim,lease,op,provider.generateId(lease,op));
                uploads.dispatched(claim,lease,op); dispatched=true;
                var result=DriveFileProof.rootFile(provider.upload(lease,op,spool,claim.intent().getProviderFileId(),lease.root().getRootBindingId(),DriveFileInputs.action(key)),
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

    public Result<DriveFileProof.FileItem> uploadChild(String owner,short slot,String childId,String key,DriveMultipartUpload.Spool spool,DriveFileResources.Operation op) {
        DriveFileInputs.keyTime(key);
        DriveFileInputs.id(childId);
        var lease=folders.ready(authority.acquire(owner,slot,op),op);
        if(childFolderRepository==null) throw DriveFileProof.unavailable();
        var childOpt=childFolderRepository.findBySubAndRootAndProviderFolderId(
                lease.sub(),lease.root().getRootBindingId(),childId)
                .filter(i -> "COMPLETED".equals(i.getStatus()));
        if(childOpt.isEmpty()) throw DriveFileProof.unavailable();
        var remoteChild=provider.childFolder(lease,op,childId);
        DriveFileProof.childFolder(remoteChild,lease.root(),childId,childOpt.get());

        try(var claim=uploads.take(lease,op,key,spool,childId)) {
            boolean dispatched=false;
            try {
                var state=claim.intent().getStatus();
                if(Set.of("DISPATCHED","RECONCILIATION_REQUIRED","COMPLETED").contains(state)) {
                    var result=DriveFileProof.childFile(provider.file(lease,op,claim.intent().getProviderFileId(),true),lease.root(),childId,claim.intent().getProviderFileId(),claim.intent());
                    uploads.complete(claim,lease,op); return new Result<>(result,lease,false);
                }
                if(state.equals("PREPARING")) uploads.reserve(claim,lease,op,provider.generateId(lease,op));
                uploads.dispatched(claim,lease,op); dispatched=true;
                var result=DriveFileProof.childFile(provider.upload(lease,op,spool,claim.intent().getProviderFileId(),lease.root().getRootBindingId(),DriveFileInputs.action(key),childId),
                        lease.root(),childId,claim.intent().getProviderFileId(),claim.intent());
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
        var node=provider.file(lease,op,fileId,false);
        DriveFileProof.FileItem result;
        try {
            result=DriveFileProof.rootFile(node,lease.root(),fileId,null);
        } catch(DriveFileException notRoot) {
            var parents=node.get("parents");
            if(parents==null || !parents.isArray() || parents.size()!=1 || !parents.get(0).isTextual()) {
                throw notRoot;
            }
            String candidateParentId=parents.get(0).textValue();
            if(childFolderRepository==null) throw DriveFileProof.unavailable();
            var childOpt=childFolderRepository.findBySubAndRootAndProviderFolderId(
                    lease.sub(),lease.root().getRootBindingId(),candidateParentId)
                    .filter(i -> "COMPLETED".equals(i.getStatus()));
            if(childOpt.isEmpty()) throw DriveFileProof.unavailable();
            var remoteChild=provider.childFolder(lease,op,candidateParentId);
            DriveFileProof.childFolder(remoteChild,lease.root(),candidateParentId,childOpt.get());
            result=DriveFileProof.childFile(node,lease.root(),candidateParentId,fileId,null);
        }
        authority.verify(lease,op); return new Result<>(result,lease,false);
    }

    public DriveFileHttpClient.Media media(Result<DriveFileProof.FileItem> result,DriveFileResources.Operation op) {
        var media=provider.media(result.lease(),op,result.data().id());
        if(media.declaredLength()!=-1 && media.declaredLength()!=result.data().sizeBytes()) throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
        return media;
    }

    public void verify(Result<?> result,DriveFileResources.Operation op){authority.verify(result.lease(),op);}
}
