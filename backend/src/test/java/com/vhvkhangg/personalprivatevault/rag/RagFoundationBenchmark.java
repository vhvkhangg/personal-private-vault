package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.RagIndexCommand;
import com.vhvkhangg.personalprivatevault.rag.api.RagJobAdmissionResult;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagLimits;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagWorkerSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceView;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Explicitly invoke with -Dtest=RagFoundationBenchmark. This is real indexing, not a default
 * Surefire test, a model-inference benchmark or permission to enable the ordinary runtime.
 */
@Timeout(value = 1500, unit = TimeUnit.SECONDS)
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.generate_statistics=true",
    "spring.jpa.properties.hibernate.session.events.log=false"
})
class RagFoundationBenchmark extends AbstractEnabledRagIntegrationTest {
    private static final int WARMUPS = 5;
    private static final int MEASUREMENTS = 100;
    private static final int INPUT_LIMIT = 1_048_576;
    private final ObjectMapper json = new ObjectMapper();
    private final List<Map<String, Object>> samples = new ArrayList<>();
    private final List<Map<String, Object>> workloads = new ArrayList<>();
    private final Map<String, Object> receipt = new LinkedHashMap<>();
    private final Map<String, Object> boundaries = new LinkedHashMap<>();

    @Autowired
    private RagSourceAuthority authority;
    @Autowired
    private RagLimits limits;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private RagWorkerSupervisor supervisor;

    private Statistics statistics;
    private String previousProcessIdentity;

    private record Workload(String name, long sourceId, int selectedSources, int depth,
                            int payloadBytes, boolean confirmChangedDescription) {
    }

