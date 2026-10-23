package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagJobLease;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.RagEngineClient;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagJobLeaseRepository;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/** Single-host supervisor. A timeout or disconnected HTTP exchange is never a death certificate. */
@Component
public class RagWorkerSupervisor {
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final RagJobLeaseRepository leases;
    private final RagSourceAuthority authority;
    private final RagRuntimeSecurity security;
    private final RagRuntimeIdentity runtime;
    private final RagEngineClient client;
    private final EntityManager entities;
    private final TransactionTemplate tx;
    private final boolean enabled;
    private final String executable, launcher, directory, dsn;
    private static final Duration STOP_LIMIT = Duration.ofSeconds(10);

    public RagWorkerSupervisor(RagJobLeaseRepository leases, RagSourceAuthority authority,
            RagRuntimeSecurity security, RagRuntimeIdentity runtime, RagEngineClient client,
            EntityManager entities, PlatformTransactionManager transactions,
            @Value("${ppv.rag.test-scoped-execution:false}") boolean enabled,
            @Value("${ppv.rag.worker.executable:}") String executable,
            @Value("${ppv.rag.worker.venv-launcher:}") String launcher,
            @Value("${ppv.rag.worker.working-directory:}") String directory,
            @Value("${ppv.rag.worker.dsn:}") String dsn) {
        this.leases=leases; this.authority=authority; this.security=security; this.runtime=runtime;
        this.client=client; this.entities=entities; this.enabled=enabled; this.executable=executable;
        this.launcher=launcher; this.directory=directory; this.dsn=dsn;
        tx=new TransactionTemplate(transactions); tx.setTimeout(10);
    }

    private static final class Session {
        final UUID nonce=UUID.randomUUID();
        boolean stopping, ready, dispatching, returned, stopped;
        NativeWorkerProcess process;
        boolean verifying;
        NativeWorkerProcess verifier;
        Runnable verificationCleanup;
    }

    /** Only the invocation that acquired the dispatch slot may abort its failed startup. */
    public static final class StartupFailure extends IllegalStateException {
        StartupFailure(RuntimeException cause) { super("Owned worker startup failed",cause); }
    }

