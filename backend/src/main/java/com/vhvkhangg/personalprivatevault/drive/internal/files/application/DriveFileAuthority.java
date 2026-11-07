package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import com.vhvkhangg.personalprivatevault.drive.internal.crypto.DriveCredentialCrypto;
import com.vhvkhangg.personalprivatevault.drive.internal.exception.*;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileResources;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.*;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.*;
import com.vhvkhangg.personalprivatevault.drive.internal.persistence.*;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;

/** Drive-private access-token lease. S1 remains the sole credential/fence owner. */
public class DriveFileAuthority {
    public record Lease(short slot,String sub,UUID epoch,String token,Instant expiresAt,DriveFolderRoot root) {
        public Lease withRoot(DriveFolderRoot root) { return new Lease(slot,sub,epoch,token,expiresAt,root); }
        @Override public String toString() { return "DriveFileLease[REDACTED]"; }
    }
    private final DriveConnectionRepository connections;
    private final DriveFolderRootRepository roots;
    private final DriveCredentialCrypto crypto;
    private final SubjectLifecycleManager lifecycle;
    private final DriveOAuthClient oauth;
    private final Clock clock;
    private final TransactionTemplate transactions;
    public DriveFileAuthority(DriveConnectionRepository connections,DriveFolderRootRepository roots,
            DriveCredentialCrypto crypto,SubjectLifecycleManager lifecycle,DriveOAuthClient oauth,Clock clock,
            PlatformTransactionManager transactions) {
        this.connections=connections; this.roots=roots; this.crypto=crypto; this.lifecycle=lifecycle;
        this.oauth=oauth; this.clock=clock; this.transactions=new TransactionTemplate(transactions);
    }
    public static void requireOwner(String owner) { if(!"1".equals(owner)) throw new DriveFileException("DRIVE_OWNER_REQUIRED",403); }

    public Lease acquire(String owner,short slot,DriveFileResources.Operation op) {
        requireOwner(owner);
        var captured=guarded(() -> current(slot,null,null));
        String refresh;
        try {
            if(!crypto.getKeyId().equals(captured.getKeyId())) throw new IllegalArgumentException();
            refresh=crypto.decrypt(slot,captured.getGoogleSub(),captured.getCredentialEpoch(),captured.getEncryptedRefreshToken());
        } catch(RuntimeException e) { throw new DriveFileException("DRIVE_RECONNECT_REQUIRED",409); }
        String scopes=oauth.verifyAndCanonicalizeScopes(captured.getGrantedScopes());
        var remaining=op.remaining();
        var budget=new DriveOAuthClient.CallbackBudget(remaining.compareTo(DriveOAuthClient.CALLBACK_TIMEOUT)<0 ? remaining : DriveOAuthClient.CALLBACK_TIMEOUT);
        DriveTokenResponse response;
        try {
            // The refresh dispatch itself linearizes under S1's guard; token HTTP waits outside it.
            response=oauth.refreshAccessTokenForFiles(refresh,budget,enqueue -> guarded(() -> {
                op.check(); current(slot,captured.getGoogleSub(),captured.getCredentialEpoch()); enqueue.run(); return null;
            }));
            oauth.validateFileRefreshResponse(response,captured.getGoogleSub(),scopes,budget);
        } catch(DriveProviderException e) {
            if("DRIVE_INVALID_GRANT".equals(e.getErrorCode())) {
                invalidate(slot,captured.getGoogleSub(),captured.getCredentialEpoch());
                throw new DriveFileException("DRIVE_RECONNECT_REQUIRED",409);
            }
            throw new DriveFileException(e.getHttpStatus()==504 ? "DRIVE_PROVIDER_TIMEOUT" : "DRIVE_PROVIDER_FAILURE",e.getHttpStatus()==504 ? 504 : 502);
        }
        return guarded(() -> {
            op.check();
            var current=current(slot,captured.getGoogleSub(),captured.getCredentialEpoch());
            UUID epoch=current.getCredentialEpoch();
            if(response.refreshToken()!=null) {
                UUID next=UUID.randomUUID();
                UUID expected=epoch;
                byte[] encrypted=crypto.encrypt(slot,current.getGoogleSub(),next,response.refreshToken());
                int changed=transaction(() -> connections.updateWithEpochCas(slot,expected,next,DriveConnectionStatus.CONNECTED,
                        encrypted,crypto.getKeyId(),current.getGrantedScopes(),current.getDisplayEmail(),clock.instant()));
                if(changed!=1) throw DriveFileException.stale();
                epoch=next;
            }
            return new Lease(slot,current.getGoogleSub(),epoch,response.accessToken(),clock.instant().plusSeconds(response.expiresIn()),null);
        });
    }

    private DriveConnectionEntity current(short slot,String expectedSub,UUID expectedEpoch) {
        var row=connections.findById(slot).orElseThrow(() -> expectedEpoch==null
                ? new DriveFileException("DRIVE_CONNECTION_REQUIRED",409) : DriveFileException.stale());
        if(expectedEpoch!=null && (!row.getCredentialEpoch().equals(expectedEpoch) || !row.getGoogleSub().equals(expectedSub))) throw DriveFileException.stale();
        if(row.getStatus()!=DriveConnectionStatus.CONNECTED) throw new DriveFileException("DRIVE_RECONNECT_REQUIRED",409);
        lifecycle.checkCurrentConnection(slot,row.getGoogleSub());
        return row;
    }
    public void verify(Lease lease,DriveFileResources.Operation op) {
        guarded(() -> { verifyGuarded(lease,op); return null; });
    }
    private void verifyGuarded(Lease lease,DriveFileResources.Operation op) {
        op.check(); current(lease.slot(),lease.sub(),lease.epoch());
        if(!clock.instant().isBefore(lease.expiresAt())) throw new DriveFileException("DRIVE_RECONNECT_REQUIRED",409);
        if(lease.root()!=null) {
            var expected=lease.root();
            var root=roots.findById(lease.sub()).orElseThrow(DriveFileException::stale);
            if(!root.getRootBindingId().equals(expected.getRootBindingId()) || !root.getRootFileId().equals(expected.getRootFileId())
                    || !root.getBindingEpoch().equals(expected.getBindingEpoch()) || !root.getStatus().equals(expected.getStatus())) throw DriveFileException.stale();
        }
    }
    public <T> T dispatch(Lease lease,DriveFileResources.Operation op,Supplier<T> enqueue) {
        return guarded(() -> { verifyGuarded(lease,op); op.countRequest(); return enqueue.get(); });
    }
    public void invalidate(Lease lease) { invalidate(lease.slot(),lease.sub(),lease.epoch()); }
    private void invalidate(short slot,String sub,UUID epoch) {
        guarded(() -> {
            var current=connections.findById(slot);
            if(current.isPresent() && current.get().getGoogleSub().equals(sub)) {
                transaction(() -> connections.downgradeToReconnectRequiredWithCas(slot,epoch,UUID.randomUUID(),clock.instant()));
            }
            return null;
        });
    }
    public <T> T finalWrite(Lease lease,DriveFileResources.Operation op,Supplier<T> write) {
        return guarded(() -> { verifyGuarded(lease,op); return transaction(write); });
    }
    public <T> T transaction(Supplier<T> work) {
        try { return transactions.execute(status -> work.get()); }
        catch(DataAccessException | TransactionException e) { throw new DriveFileException("DRIVE_FILE_PERSISTENCE_FAILURE",500); }
    }
    private <T> T guarded(Supplier<T> work) {
        lifecycle.getLock().lock();
        try { return work.get(); } finally { lifecycle.getLock().unlock(); }
    }
}
