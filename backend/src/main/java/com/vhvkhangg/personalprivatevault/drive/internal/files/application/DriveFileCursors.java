package com.vhvkhangg.personalprivatevault.drive.internal.files.application;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

/** Bounded process-local continuation handles; neither provider tokens nor handles grant authority. */
public class DriveFileCursors {
    private record Entry(short slot,String sub,UUID credential,UUID root,UUID rootEpoch,int limit,String provider,Instant expiry) {
        @Override public String toString(){return "DriveCursor[REDACTED]";}
    }
    private final Clock clock;
    private final Map<String,Entry> entries=new HashMap<>();
    private final SecureRandom random=new SecureRandom();
    public DriveFileCursors(Clock clock){this.clock=clock;}
    public synchronized String providerToken(String cursor,DriveFileAuthority.Lease lease,int limit) {
        purge();
        if(cursor==null) return null;
        var value=entries.get(cursor);
        if(cursor.length()!=43 || value==null || value.slot()!=lease.slot() || !value.sub().equals(lease.sub())
                || !value.credential().equals(lease.epoch()) || !value.root().equals(lease.root().getRootBindingId())
                || !value.rootEpoch().equals(lease.root().getBindingEpoch()) || value.limit()!=limit) throw invalid();
        return value.provider();
    }
    public synchronized String issue(String provider,DriveFileAuthority.Lease lease,int limit) {
        if(provider==null) return null;
        if(provider.isBlank() || provider.getBytes(StandardCharsets.UTF_8).length>2048) throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502);
        purge(); if(entries.size()>=128) throw DriveFileException.capacity();
        byte[] bytes=new byte[32]; random.nextBytes(bytes); String cursor=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        entries.put(cursor,new Entry(lease.slot(),lease.sub(),lease.epoch(),lease.root().getRootBindingId(),lease.root().getBindingEpoch(),limit,provider,clock.instant().plusSeconds(300)));
        return cursor;
    }
    private void purge(){var now=clock.instant();entries.values().removeIf(e -> !now.isBefore(e.expiry()));}
    private DriveFileException invalid(){return new DriveFileException("DRIVE_CURSOR_INVALID",400);}
}