    /** Must cover the complete launcher and synchronous sender scope, including every exceptional exit. */
    public void begin(UUID job, UUID attempt) {
        outsideTransaction();
        if (!enabled) throw new IllegalStateException("Worker execution is contained pending mandatory verification");
        security.requireIsolatedRuntime();
        var initial=leases.findByJobIdAndAttemptId(job,attempt).orElseThrow();
        if(!isCurrentController(initial) || !"ADMITTED".equals(initial.getStatus()) || initial.getWorkerIdentity()!=null)
            throw new IllegalStateException("Job cannot be launched again");
        var session=sessions.computeIfAbsent(job, ignored -> new Session());
        synchronized(session) {
            if (session.stopping || session.dispatching || session.returned) throw new IllegalStateException("Job dispatch is single use");
            session.dispatching=true;
        }
        boolean isBatchV3 = initial.getDependencyManifest() != null && initial.getDependencyManifest().contains("\"contract_version\":\"3.0\"");
        try {
            var prepared = isBatchV3 ? null : authority.prepareSource(initial.getSourceId(), Map.of());
            var preparedBatch = isBatchV3 ? authority.prepareBatch(job, attempt) : Map.<Long, RagSourceAuthority.Prepared>of();
            tx.executeWithoutResult(status -> {
                var proof = isBatchV3 ? authority.lockPreparedBatchAttempt(job, attempt, preparedBatch)
                        : authority.lockPreparedAttempt(job, attempt, prepared, false);
                if (!"ADMITTED".equals(proof.lease().getStatus()) || proof.lease().getWorkerIdentity() != null)
                    throw new IllegalStateException("Job cannot be launched again");
                proof.lease().updateStatus("STARTING", authority.databaseTime());
                entities.flush();
            });
            checkRunning(session);
            Path work = requiredPath(directory), binary = requiredPath(executable), venv = requiredPath(launcher);
            var environment = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
            for (String name : List.of("SystemRoot", "WINDIR", "TEMP", "TMP", "LOCALAPPDATA", "PATH")) {
                String value = System.getenv(name); if (value != null) environment.put(name, value);
            }
            environment.put("__PYVENV_LAUNCHER__", venv.toString());
            environment.put("PYTHONPATH", work.resolve("src").toString());
            environment.put("PYTHONNOUSERSITE", "1"); environment.put("PYTHONDONTWRITEBYTECODE", "1");
            environment.put("PPV_RAG_SERVICE_KEY", client.serviceCredential());
            environment.put("PPV_RAG_WORKER_DSN", dsn);
            environment.put("PPV_RAG_HOST", "127.0.0.1"); environment.put("PPV_RAG_PORT", Integer.toString(client.port()));
            environment.put("PPV_RAG_TEST_SCOPED_EXECUTION", "true");
            environment.put("PPV_RAG_JOB_ID", job.toString()); environment.put("PPV_RAG_ATTEMPT_ID", attempt.toString());
            environment.put("PPV_RAG_ENDPOINT_NONCE", session.nonce.toString());
            long memoryLimit = isBatchV3 ? 2048L * 1024 * 1024 : 512L * 1024 * 1024;
            NativeWorkerProcess process = NativeWorkerProcess.launchSuspended(job, binary, List.of("-m", "uvicorn", "ppv_rag.main:app",
                    "--host", "127.0.0.1", "--port", Integer.toString(client.port()), "--no-access-log", "--no-server-header", "--log-level", "critical"),
                    environment, work, memoryLimit, 1);
            synchronized(session) { session.process = process; }
            checkRunning(session);
            // The child remains suspended until this binding's physical COMMIT has returned successfully.
            tx.executeWithoutResult(status -> {
                var proof = isBatchV3 ? authority.lockPreparedBatchAttempt(job, attempt, preparedBatch)
                        : authority.lockPreparedAttempt(job, attempt, prepared, false);
                proof.lease().bindWorker(Map.of("process_id", process.pid(), "started_at", process.creationTime().toString(),
                        "creation_file_time", Long.toString(process.creationTimeFileTime()), "runtime_id", runtime.instance().toString(),
                        "endpoint_nonce", session.nonce.toString()));
                proof.lease().updateStatus("ACTIVE", authority.databaseTime()); entities.flush();
            });
            checkRunning(session); process.resume();
            long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
            do {
                checkRunning(session);
                if (client.isBoundReady(job,attempt,session.nonce,process.pid(),Duration.ofNanos(Math.max(1,end-System.nanoTime())))
                        && System.nanoTime()<end
                        && process.ownsProcess(process.pid(),process.creationTimeFileTime())) {
                    synchronized(session) { if(session.stopping) throw new IllegalStateException("Job is stopping"); session.ready=true; }
                    return;
                }
                if (process.hasTerminatedTree()) throw new IllegalStateException("Worker startup failed");
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
            } while(System.nanoTime()<end && !Thread.currentThread().isInterrupted());
            throw new IllegalStateException("Worker readiness deadline exceeded");
        } catch (RuntimeException failure) {
            end(job);
            stopAndVerify(job);
            throw new StartupFailure(failure);
        }
    }

    public UUID requireReady(UUID job, UUID attempt) {
        if (!enabled) throw new IllegalStateException("Worker execution is contained pending mandatory verification");
        var session=sessions.get(job);
        if (session==null) throw new IllegalStateException("Worker is not supervised");
        synchronized(session) {
            if (!session.ready || session.stopping || !session.dispatching || session.process==null
                    || !session.process.ownsProcess(session.process.pid(),session.process.creationTimeFileTime()))
                throw new IllegalStateException("Worker is not ready");
            return session.nonce;
        }
    }

    public void end(UUID job) {
        var session=sessions.get(job);
        if(session!=null) synchronized(session) { session.dispatching=false; session.returned=true; session.notifyAll(); }
    }