    @Test
    void measureFourAuthorizedSingleJobWorkloadsAndRetainEveryOutcome() throws Exception {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        assertThat(statistics.isStatisticsEnabled()).isTrue();
        receipt.put("schema", "phase17-alternative-b-benchmark-v2-native-resources");
        receipt.put("reproduce", "From backend: mvn -ntp -DargLine=--enable-native-access=ALL-UNNAMED "
            + "-Dtest=RagFoundationBenchmark test (bounded outer runner: 1800 seconds)");
        receipt.put("started_at", Instant.now().toString());
        receipt.put("concurrency", 1);
        receipt.put("warmups_per_workload", WARMUPS);
        receipt.put("measured_jobs_per_workload", MEASUREMENTS);
        receipt.put("normal_fixture_required_success_rate", 1.0);
        receipt.put("methodology", "Each explicit manual job launches a cold confined native Python process, "
            + "uses authenticated loopback HTTP and restricted PostgreSQL staging, stops the actual "
            + "consumer, and physically commits verified publication/seal/terminal/reservation release. "
            + "JVM, database and filesystem caches remain warm. No retry, sleep or automatic indexing.");
        receipt.put("percentiles", "Nearest rank: sorted ceil(n*p)-1, measured successful jobs only; "
            + "failures remain in samples and invalidate acceptance.");
        receipt.put("environment", environment());
        receipt.put("configured_limits", Map.of("selected_sources", limits.selectedSources(),
            "selected_input_bytes", limits.inputBytes(), "dependency_depth_nodes", 5,
            "retained_jobs", limits.retainedJobs(), "jobs_per_source", limits.jobsPerSource(),
            "worker_job_object_memory_bytes", 512L * 1024 * 1024,
            "lock_timeout_ms", limits.lockMillis(), "statement_timeout_ms", limits.statementMillis(),
            "transaction_timeout_seconds", limits.transactionSeconds()));
        receipt.put("limitations", List.of(
            "No model, parser, OCR, retrieval or later-phase performance is measured.",
            "Hibernate prepared-statement deltas are JVM-wide observations; exclude JdbcTemplate, "
                + "Python/libpq and server-side trigger queries and may include background JVM work.",
            "Java CPU is aggregate process CPU; excludes the Python worker and PostgreSQL server.",
            "Memory samples are Java heap and host available physical memory at job boundaries; "
                + "they are not peak worker RAM or per-job allocations. No forced GC.",
            "Native job counters retain cumulative worker CPU, committed-memory peaks and I/O "
                + "after verified termination. Committed-memory peak is not resident working-set peak; "
                + "I/O includes Windows job-associated traffic, not physical-disk-only writes.",
            "Worker peak resident working set, Windows process handle leakage and Docker disk I/O "
                + "are NOT_VERIFIED_BY_THIS_BENCHMARK; external samples must report their coverage.",
            "Latency/resource acceptance thresholds beyond approved caps require evidence review; "
                + "this fixture does not invent or self-approve them.",
            "This benchmark establishes no OS-enforced offline guarantee."));
        receipt.put("resources_before", resourceSnapshot());
        String filter = System.getProperty("benchmark.workload");
        List<Workload> selectedFixtures = new ArrayList<>();
        Throwable failure = null;
        try {
            List<Workload> fixtures = createFixturesAndCheckBoundaries();
            receipt.put("catalog_sources", jdbcTemplate.queryForObject("SELECT count(*) FROM rag.sources", Long.class));
            if (filter != null && !filter.isBlank() && !"all".equalsIgnoreCase(filter)) {
                if ("part1".equalsIgnoreCase(filter)) {
                    selectedFixtures = fixtures.stream()
                        .filter(f -> f.name().equals("small") || f.name().equals("maximum_dependency_depth"))
                        .toList();
                } else if ("part2".equalsIgnoreCase(filter)) {
                    selectedFixtures = fixtures.stream()
                        .filter(f -> f.name().equals("maximum_selected_sources") || f.name().equals("maximum_selected_input_bytes"))
                        .toList();
                } else {
                    selectedFixtures = fixtures.stream()
                        .filter(f -> f.name().equalsIgnoreCase(filter))
                        .toList();
                }
            } else {
                selectedFixtures = fixtures;
            }
            for (Workload fixture : selectedFixtures) {
                run(fixture);
            }
            assertThat(jdbcTemplate.queryForObject("""
                SELECT count(*) FROM rag.job_leases
                WHERE status IN ('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING')
                """, Long.class)).isZero();
            assertThat(jdbcTemplate.queryForObject("""
                SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL
                """, Long.class)).isZero();
        } catch (Exception | AssertionError problem) {
            failure = problem;
            receipt.put("failure_type", problem.getClass().getName());
            // Exception messages may contain SQL or fixture data: retain only type and stage.
        } finally {
            receipt.put("finished_at", Instant.now().toString());
            receipt.put("resources_after", resourceSnapshot());
            receipt.put("boundary_checks", boundaries);
            receipt.put("workloads", workloads);
            receipt.put("samples", samples);
            receipt.put("attempted_jobs", samples.size());
            receipt.put("successful_jobs", samples.stream().filter(row -> Boolean.TRUE.equals(row.get("success"))).count());
            receipt.put("failed_jobs", samples.stream().filter(row -> Boolean.FALSE.equals(row.get("success"))).count());
            receipt.put("all_requested_work_completed", failure == null && samples.size() == selectedFixtures.size() * (WARMUPS + MEASUREMENTS));
            String fileSuffix = (filter != null && !filter.isBlank() && !"all".equalsIgnoreCase(filter)) ? ("-" + filter.toLowerCase()) : "";
            Path output = repository().resolve("logs/phase17-alternative-b-implementation-2026-10-15/benchmark-results-with-native-resources" + fileSuffix + ".json");
            Files.createDirectories(output.getParent());
            json.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), receipt);
        }
        // Persist every collected success/failure before the acceptance assertion.
        if (failure != null) {
            throw new AssertionError("Real benchmark did not meet 100% normal-fixture success; inspect source-free receipt", failure);
        }
        int expectedCount = selectedFixtures.size() * (WARMUPS + MEASUREMENTS);
        assertThat(samples).hasSize(expectedCount);
        assertThat(samples).allSatisfy(row -> assertThat(row.get("success")).isEqualTo(true));
    }

    private List<Workload> createFixturesAndCheckBoundaries() {
        assertThat(limits.selectedSources()).isEqualTo(100);
        assertThat(limits.inputBytes()).isEqualTo(INPUT_LIMIT);
        RagSourceView small = film("Synthetic small benchmark", false);
        RagSourceView star = film("Synthetic selected limit benchmark", false);
        List<RagSourceView> leaves = new ArrayList<>();
        for (int index = 0; index < 99; index++) {
            RagSourceView leaf = film("Synthetic dependency " + index, false);
            leaves.add(leaf);
            if (index < 98) {
                enabledRag.addDependency(star.id(), leaf.id());
            }
        }
        assertThat(authority.prepareSource(star.id(), Map.of()).proofs()).hasSize(99);
        boundaries.put("selected_sources_below_99", true);
        enabledRag.addDependency(star.id(), leaves.getLast().id());
        assertThat(authority.prepareSource(star.id(), Map.of()).proofs()).hasSize(100);
        boundaries.put("selected_sources_at_100", true);
        assertThatThrownBy(() -> enabledRag.addDependency(star.id(), small.id()))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("count budget");
        assertThat(authority.prepareSource(star.id(), Map.of()).proofs()).hasSize(100);
        boundaries.put("selected_sources_above_101_rejected_without_topology_change", true);

        List<RagSourceView> chain = new ArrayList<>();
        for (int index = 0; index < 5; index++) {
            chain.add(film("Synthetic depth " + index, false));
            if (index > 0) {
                enabledRag.addDependency(chain.get(index - 1).id(), chain.get(index).id());
            }
            if (index == 3) {
                assertThat(authority.prepareSource(chain.getFirst().id(), Map.of()).proofs()).hasSize(4);
                boundaries.put("depth_nodes_below_4", true);
            }
        }
        assertThat(authority.prepareSource(chain.getFirst().id(), Map.of()).proofs()).hasSize(5);
        boundaries.put("depth_nodes_at_5", true);
        assertThatThrownBy(() -> enabledRag.addDependency(chain.getLast().id(), small.id()))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("depth");
        assertThat(authority.prepareSource(chain.getFirst().id(), Map.of()).proofs()).hasSize(5);
        boundaries.put("depth_nodes_above_6_rejected_without_topology_change", true);

        RagSourceView near = film("Synthetic input byte limit benchmark", true);
        int initial = bytes(authority.prepareSource(near.id(), Map.of()).payload());
        // Initial description is one safe nonblank ASCII character: approval does not
        // bypass the restrictive-metadata/empty-description policy to build a fixture.
        int charactersAtLimit = INPUT_LIMIT - initial + 1;
        assertThat(charactersAtLimit).isPositive();
        replaceDescription(near, charactersAtLimit - 1);
        assertThat(preparedWithExactConfirmation(near).payload().getBytes(StandardCharsets.UTF_8))
            .hasSize(INPUT_LIMIT - 1);
        boundaries.put("selected_input_below_1048575_bytes", true);
        replaceDescription(near, charactersAtLimit + 1);
        assertThatThrownBy(() -> preparedWithExactConfirmation(near))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("byte budget");
        boundaries.put("selected_input_above_1048577_bytes_rejected", true);
        replaceDescription(near, charactersAtLimit);
        assertThat(bytes(preparedWithExactConfirmation(near).payload())).isEqualTo(INPUT_LIMIT);
        boundaries.put("selected_input_at_1048576_bytes", true);

        return List.of(workload("small", small, 1, 1, false),
            workload("maximum_selected_sources", star, 100, 2, false),
            workload("maximum_dependency_depth", chain.getFirst(), 5, 5, false),
            workload("maximum_selected_input_bytes", near, 1, 1, true));
    }

    private Workload workload(String name, RagSourceView source, int count, int depth, boolean confirmation) {
        RagSourceAuthority.Prepared prepared = confirmation ? preparedWithExactConfirmation(source)
            : authority.prepareSource(source.id(), Map.of());
        assertThat(prepared.proofs()).hasSize(count);
        return new Workload(name, source.id(), count, depth, bytes(prepared.payload()), confirmation);
    }

    private RagSourceView film(String title, boolean description) {
        Long owner = jdbcTemplate.queryForObject(
            "INSERT INTO vault_entries(entry_type) VALUES ('FILM') RETURNING id", Long.class);
        jdbcTemplate.update("""
            INSERT INTO films(id,title,format,production_style,is_nsfw,description)
            VALUES (?,?,'MOVIE','LIVE_ACTION',false,?)
            """, owner, title, description ? "A" : null);
        RagSourceView candidate = RagTestCommands.register(enabledRag, RagSourceType.FILM, owner.toString());
        return RagTestCommands.approve(enabledRag, candidate.id(),
            description ? Set.of("title", "description") : Set.of("title", "format"));
    }

    private void replaceDescription(RagSourceView source, int length) {
        assertThat(jdbcTemplate.update("UPDATE films SET description=? WHERE id=?",
            "A".repeat(length), Long.parseLong(source.sourceId()))).isEqualTo(1);
    }

    private RagSourceAuthority.Prepared preparedWithExactConfirmation(RagSourceView source) {
        RagSourceView current = enabledRag.findSource(source.id()).orElseThrow();
        return authority.prepareSource(source.id(), Map.of(source.id(), current.currentDescriptionRevisionHash()));
    }

    private void run(Workload fixture) {
        List<Map<String, Object>> measured = new ArrayList<>();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("name", fixture.name());
        summary.put("selected_sources", fixture.selectedSources());
        summary.put("dependency_depth_nodes", fixture.depth());
        summary.put("selected_input_bytes", fixture.payloadBytes());
        summary.put("resources_before", resourceSnapshot());
        workloads.add(summary);
        try {
            for (int iteration = 0; iteration < WARMUPS + MEASUREMENTS; iteration++) {
                boolean warmup = iteration < WARMUPS;
                Map<String, Object> sample = runOne(fixture, iteration, warmup);
                if (!warmup) {
                    measured.add(sample);
                }
            }
        } finally {
            List<Map<String, Object>> attempts = samples.stream()
                .filter(sample -> fixture.name().equals(sample.get("workload"))).toList();
            summary.put("attempted_jobs", attempts.size());
            summary.put("successful_jobs", attempts.stream().filter(row -> Boolean.TRUE.equals(row.get("success"))).count());
            summary.put("failed_jobs", attempts.stream().filter(row -> Boolean.FALSE.equals(row.get("success"))).count());
            summary.put("measured_success_count", measured.size());
            for (String metric : List.of("admission_ms", "cold_worker_stage_and_stop_ms", "native_resource_query_ms",
                "publication_ms", "total_ms", "java_process_cpu_ms", "hibernate_prepared_statements",
                "native_job_peak_committed_bytes", "native_process_peak_committed_bytes", "native_worker_cpu_ms",
                "native_read_operations", "native_write_operations", "native_read_transfer_bytes", "native_write_transfer_bytes")) {
                summary.put(metric, percentiles(measured, metric));
            }
            summary.put("resources_after", resourceSnapshot());
        }
    }

    private Map<String, Object> runOne(Workload fixture, int iteration, boolean warmup) {
        Map<String, Object> sample = new LinkedHashMap<>();
        sample.put("workload", fixture.name());
        sample.put("iteration", iteration);
        sample.put("warmup", warmup);
        sample.put("success", false);
        samples.add(sample);
        RagJobAdmissionResult admitted = null;
        long start = System.nanoTime();
        long cpu = operatingSystem().getProcessCpuTime();
        long statements = statistics.getPrepareStatementCount();
        try {
            if (fixture.confirmChangedDescription() && iteration == 0) {
                RagSourceView current = enabledRag.findSource(fixture.sourceId()).orElseThrow();
                admitted = enabledRag.admitJob(fixture.sourceId(), new RagIndexCommand(current.sourceVersion(),
                    current.authorizationEpoch(), current.fieldPolicyVersion(),
                    Map.of(current.id(), current.currentDescriptionRevisionHash())));
            } else {
                admitted = RagTestCommands.index(enabledRag, fixture.sourceId());
            }
            long admission = System.nanoTime();
            sample.put("last_completed_phase", "admission");
            enabledRag.executeSyntheticStage(admitted.jobId(), admitted.attemptId(), "SYNTHETIC_TEST");
            long stage = System.nanoTime();
            sample.put("last_completed_phase", "worker_stage_and_stop");
            NativeWorkerProcess.ResourceUsage nativeUsage = supervisor.stoppedResources(admitted.jobId());
            long accounted = System.nanoTime();
            assertThat(nativeUsage.peakJobMemoryBytes()).isPositive().isLessThanOrEqualTo(512L * 1024 * 1024);
            assertThat(nativeUsage.peakProcessMemoryBytes()).isPositive().isLessThanOrEqualTo(512L * 1024 * 1024);
            sample.put("native_job_peak_committed_bytes", nativeUsage.peakJobMemoryBytes());
            sample.put("native_process_peak_committed_bytes", nativeUsage.peakProcessMemoryBytes());
            sample.put("native_user_cpu_100ns", nativeUsage.totalUserCpu100ns());
            sample.put("native_kernel_cpu_100ns", nativeUsage.totalKernelCpu100ns());
            sample.put("native_worker_cpu_ms", (nativeUsage.totalUserCpu100ns() + nativeUsage.totalKernelCpu100ns()) / 10_000.0);
            sample.put("native_read_operations", nativeUsage.readOperations());
            sample.put("native_write_operations", nativeUsage.writeOperations());
            sample.put("native_read_transfer_bytes", nativeUsage.readTransferBytes());
            sample.put("native_write_transfer_bytes", nativeUsage.writeTransferBytes());
            var publication = enabledRag.finalizeGeneration(admitted.jobId(), admitted.attemptId(), null);
            long published = System.nanoTime();
            sample.put("admission_ms", millis(admission - start));
            sample.put("cold_worker_stage_and_stop_ms", millis(stage - admission));
            sample.put("native_resource_query_ms", millis(accounted - stage));
            sample.put("publication_ms", millis(published - accounted));
            sample.put("total_ms", millis(published - start));
            sample.put("java_process_cpu_ms", millis(operatingSystem().getProcessCpuTime() - cpu));
            sample.put("hibernate_prepared_statements", statistics.getPrepareStatementCount() - statements);
            sample.put("last_completed_phase", "publication_commit_ack");
            assertThat(publication.active()).isTrue();
            assertThat(publication.status()).isEqualTo("PUBLISHED");
            var status = enabledRag.getJobStatus(admitted.jobId());
            assertThat(status.status()).isEqualTo("COMPLETED");
            assertThat(status.indexState()).isEqualTo("CURRENT");
            Map<String, Object> worker = jdbcTemplate.queryForMap("""
                SELECT (worker_identity->>'process_id')::bigint AS pid,
                       worker_identity->>'creation_file_time' AS created,
                       staging_closed FROM rag.job_leases WHERE job_id=? AND attempt_id=?
                """, admitted.jobId(), admitted.attemptId());
            long pid = ((Number) worker.get("pid")).longValue();
            String creation = worker.get("created").toString();
            String identity = pid + ":" + creation;
            assertThat(identity).isNotEqualTo(previousProcessIdentity);
            assertThat(worker.get("staging_closed")).isEqualTo(true);
            assertThat(NativeWorkerProcess.isRecordedProcessTerminated(pid, Long.parseLong(creation))).isTrue();
            previousProcessIdentity = identity;
            sample.put("worker_process_id", pid);
            sample.put("worker_creation_file_time", creation);
            sample.put("java_heap_used_bytes", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
            sample.put("sql_client_connections_at_boundary", clientConnections());
            sample.put("success", true);
            return sample;
        } catch (RuntimeException | AssertionError failure) {
            sample.put("failure_type", failure.getClass().getName());
            sample.put("elapsed_until_failure_ms", millis(System.nanoTime() - start));
            if (admitted != null) {
                try {
                    enabledRag.cancelJob(admitted.jobId());
                } catch (RuntimeException cleanupFailure) {
                    sample.put("safe_cancellation_failure_type", cleanupFailure.getClass().getName());
                }
            }
            throw failure;
        }
    }

    private Map<String, Object> environment() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("os_name", System.getProperty("os.name"));
        values.put("os_version", System.getProperty("os.version"));
        values.put("architecture", System.getProperty("os.arch"));
        values.put("java_version", System.getProperty("java.runtime.version"));
        values.put("available_processors", Runtime.getRuntime().availableProcessors());
        values.put("processor_identifier", System.getenv().getOrDefault("PROCESSOR_IDENTIFIER", "UNAVAILABLE"));
        values.put("host_physical_memory_bytes", operatingSystem().getTotalMemorySize());
        values.put("postgresql_version", jdbcTemplate.queryForObject("SELECT version()", String.class));
        values.put("worker_versions_and_licenses", "See dependency-inventory.json from the same pinned installed environment.");
        return values;
    }

    private Map<String, Object> resourceSnapshot() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("java_heap_used_bytes", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
        values.put("java_committed_virtual_bytes", operatingSystem().getCommittedVirtualMemorySize());
        values.put("java_process_cpu_nanos", operatingSystem().getProcessCpuTime());
        values.put("java_live_threads", ManagementFactory.getThreadMXBean().getThreadCount());
        values.put("host_available_physical_memory_bytes", operatingSystem().getFreeMemorySize());
        try {
            values.put("sql_client_connections", clientConnections());
            values.put("postgresql_database_bytes", jdbcTemplate.queryForObject("SELECT pg_database_size(current_database())", Long.class));
        } catch (RuntimeException unavailable) {
            values.put("sql_resource_metrics", "NOT_VERIFIED");
            values.put("sql_resource_failure_type", unavailable.getClass().getName());
        }
        values.put("hibernate_prepared_statements", statistics.getPrepareStatementCount());
        try {
            values.put("workspace_disk_usable_bytes", Files.getFileStore(repository()).getUsableSpace());
        } catch (java.io.IOException unavailable) {
            values.put("workspace_disk_usable_bytes", "NOT_VERIFIED");
        }
        return values;
    }

    private Long clientConnections() {
        return jdbcTemplate.queryForObject("""
            SELECT count(*) FROM pg_catalog.pg_stat_activity
            WHERE datname=current_database() AND backend_type='client backend'
            """, Long.class);
    }

    private static com.sun.management.OperatingSystemMXBean operatingSystem() {
        return (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    private static int bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }

    private static double millis(long nanos) {
        return nanos / 1_000_000.0;
    }

    private static Map<String, Object> percentiles(List<Map<String, Object>> values, String metric) {
        List<Double> sorted = values.stream().map(row -> ((Number) row.get(metric)).doubleValue()).sorted().toList();
        if (sorted.isEmpty()) {
            return Map.of("count", 0, "status", "NO_SUCCESSFUL_MEASUREMENTS");
        }
        return Map.of("count", sorted.size(), "p50", sorted.get((int) Math.ceil(sorted.size() * 0.50) - 1),
            "p95", sorted.get((int) Math.ceil(sorted.size() * 0.95) - 1), "maximum", sorted.getLast(),
            "minimum", sorted.getFirst());
    }
}
