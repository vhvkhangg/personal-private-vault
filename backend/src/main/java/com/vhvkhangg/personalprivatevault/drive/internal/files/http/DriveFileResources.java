package com.vhvkhangg.personalprivatevault.drive.internal.files.http;

import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileException;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** Two operation admissions, protected owned spool, and bounded operation-owned deadline tasks. */
public class DriveFileResources implements AutoCloseable {
    public static final long MAX_FILE = 52_428_800L;
    public static final long MAX_REQUEST = 57_671_680L;
    public static final int BUFFER = 64 * 1024;
    public record Limits(Duration metadata, Duration transfer, Duration idle, Duration operation) {
        public static Limits production() { return new Limits(Duration.ofSeconds(10), Duration.ofSeconds(120), Duration.ofSeconds(15), Duration.ofSeconds(180)); }
    }
    private final Path spool;
    private final Limits limits;
    private final Semaphore permits = new Semaphore(2);
    private final ScheduledThreadPoolExecutor deadlines = new ScheduledThreadPoolExecutor(2, r -> {
        Thread t = new Thread(r, "ppv-drive-file-deadline"); t.setDaemon(true); return t;
    });
    private final Set<Path> owned = new HashSet<>();
    private int uploadReservations;

    public DriveFileResources(Path spool, Limits limits) {
        this.spool = spool.toAbsolutePath().normalize(); this.limits = limits;
        if (!Files.isDirectory(this.spool, LinkOption.NOFOLLOW_LINKS) || !Files.isWritable(this.spool)) {
            throw new IllegalStateException("Drive file spool must be an existing protected writable directory");
        }
        for (Path p=this.spool; p!=null; p=p.getParent()) {
            if (Files.isSymbolicLink(p) || Files.exists(p.resolve(".git"), LinkOption.NOFOLLOW_LINKS)) {
                throw new IllegalStateException("Drive file spool must be external and contain no symbolic-link ancestor");
            }
        }
        deadlines.setRemoveOnCancelPolicy(true);
    }

    public synchronized Operation open(boolean upload) {
        if (!permits.tryAcquire()) throw DriveFileException.capacity();
        try {
            if (upload) {
                int orphans=0; long bytes=0;
                try (var files=Files.newDirectoryStream(spool, "ppv-gdi26-upload-*")) {
                    for (Path p:files) {
                        if (owned.contains(p)) continue;
                        if (!p.getFileName().toString().matches("ppv-gdi26-upload-[0-9a-f-]{36}\\.tmp")
                                || !Files.isRegularFile(p,LinkOption.NOFOLLOW_LINKS)) throw DriveFileException.capacity();
                        bytes += Files.size(p); orphans++;
                        if (orphans+uploadReservations>=2 || bytes+(uploadReservations+1)*MAX_FILE>2*MAX_FILE) throw DriveFileException.capacity();
                    }
                }
                if (orphans+uploadReservations>=2 || bytes+(uploadReservations+1)*MAX_FILE>2*MAX_FILE) throw DriveFileException.capacity();
                uploadReservations++;
            }
            return new Operation(upload);
        } catch (IOException | RuntimeException e) {
            permits.release();
            if(e instanceof DriveFileException safe) throw safe;
            throw DriveFileException.capacity();
        }
    }

    public int activeOperations() { return 2-permits.availablePermits(); }
    public synchronized int activeSpools() { return owned.size(); }
    public Limits limits() { return limits; }

    public final class Operation implements AutoCloseable {
        private final boolean upload;
        private final long deadline=System.nanoTime()+limits.operation().toNanos();
        private final AtomicBoolean closed=new AtomicBoolean();
        private final AtomicBoolean cancelled=new AtomicBoolean();
        private final AtomicLong progress=new AtomicLong(System.nanoTime());
        private final List<AutoCloseable> streams=new CopyOnWriteArrayList<>();
        private final List<Runnable> cancellation=new CopyOnWriteArrayList<>();
        private final AtomicInteger requests=new AtomicInteger();
        private volatile long phaseDeadline=Long.MAX_VALUE;
        private volatile boolean body;
        private ScheduledFuture<?> timer;
        private Path file;
        private Operation(boolean upload) { this.upload=upload; reschedule(); }

        public void check() {
            long now=System.nanoTime();
            if (cancelled.get() || now>=deadline || now>=phaseDeadline || (body && now-progress.get()>=limits.idle().toNanos())) {
                cancel(); throw DriveFileException.timeout();
            }
        }
        public Duration remaining() { check(); return Duration.ofNanos(deadline-System.nanoTime()); }
        public boolean isCancelled() { return cancelled.get(); }
        public void progress() { progress.set(System.nanoTime()); }
        public synchronized void beginBody() { check(); body=true; progress(); phaseDeadline=System.nanoTime()+limits.transfer().toNanos(); reschedule(); }
        public synchronized void metadataPhase() { check(); body=false; phaseDeadline=System.nanoTime()+limits.metadata().toNanos(); reschedule(); }
        public synchronized void endPhase() { body=false; phaseDeadline=Long.MAX_VALUE; reschedule(); }
        public void countRequest() { check(); if(requests.incrementAndGet()>8) throw new DriveFileException("DRIVE_PROVIDER_FAILURE",502); }
        public int requestCount() { return requests.get(); }
        public void onCancel(Runnable action) { cancellation.add(action); if(cancelled.get()) action.run(); }
        public <T extends AutoCloseable> T track(T stream) {
            if(streams.size()>=32) throw DriveFileException.capacity();
            streams.add(stream);
            if(cancelled.get() || closed.get()) closeQuietly(stream);
            return stream;
        }
        public synchronized Path spoolFile() {
            check(); if(!upload) throw new IllegalStateException("Upload admission required");
            if(file==null) {
                try {
                    file=spool.resolve("ppv-gdi26-upload-"+UUID.randomUUID()+".tmp");
                    Files.createFile(file);
                    synchronized(DriveFileResources.this) { owned.add(file); }
                } catch(IOException e) { throw DriveFileException.capacity(); }
            }
            return file;
        }
        private synchronized void reschedule() {
            if(timer!=null) timer.cancel(false);
            if(closed.get() || cancelled.get()) return;
            long next=Math.min(deadline,phaseDeadline);
            if(body) next=Math.min(next,progress.get()+limits.idle().toNanos());
            timer=deadlines.schedule(() -> {
                try { check(); reschedule(); } catch(DriveFileException expired) { /* check owns cancellation */ }
            },Math.max(1,next-System.nanoTime()),TimeUnit.NANOSECONDS);
        }
        public void cancel() {
            if(!cancelled.compareAndSet(false,true)) return;
            for(Runnable action:cancellation) { try { action.run(); } catch(RuntimeException ignored) { /* terminal cleanup */ } }
            for(var stream:streams) closeQuietly(stream);
        }
        @Override public void close() {
            if(!closed.compareAndSet(false,true)) return;
            synchronized(this) { if(timer!=null) timer.cancel(false); }
            for(var stream:streams) closeQuietly(stream);
            synchronized(DriveFileResources.this) {
                if(file!=null) {
                    try { Files.deleteIfExists(file); } catch(IOException e) { /* retained orphan is counted on next admission */ }
                    owned.remove(file);
                }
                if(upload) uploadReservations--;
                permits.release();
            }
        }
    }
    private void closeQuietly(AutoCloseable value) { try { value.close(); } catch(Exception ignored) { /* original category retained */ } }
    @Override public void close() { deadlines.shutdownNow(); }
}