    public boolean stopAndVerify(UUID job) {
        outsideTransaction();
        if(!sessions.containsKey(job)) {
            var outstanding=leases.findByJobId(job);
            if(outstanding.size()!=1 || !Set.of("ADMITTED","STOPPING","RECOVERY_REQUIRED").contains(outstanding.getFirst().getStatus())
                    || outstanding.getFirst().getWorkerIdentity()!=null || !isCurrentController(outstanding.getFirst())) return false;
            Number issued=(Number)entities.createNativeQuery("SELECT count(*) FROM rag.worker_grants WHERE job_id=:job")
                    .setParameter("job",job).getSingleResult();
            if(issued.longValue()!=0) return false;
        }
        var session=sessions.computeIfAbsent(job, ignored -> new Session());
        synchronized(session) { session.stopping=true; session.ready=false; }
        long end=System.nanoTime()+STOP_LIMIT.toNanos();
        while(System.nanoTime()<end) {
            NativeWorkerProcess process;
            synchronized(session) { process=session.process; }
            NativeWorkerProcess verifier;
            synchronized(session) { verifier=session.verifier; }
            boolean verificationStopped=verifier==null || verifier.terminateAndVerify(Duration.ofMillis(250));
            boolean terminated=process==null || process.terminateAndVerify(Duration.ofMillis(Math.max(1,
                    Math.min(250,TimeUnit.NANOSECONDS.toMillis(end-System.nanoTime())))));
            boolean transportStopped=client.stopAndVerifyTransport(job);
            synchronized(session) {
                if(terminated && verificationStopped && !session.verifying && transportStopped && !session.dispatching && (session.process==null || session.process.hasTerminatedTree())) {
                    if (session.verificationCleanup!=null) {
                        session.verificationCleanup.run(); session.verificationCleanup=null;
                    }
                    session.stopped=true; return true;
                }
                try { session.wait(20); }
                catch(InterruptedException interrupted) { Thread.currentThread().interrupt(); return false; }
            }
        }
        return false;
    }

    public boolean isSupervised(UUID job) {
        return sessions.containsKey(job);
    }

    public boolean proveStopped(UUID job) {
        var session=sessions.get(job);
        if(session==null) return false;
        synchronized(session) { return session.stopped && session.stopping && !session.dispatching && !session.verifying; }
    }

    /** Source-free accounting is read before a known terminal C closes the native job handle. */
    public NativeWorkerProcess.ResourceUsage stoppedResources(UUID job) {
        outsideTransaction();
        var session=sessions.get(job);
        if(session==null) throw new IllegalStateException("Native worker accounting is unavailable");
        synchronized(session) {
            if(!session.stopped || !session.stopping || session.dispatching || session.process==null)
                throw new IllegalStateException("Native worker termination is not proven");
            return session.process.resourceUsage();
        }
    }

    /** Reconcile only already-proven stopped handles after an uncertain SQL acknowledgement. */
    public Set<UUID> provenStoppedJobs() {
        var jobs=new HashSet<UUID>();
        sessions.forEach((job,session) -> { synchronized(session) {
            if(session.stopped && session.stopping && !session.dispatching && !session.verifying) jobs.add(job);
        } });
        return Set.copyOf(jobs);
    }

    /** Does not resume work. Unknown creator/process identity retains both slot and reservations. */
    public boolean recoverStopped(RagJobLease lease) {
        outsideTransaction();
        if(proveStopped(lease.getJobId())) return true;
        var controller=lease.getControllerIdentity();
        if(controller==null) return false;
        try {
            long pid=((Number)controller.get("process_id")).longValue();
            Instant started=Instant.parse((String)controller.get("started_at"));
            if(pid==ProcessHandle.current().pid() && runtime.instance().toString().equals(controller.get("runtime_id")))
                return stopAndVerify(lease.getJobId());
            var old=ProcessHandle.of(pid);
            if(old.isPresent() && old.get().isAlive()) {
                var actual=old.get().info().startInstant();
                if(actual.isEmpty() || actual.get().equals(started)) return false;
            }
            var worker=lease.getWorkerIdentity();
            boolean terminated;
            if(worker==null) {
                // No raw input or grant is ever issued before a known committed worker binding.
                terminated="ADMITTED".equals(lease.getStatus());
            } else {
                long workerPid=((Number)worker.get("process_id")).longValue();
                long fileTime=Long.parseLong((String)worker.get("creation_file_time"));
                terminated=NativeWorkerProcess.recoverTerminated(lease.getJobId(),STOP_LIMIT)
                        || NativeWorkerProcess.isRecordedProcessTerminated(workerPid,fileTime,STOP_LIMIT);
            }
            if(!terminated) return false;
            if (worker!=null && worker.get("verification") instanceof Map<?,?> verification) {
                terminated=NativeWorkerProcess.recoverTerminated(lease.getAttemptId(),STOP_LIMIT)
                        || NativeWorkerProcess.isRecordedProcessTerminated(((Number)verification.get("process_id")).longValue(),
                            Long.parseLong((String)verification.get("creation_file_time")),STOP_LIMIT);
                if (!terminated) return false;
            }
            var session=sessions.computeIfAbsent(lease.getJobId(), ignored -> new Session());
            synchronized(session) { session.stopping=true; session.stopped=true; session.returned=true; }
            return true;
        } catch(RuntimeException unknown) { return false; }
    }

