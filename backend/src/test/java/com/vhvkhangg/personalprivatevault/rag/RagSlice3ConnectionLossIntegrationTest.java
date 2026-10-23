package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.application.*;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.*;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;

/** Actual PostgreSQL wire loss, not an exception thrown after JDBC commit returns. */
@Timeout(120)
@org.springframework.test.annotation.DirtiesContext(classMode=org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class RagSlice3ConnectionLossIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @org.junit.jupiter.api.AfterEach void captureSyntheticBindings(org.junit.jupiter.api.TestInfo test) throws Exception {RagSlice3Evidence.capture(jdbcTemplate,objectMapper,test.getDisplayName());}
    @TempDir java.nio.file.Path work;
    static final PgWireProxy WIRE=new PgWireProxy();
    @Autowired RagDocumentFinalizationGate gate;
    @Autowired PlatformTransactionManager transactions;
    @Autowired RagRecoveryService recovery;
    @org.junit.jupiter.api.AfterAll static void closeOwnedWireProxy() throws IOException {WIRE.listener.close();WIRE.disconnectAll();}

    RagSourceView source(String name,String text) throws Exception {
        var path=work.resolve(name);java.nio.file.Files.writeString(path,text,StandardCharsets.UTF_8);
        var view=enabledRag.registerSnapshotCandidate(path.toString());
        return enabledRag.approveSource(view.id(),new RagConsentCommand(Set.of("content"),view.sourceVersion(),view.consentResetVersion(),view.authorizationEpoch(),view.fieldPolicyVersion(),null,view.snapshot()));
    }
    RagJobAdmissionResult stage(RagSourceView source) {
        var item=new RagDocumentRootItem("root",source.sourceType(),source.sourceId(),source.sourceVersion(),source.authorizationEpoch(),source.fieldPolicyVersion(),source.approvedFields());
        var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(List.of(item),Map.of()));
        enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),List.of(new RagDocumentBatchTask("root",source.sourceId(),"MD",null,null,null,List.of("content"),Map.of())));return job;
    }
    RagBatchFinalizationResult finish(RagJobAdmissionResult job) {
        String manifest=jdbcTemplate.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?",String.class,job.jobId());
        return enabledRag.finalizeDocumentBatch(job.jobId(),job.attemptId(),new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest)));
    }

    @DynamicPropertySource static void proxiedRuntime(DynamicPropertyRegistry registry) {
        registry.add("ppv.rag.test-runtime-jdbc-url",()->"jdbc:postgresql://127.0.0.1:"+WIRE.port()+"/"+POSTGRES.getDatabaseName()+"?sslmode=disable&gssEncMode=disable&socketTimeout=8");
        registry.add("ppv.rag.reconciliation-delay-ms",()->"3600000");
    }

    @Test void committedWireAcknowledgementIsLostAndDurableReconciliationDoesNotRepublish() throws Exception {
        var v=source("Wire.md","# Committed wire loss\n\nGenuine native content.\n"); var job=stage(v);
        withCommitFault(false,()->assertThatThrownBy(()->finish(job)).isInstanceOf(RuntimeException.class));
        assertThat(WIRE.dropped.get()).isEqualTo(1);
        assertThat(WIRE.lastTag).isEqualTo("COMMIT");
        try(var connection=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());var query=connection.prepareStatement("SELECT status,(SELECT count(*) FROM rag.index_generations WHERE job_id=l.job_id),(SELECT count(*) FROM rag.file_outcomes WHERE job_id=l.job_id) FROM rag.job_leases l WHERE job_id=?")) {
            query.setObject(1,job.jobId());try(var rows=query.executeQuery()) { assertThat(rows.next()).isTrue();assertThat(rows.getString(1)).isEqualTo("COMPLETED");assertThat(rows.getInt(2)).isEqualTo(1);assertThat(rows.getInt(3)).isEqualTo(1); }
        }
        recovery.reconcile();
        assertThat(finish(job).publishedCount()).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE job_id=?",Integer.class,job.jobId())).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isZero();
    }

    @Test void uncommittedWireLossRetainsReservationsWhileDatabaseIsUnreachableThenSafelyAborts() throws Exception {
        var v=source("Before.md","# Uncommitted wire loss\n"); var job=stage(v);
        withCommitFault(true,()->assertThatThrownBy(()->finish(job)).isInstanceOf(RuntimeException.class));
        assertThat(WIRE.dropped.get()).isEqualTo(1); assertThat(WIRE.lastTag).isEqualTo("BEFORE_COMMIT");
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("RECOVERY_REQUIRED");
        WIRE.unavailable.set(true); WIRE.disconnectAll();
        try {
            assertThatThrownBy(recovery::reconcile).isInstanceOf(RuntimeException.class);
            try(var c=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());var s=c.prepareStatement("SELECT l.status,e.reserved_by,(SELECT count(*) FROM rag.index_generations WHERE job_id=l.job_id) FROM rag.job_leases l JOIN rag.external_file_sources e ON e.reserved_by=l.attempt_id WHERE l.job_id=?")) {
                s.setObject(1,job.jobId());try(var r=s.executeQuery()) { assertThat(r.next()).isTrue();assertThat(r.getString(1)).isEqualTo("RECOVERY_REQUIRED");assertThat(r.getObject(2)).isEqualTo(job.attemptId());assertThat(r.getInt(3)).isZero(); }
            }
        } finally { WIRE.unavailable.set(false); }
        recovery.reconcile();
        assertThat(jdbcTemplate.queryForObject("SELECT status FROM rag.job_leases WHERE job_id=?",String.class,job.jobId())).isEqualTo("ABORTED");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=?",Integer.class,job.attemptId())).isZero();
    }

    void withCommitFault(boolean before,Runnable assertion) throws Exception {
        var field=RagDocumentFinalizationGate.class.getDeclaredField("transaction");field.setAccessible(true);var original=field.get(gate);
        var ef=RagDocumentFinalizationGate.class.getDeclaredField("entityManager");ef.setAccessible(true);var entities=(jakarta.persistence.EntityManager)ef.get(gate);
        var manager=new PlatformTransactionManager() {
            public TransactionStatus getTransaction(TransactionDefinition d) { return transactions.getTransaction(d); }
            public void rollback(TransactionStatus s) { transactions.rollback(s); }
            public void commit(TransactionStatus s) {
                int pid=entities.unwrap(org.hibernate.Session.class).doReturningWork(c->{try(var q=c.createStatement();var r=q.executeQuery("SELECT pg_backend_pid()")){r.next();return r.getInt(1);}});
                WIRE.arm(pid,before);transactions.commit(s);
            }
        };
        var tx=new TransactionTemplate(manager);tx.setTimeout(15);field.set(gate,tx);
        try { assertion.run(); } finally { field.set(gate,original); WIRE.armedPid.set(0); }
    }

    /** Bounded frame forwarding. Never records SQL, credentials or source payloads. */
    static final class PgWireProxy {
        final ServerSocket listener;
        final Set<Socket> sockets=ConcurrentHashMap.newKeySet();
        final AtomicInteger armedPid=new AtomicInteger(),dropped=new AtomicInteger();
        final AtomicBoolean unavailable=new AtomicBoolean();
        volatile boolean before; volatile String lastTag="";
        PgWireProxy() {
            try { listener=new ServerSocket(0,32,InetAddress.getLoopbackAddress()); }
            catch(IOException e) { throw new UncheckedIOException(e); }
            Thread.ofVirtual().start(()-> { while(!listener.isClosed()) {
                try { Socket client=listener.accept();if(unavailable.get()){client.close();continue;}Thread.ofVirtual().start(()->connect(client)); }
                catch(IOException e) { if(!listener.isClosed()) throw new UncheckedIOException(e); }
            }});
        }
        int port() { return listener.getLocalPort(); }
        void arm(int pid,boolean before) { this.before=before;lastTag="";dropped.set(0);armedPid.set(pid); }
        void disconnectAll() { for(var socket:sockets) close(socket); }
        void connect(Socket client) {
            Socket server=null;
            try {
                server=new Socket("127.0.0.1",POSTGRES.getMappedPort(5432));sockets.add(client);sockets.add(server);
                client.setSoTimeout(30000);server.setSoTimeout(30000);
                var incoming=new DataInputStream(client.getInputStream());var outgoing=new DataOutputStream(server.getOutputStream());
                int length=incoming.readInt();if(length<8||length>65536)throw new IOException("startup bounds");
                byte[] startup=incoming.readNBytes(length-4);if(startup.length!=length-4)throw new EOFException();
                outgoing.writeInt(length);outgoing.write(startup);outgoing.flush();
                Socket owned=server;var pid=new AtomicInteger();
                var upstream=Thread.ofVirtual().start(()->frames(client,owned,pid,true));
                frames(server,client,pid,false);close(client);close(server);upstream.join(2000);
            } catch(IOException|InterruptedException e) { if(e instanceof InterruptedException) Thread.currentThread().interrupt(); }
            finally {close(client);if(server!=null)close(server);}
        }
        void frames(Socket source,Socket destination,AtomicInteger pid,boolean upstream) {
            try {
                var input=new DataInputStream(source.getInputStream());var output=new DataOutputStream(destination.getOutputStream());
                while(true) {
                    int type=input.readUnsignedByte(),length=input.readInt();if(length<4||length>64*1024*1024)throw new IOException("frame bounds");
                    byte[] payload=input.readNBytes(length-4);if(payload.length!=length-4)throw new EOFException();
                    if(!upstream&&type=='K'&&payload.length==8)pid.set(java.nio.ByteBuffer.wrap(payload).getInt());
                    // Armed only after the publication callback has flushed and
                    // read its backend PID. No subsequent client frame reaches
                    // PostgreSQL, including cached extended-protocol COMMIT.
                    if(upstream&&before&&pid.get()!=0&&armedPid.compareAndSet(pid.get(),0)) {
                        lastTag="BEFORE_COMMIT";dropped.incrementAndGet();close(source);close(destination);return;
                    }
                    boolean commit=type==(upstream?'Q':'C')&&Arrays.equals(payload,"COMMIT\0".getBytes(StandardCharsets.US_ASCII));
                    if(commit&&before==upstream&&pid.get()!=0&&armedPid.compareAndSet(pid.get(),0)) {
                        lastTag=upstream?"BEFORE_COMMIT":"COMMIT";dropped.incrementAndGet();close(source);close(destination);return;
                    }
                    output.writeByte(type);output.writeInt(length);output.write(payload);output.flush();
                }
            } catch(IOException expectedDisconnect) { close(source);close(destination); }
        }
        void close(Socket socket) { sockets.remove(socket);try {socket.close();}catch(IOException ignored) { /* owned test transport already disconnected */ } }
    }
}
