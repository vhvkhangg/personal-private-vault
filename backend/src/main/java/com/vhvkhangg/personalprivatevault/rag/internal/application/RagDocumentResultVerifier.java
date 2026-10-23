package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagStagedDocumentOutput;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagJobLeaseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Trusted source-context replay using the accepted D17 parser, without worker SQL credentials.
 * Synchronous validation only: no queue, publication, retry or second parser implementation.
 */
@Component
public class RagDocumentResultVerifier {
    private final String executable, launcher, directory;
    private final RagJobLeaseRepository leases;
    private final RagWorkerSupervisor supervisor;
    private final com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore store;
    private final ObjectMapper json = new ObjectMapper();

    public RagDocumentResultVerifier(RagJobLeaseRepository leases, RagWorkerSupervisor supervisor,
            com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore store,
            @Value("${ppv.rag.worker.executable:}") String executable,
            @Value("${ppv.rag.worker.venv-launcher:}") String launcher,
            @Value("${ppv.rag.worker.working-directory:}") String directory) {
        this.leases=leases; this.supervisor=supervisor; this.store=store; this.executable=executable; this.launcher=launcher; this.directory=directory;
    }

    public void verify(UUID job, UUID attempt, List<Map<String,Object>> roots, List<RagStagedDocumentOutput> staged) {
        var results = staged.stream().map(s -> Map.of("root_key",s.getRootKey(),"success",s.isSuccess(),
                "reason_code",s.getReasonCode(),"chunks",s.getChunksPayload(),"resources",s.getResourceReport(),
                "warnings",s.getWarningCodes())).toList();
        UUID inputId=UUID.randomUUID(), outputId=UUID.randomUUID();
        supervisor.beginVerification(job,attempt);
        NativeWorkerProcess process=null;
        store.beginCapture(inputId); store.beginCapture(outputId);
        try {
            byte[] bytes=json.writeValueAsBytes(Map.of("roots",roots,"results",results));
            if (bytes.length>32*1024*1024) throw new IllegalStateException("Document result budget exceeded");
            Path input=store.writeTemp(inputId,new java.io.ByteArrayInputStream(bytes),(long)bytes.length,null).tempPath();
            Path output=input.getParent().resolve(outputId+".tmp");
            Path work=Path.of(directory).toAbsolutePath();
            var environment=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
            for (String key:List.of("SystemRoot","WINDIR","TEMP","TMP","LOCALAPPDATA","PATH")) {
                if (System.getenv(key)!=null) environment.put(key,System.getenv(key));
            }
            environment.put("__PYVENV_LAUNCHER__",Path.of(launcher).toAbsolutePath().toString());
            environment.put("PYTHONPATH",work.resolve("src").toString());
            environment.put("PYTHONNOUSERSITE","1"); environment.put("PYTHONDONTWRITEBYTECODE","1");
            // Deliberately no service key, database DSN or worker input capability.
            process=NativeWorkerProcess.launchSuspended(attempt,Path.of(executable),
                    List.of("-m","ppv_rag.document_verification",input.toString(),output.toString()),
                    environment,work,1024L*1024*1024,1);
            {
                supervisor.bindVerification(job,attempt,process);
                process.resume();
                long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(Math.min(900, 15L+60L*roots.size()));
                try {
                    while (!process.hasTerminatedTree()) {
                        var lease=leases.findByJobIdAndAttemptId(job,attempt).orElseThrow();
                        if (Thread.currentThread().isInterrupted() || System.nanoTime()>=end
                                || !"ACTIVE".equals(lease.getStatus()) || !lease.getLeasedUntil().isAfter(java.time.Instant.now()))
                            throw new IllegalStateException("Document verification cancelled or expired");
                        TimeUnit.MILLISECONDS.sleep(100);
                    }
                } finally {
                    if (!process.terminateAndVerify(Duration.ofSeconds(5)))
                        throw new IllegalStateException("Document verifier termination is unproven");
                }
            }
            if (!Files.isRegularFile(output) || Files.size(output)!=64
                    || !RagSourceAuthority.digest(new String(bytes,StandardCharsets.UTF_8)).equals(Files.readString(output)))
                throw new IllegalStateException("Document output does not match authorized source context");
        } catch (java.io.IOException invalid) {
            throw new IllegalStateException("Document verification unavailable");
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt(); throw new IllegalStateException("Document verification interrupted");
        } finally {
            boolean stopped=false;
            try { stopped=process==null || process.terminateAndVerify(Duration.ofSeconds(5)); }
            catch (RuntimeException unknown) { /* Retain managed input and process ownership. */ }
            supervisor.endVerification(job,process);
            if (!stopped) {
                supervisor.deferVerificationCleanup(job,() -> { store.endCapture(inputId); store.endCapture(outputId); });
                throw new IllegalStateException("Document verifier termination requires recovery");
            }
            // Failed erasure remains in the existing managed-temp durable retry inventory.
            var inputCleanup=store.cleanupTemp(inputId); var outputCleanup=store.cleanupTemp(outputId);
            store.endCapture(inputId); store.endCapture(outputId);
            if (inputCleanup.status()!=com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore.DeletionStatus.COMPLETED
                    || outputCleanup.status()!=com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.storage.RagOriginalBytesStore.DeletionStatus.COMPLETED)
                throw new IllegalStateException("Document verification cleanup pending");
        }
    }
}
