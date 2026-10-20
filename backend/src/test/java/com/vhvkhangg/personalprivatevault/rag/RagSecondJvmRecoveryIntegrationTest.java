package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/** Real second JVM, abrupt controller halt, Windows Job closure and fresh startup recovery. */
@Timeout(180)
class RagSecondJvmRecoveryIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @Autowired private Environment environment;

    @Test
    void liveControllerPastLeaseExpiryPreventsSecondJvmTakeoverThenGenuineDeathAllowsRecovery() throws Exception {
        runCrashBoundary("ADMITTED_WAIT",true);
    }

    @Test
    void crashAfterIssuedGrantCommitNeverDispatchesOrAutomaticallyResumesInput() throws Exception {
        runCrashBoundary("ISSUED_WAIT",false);
    }

    @Test
    void actualControllerCrashAfterPythonStagingDiscardsOrphanReceiptWithoutPublication() throws Exception {
        runCrashBoundary("STAGED_WAIT",false);
    }

    @Test
    void controllerDeathWithActualBufferedHttpInputRetainsExclusionUntilVerifiedRecovery() throws Exception {
        runCrashBoundary("BUFFERED_WAIT",false);
    }

    private void runCrashBoundary(String mode,boolean proveLivePastExpiry) throws Exception {
        RagSourceView source=approvedFilm();
        UUID originalRuntime=jdbcTemplate.queryForObject("SELECT current_instance FROM rag.runtime_state",UUID.class);
        Probe controller=null;
        UUID job=null;
        try {
            controller=launch(mode,source.id().toString());
            Map<String,Object> admitted=controller.receipt();
            job=UUID.fromString((String)admitted.get("job_id"));
            UUID attempt=UUID.fromString((String)admitted.get("attempt_id"));
            assertThat(((Number)admitted.get("controller_pid")).longValue()).isEqualTo(controller.process.pid());
            assertThat(admitted.get("controller_started_at")).isEqualTo(
                controller.process.info().startInstant().orElseThrow().toString());
            assertThat(admitted.get("registered_runtime")).isEqualTo(admitted.get("runtime_id"));
            assertThat(reservation(source)).isEqualTo(attempt);
            assertEditBlocked(source);
            int staged="STAGED_WAIT".equals(mode)?1:0;
            int issued="ADMITTED_WAIT".equals(mode)?0:1;
            assertThat(((Number)admitted.get("staged_receipts")).intValue()).isEqualTo(staged);
            assertThat(((Number)admitted.get("issued_grants")).intValue()).isEqualTo(issued);
            assertThat(((Number)admitted.get("publications")).intValue()).isZero();
            if("BUFFERED_WAIT".equals(mode)) {
                assertThat(admitted.get("body_claim_committed")).isEqualTo(true);
                assertThat(((Number)admitted.get("partial_body_bytes_sent")).intValue()).isPositive();
                assertThat(jdbcTemplate.queryForObject("""
                    SELECT count(*) FROM rag.worker_grants WHERE job_id=? AND consumed_at IS NOT NULL
                    """,Integer.class,job)).isEqualTo(1);
            }
            if(proveLivePastExpiry) {
                UUID observedJob=job;
                // The real public API's minimum lease is one minute; wait on actual
                // PostgreSQL time rather than editing its immutable authority deadline.
                await().atMost(Duration.ofSeconds(65)).pollInterval(Duration.ofMillis(50)).until(() ->
                    Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
                        SELECT clock_timestamp()>=leased_until FROM rag.job_leases WHERE job_id=?
                        """,Boolean.class,observedJob)));
            }
            if("ADMITTED_WAIT".equals(mode)) {
                try(Probe competitor=launch("RECOVER_EXIT",job.toString())) {
                    Map<String,Object> rejected=competitor.receipt();
                    competitor.assertExited(0);
                    assertThat(rejected.get("current_runtime")).isEqualTo(false);
                    assertThat(rejected.get("manual_admission_denied")).isEqualTo(true);
                    assertThat(rejected.get("registered_runtime")).isEqualTo(admitted.get("runtime_id"));
                    assertThat(rejected.get("status")).isEqualTo("ADMITTED");
                    assertThat(((Number)rejected.get("reserved_sources")).intValue()).isEqualTo(1);
                    assertThat(reservation(source)).isEqualTo(attempt);
                    assertEditBlocked(source);
                }
            }
            Map<String,Object> worker=jdbcTemplate.queryForMap(
                "SELECT worker_identity FROM rag.job_leases WHERE job_id=?",job);
            Long workerPid=null,workerCreation=null;
            if(issued!=0) {
                var identity=new ObjectMapper().readTree(worker.get("worker_identity").toString());
                workerPid=identity.path("process_id").longValue();
                workerCreation=Long.parseLong(identity.path("creation_file_time").asText());
                assertThat(NativeWorkerProcess.isRecordedProcessTerminated(workerPid,workerCreation)).isFalse();
            } else assertThat(worker.get("worker_identity")).isNull();
            controller.halt();
            assertThat(controller.process.isAlive()).isFalse();
            if(workerPid!=null) {
                long exactPid=workerPid,exactCreation=workerCreation;
                await().atMost(Duration.ofSeconds(5)).pollInterval(Duration.ofMillis(10)).until(() ->
                    NativeWorkerProcess.isRecordedProcessTerminated(exactPid,exactCreation));
            }
            // A real controller death and Windows kill-on-close are not a release.
            assertThat(reservation(source)).isEqualTo(attempt);
            assertEditBlocked(source);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",
                Integer.class,job)).isEqualTo(staged);
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations",Integer.class)).isZero();
            try(Probe replacement=launch("RECOVER_EXIT",job.toString())) {
                Map<String,Object> recovered=replacement.receipt();
                replacement.assertExited(0);
                assertThat(recovered.get("current_runtime")).isEqualTo(true);
                assertThat(recovered.get("status")).isEqualTo("ABORTED");
                assertThat(((Number)recovered.get("reserved_sources")).intValue()).isZero();
                assertThat(((Number)recovered.get("staged_receipts")).intValue()).isZero();
                assertThat(((Number)recovered.get("issued_grants")).intValue()).isZero();
                assertThat(((Number)recovered.get("publications")).intValue()).isZero();
            }
            assertThat(reservation(source)).isNull();
            assertThat(jdbcTemplate.queryForObject("""
                SELECT staging_closed AND status='ABORTED' AND dependency_manifest='SCRUBBED'
                FROM rag.job_leases WHERE job_id=?
                """,Boolean.class,job)).isTrue();
            assertThat(jdbcTemplate.queryForObject("SELECT purge_status FROM rag.runs WHERE job_id=?",
                String.class,job)).isEqualTo("PURGE_COMPLETE");
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class))
                .as("recovery never automatically creates or resumes indexing").isEqualTo(1);
            assertThat(jdbcTemplate.update("UPDATE films SET title='Synthetic edit after actual recovery' WHERE id=?",
                Long.parseLong(source.sourceId()))).isEqualTo(1);
        } finally {
            if(controller!=null) controller.close();
            if(job==null && controller!=null) {
                var owned=jdbcTemplate.queryForList("""
                    SELECT job_id FROM rag.job_leases
                    WHERE controller_identity->>'process_id'=? AND controller_identity->>'started_at'=?
                    """,UUID.class,Long.toString(controller.process.pid()),controller.startedAt);
                assertThat(owned).hasSizeLessThanOrEqualTo(1);
                if(!owned.isEmpty()) job=owned.getFirst();
            }
            if(job!=null && jdbcTemplate.queryForObject("""
                SELECT count(*) FROM rag.job_leases WHERE job_id=?
                    AND status IN ('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING')
                """,Integer.class,job)!=0) {
                // Failed assertions may not leave an owned consumer or hide retained
                // reservations. Cleanup uses a new real recovery JVM, never admin release.
                try(Probe cleanup=launch("RECOVER_EXIT",job.toString())) {
                    assertThat(cleanup.receipt().get("status")).isEqualTo("ABORTED");
                    cleanup.assertExited(0);
                }
            }
            assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL",Integer.class)).isZero();
            assertThat(jdbcTemplate.queryForObject("""
                SELECT count(*) FROM rag.job_leases WHERE status IN
                    ('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING')
                """,Integer.class)).isZero();
            // Both child contexts have really stopped. Restore only the cached,
            // empty test fixture's registration, not any production job ownership.
            jdbcTemplate.update("UPDATE rag.runtime_state SET current_instance=? WHERE singleton",originalRuntime);
        }
    }

    private Probe launch(String mode,String identifier) throws Exception {
        Path folder=repository().resolve("logs/phase17-alternative-b-implementation-2026-10-15/recovery-probes");
        Files.createDirectories(folder);
        String id=UUID.randomUUID().toString();
        Path receipt=folder.resolve(id+".json");
        Path arguments=folder.resolve(id+".args");
        String classPath=System.getProperty("surefire.test.class.path",System.getProperty("java.class.path"));
        String entry=RagRecoveryProbeProcess.class.getName();
        Files.writeString(arguments,"--enable-native-access=ALL-UNNAMED\n--class-path\n"+argument(classPath)
            +"\n"+entry+"\n"+mode+"\n"+argument(receipt.toString())+"\n"+argument(identifier)+"\n",StandardCharsets.UTF_8);
        var properties=new LinkedHashMap<String,Object>();
        properties.put("ppv.rag.storage.snapshot-root",environment.getRequiredProperty("ppv.rag.storage.snapshot-root"));
        properties.put("server.port","0");
        properties.put("server.address","127.0.0.1");
        properties.put("spring.datasource.url",POSTGRES.getJdbcUrl());
        properties.put("spring.datasource.username","rag_runtime");
        properties.put("spring.datasource.password",AbstractRagIntegrationTest.PASSWORD);
        properties.put("spring.flyway.enabled","false");
        properties.put("spring.main.banner-mode","off");
        properties.put("logging.level.root","ERROR");
        properties.put("ppv.rag.limits.lease-duration-minutes","1");
        properties.put("ppv.rag.reconciliation-delay-ms","3600000");
        properties.put("ppv.security.jwt.secret","dGVzdC1zZWNyZXQta2V5LWZvci1wZXJzb25hbC1wcml2YXRlLXZhdWx0LXRlc3RzLTEyMzQ1Ng==");
        for(String key:List.of("ppv.rag.test-scoped-execution","ppv.rag.engine.url","ppv.rag.engine.service-key",
                "ppv.rag.worker.executable","ppv.rag.worker.venv-launcher",
                "ppv.rag.worker.working-directory","ppv.rag.worker.dsn")) {
            properties.put(key,environment.getRequiredProperty(key));
        }
        var builder=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java.exe").toString(),"@"+arguments);
        builder.directory(repository().resolve("backend").toFile());
        builder.environment().put("PPV_PROBE_CONFIGURATION",new ObjectMapper().writeValueAsString(properties));
        builder.redirectErrorStream(true).redirectOutput(folder.resolve(id+".log").toFile());
        return new Probe(builder.start(),receipt);
    }

    private static String argument(String value) {
        return "\""+value.replace("\\","\\\\").replace("\"","\\\"")+"\"";
    }

    private static final class Probe implements AutoCloseable {
        private final Process process;
        private final Path path;
        private final String startedAt;
        private Probe(Process process,Path path) {
            this.process=process;this.path=path;
            this.startedAt=process.info().startInstant().orElseThrow().toString();
        }
        Map<String,Object> receipt() throws Exception {
            AtomicReference<Map<String,Object>> holder = new AtomicReference<>();
            await().atMost(Duration.ofSeconds(35)).pollInterval(Duration.ofMillis(50))
                .until(() -> {
                    if (!Files.isRegularFile(path)) {
                        return !process.isAlive() && Files.isRegularFile(path);
                    }
                    try {
                        byte[] bytes = Files.readAllBytes(path);
                        if (bytes.length == 0) return false;
                        holder.set(new ObjectMapper().readValue(bytes, new TypeReference<>() {}));
                        return true;
                    } catch (IOException sharingViolationOrPartialWrite) {
                        return false;
                    }
                });
            assertThat(path).as("actual second-JVM control receipt must exist").isRegularFile();
            Map<String,Object> result = holder.get();
            if (result == null) {
                result = new ObjectMapper().readValue(path.toFile(), new TypeReference<>() {});
            }
            assertThat(result.get("success")).as("probe error type: %s", result.get("error_type")).isEqualTo(true);
            return result;
        }
        void assertExited(int code) throws Exception {
            assertThat(process.waitFor(15,TimeUnit.SECONDS)).as("actual owned JVM must terminate").isTrue();
            assertThat(process.exitValue()).isEqualTo(code);
        }
        void halt() throws Exception {
            process.getOutputStream().write('H');process.getOutputStream().flush();
            assertExited(73);
        }
        @Override public void close() throws Exception {
            if(process.isAlive()) {
                try { halt(); }
                finally {
                    if(process.isAlive()) {
                        process.destroy();
                        if(!process.waitFor(5,TimeUnit.SECONDS)) {
                            process.destroyForcibly();
                            assertThat(process.waitFor(5,TimeUnit.SECONDS)).isTrue();
                        }
                    }
                }
            }
        }
    }

    private UUID reservation(RagSourceView source) {
        return jdbcTemplate.queryForObject("SELECT reserved_by FROM public.vault_rag_source_state WHERE source_id=?",
            UUID.class,Long.parseLong(source.sourceId()));
    }

    private void assertEditBlocked(RagSourceView source) {
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE films SET is_nsfw=true WHERE id=?",
            Long.parseLong(source.sourceId())))
            .isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT is_nsfw FROM films WHERE id=?",
            Boolean.class,Long.parseLong(source.sourceId()))).isFalse();
    }

    private RagSourceView approvedFilm() {
        Long owner=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FILM') RETURNING id",Long.class);
        jdbcTemplate.update("INSERT INTO films(id,title,format,production_style,is_nsfw) "
            +"VALUES (?,'Synthetic recovery process boundary','MOVIE','LIVE_ACTION',false)",owner);
        var registered=RagTestCommands.register(enabledRag,RagSourceType.FILM,owner.toString());
        return RagTestCommands.approve(enabledRag,registered.id(),Set.of("title","format"));
    }
}