    /** Called only after a known terminal/seal/release COMMIT. No dispatch can be admitted again by SQL. */
    public void forget(UUID job) {
        var session=sessions.get(job);
        if(session!=null) synchronized(session) {
            if(!session.stopped || session.dispatching || session.verifying) throw new IllegalStateException("Worker termination is not proven");
            if(session.process!=null) session.process.close();
            if(session.verifier!=null) session.verifier.close();
            client.forgetTransport(job);
            sessions.remove(job,session);
        }
    }

    public void beginVerification(UUID job, UUID attempt) {
        outsideTransaction();
        var session=Objects.requireNonNull(sessions.get(job));
        synchronized(session) {
            var lease=leases.findByJobIdAndAttemptId(job,attempt).orElseThrow();
            if (!session.stopped || session.dispatching || session.verifying || !"ACTIVE".equals(lease.getStatus())
                    || lease.getWorkerIdentity().containsKey("verification"))
                throw new IllegalStateException("Document verification cannot start");
            session.verifying=true;
        }
    }

    /** Persist the exact suspended validator identity before it can consume the managed input. */
    public void bindVerification(UUID job, UUID attempt, NativeWorkerProcess verifier) {
        var session=Objects.requireNonNull(sessions.get(job));
        synchronized(session) { session.verifier=verifier; }
        tx.executeWithoutResult(status -> {
            var lease=leases.findByJobIdAndAttemptIdForUpdate(job,attempt).orElseThrow(); entities.refresh(lease);
            if (!"ACTIVE".equals(lease.getStatus())) throw new IllegalStateException("Document verification cancelled");
            lease.bindVerification(Map.of("process_id",verifier.pid(),"creation_file_time",Long.toString(verifier.creationTimeFileTime())));
            entities.flush();
        });
    }

    public void endVerification(UUID job, NativeWorkerProcess verifier) {
        var session=Objects.requireNonNull(sessions.get(job));
        synchronized(session) {
            boolean stopped;
            try { stopped=verifier==null || verifier.hasTerminatedTree(); }
            catch (RuntimeException unknown) { stopped=false; }
            session.verifying=false; session.stopped=stopped;
            session.notifyAll();
        }
    }

    public void deferVerificationCleanup(UUID job, Runnable cleanup) {
        var session=Objects.requireNonNull(sessions.get(job));
        synchronized(session) {
            if (session.stopped && !session.verifying) cleanup.run();
            else session.verificationCleanup=cleanup;
        }
    }

    private static Path requiredPath(String value) {
        if(value==null || value.isBlank()) throw new IllegalStateException("Isolated worker configuration is unavailable");
        var path=Path.of(value); if(!path.isAbsolute()) throw new IllegalStateException("Worker path must be absolute"); return path.normalize();
    }
    private boolean isCurrentController(RagJobLease lease) {
        var controller=lease.getControllerIdentity();
        return controller!=null && controller.get("process_id") instanceof Number pid
                && pid.longValue()==ProcessHandle.current().pid()
                && runtime.instance().toString().equals(controller.get("runtime_id"))
                && ProcessHandle.current().info().startInstant().orElseThrow().toString().equals(controller.get("started_at"));
    }
    private static void checkRunning(Session session) {
        synchronized(session) { if(session.stopping || Thread.currentThread().isInterrupted()) throw new IllegalStateException("Job is stopping"); }
    }
    private static void outsideTransaction() {
        if(TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("Worker lifecycle cannot run inside an ambient transaction");
    }
    @PreDestroy void shutdown() {
        sessions.keySet().forEach(this::stopAndVerify);
        sessions.values().forEach(session -> { synchronized(session) {
            if(session.process!=null) session.process.close(); if(session.verifier!=null) session.verifier.close();
        } });
    }
}
