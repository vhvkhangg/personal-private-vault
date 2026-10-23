package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 20)
class RagNativeWorkerProcessTest {
    private static final Duration STOP_TIMEOUT = Duration.ofSeconds(5);
    private static final long MEMORY_BYTES = 512L * 1024 * 1024;

    @TempDir
    Path directory;

    @Test
    void suspendedChildCannotConsumeBeforeBindingAndResume() throws Exception {
        Path marker = directory.resolve("bound.txt");
        Path script = fixture("""
            import os, pathlib, sys, time
            marker = pathlib.Path(sys.argv[1])
            marker.with_suffix('.tmp').write_text(str(os.getpid()), encoding='ascii')
            os.replace(marker.with_suffix('.tmp'), marker)
            time.sleep(30)
            """);
        try (WatchService changes = watch();
             NativeWorkerProcess child = launch(UUID.randomUUID(), script, marker)) {
            try {
                assertThat(Files.exists(marker)).isFalse();
                assertThat(child.hasTerminatedTree()).isFalse();
                assertThat(child.ownsProcess(child.pid(), child.creationTimeFileTime())).isTrue();
                assertThat(child.ownsProcess(child.pid(), child.creationTimeFileTime() + 1)).isFalse();
                assertThat(NativeWorkerProcess.isRecordedProcessTerminated(
                    child.pid(), child.creationTimeFileTime())).isFalse();
                child.resume();
                awaitMarker(changes, marker);
                assertThat(Files.readString(marker)).isEqualTo(Long.toString(child.pid()));
                assertThatThrownBy(child::resume).isInstanceOf(IllegalStateException.class);
            } finally {
                assertThat(child.terminateAndVerify(STOP_TIMEOUT)).isTrue();
            }
            assertThat(child.hasTerminatedTree()).isTrue();
            assertThat(NativeWorkerProcess.isRecordedProcessTerminated(
                child.pid(), child.creationTimeFileTime())).isTrue();
        }
    }

    @Test
    void singleProcessLimitDeniesNormalAndBreakawayChildBodies() throws Exception {
        Path marker = directory.resolve("limits.txt");
        Path script = fixture("""
            import os, pathlib, subprocess, sys, time
            marker = pathlib.Path(sys.argv[1])
            forbidden = marker.with_suffix('.forbidden')
            results = []
            for flag in (0, subprocess.CREATE_BREAKAWAY_FROM_JOB):
                try:
                    child = subprocess.Popen([
                        sys._base_executable, '-c',
                        'import pathlib,sys; pathlib.Path(sys.argv[1]).write_text(str(1))',
                        str(forbidden)], creationflags=flag)
                except OSError as error:
                    results.append(str(error.winerror))
                else:
                    results.append('CHILD_STARTED:' + str(child.wait(timeout=2)))
                if forbidden.exists():
                    results.append('UNAUTHORIZED_CHILD_BODY')
            marker.with_suffix('.tmp').write_text(','.join(results), encoding='ascii')
            os.replace(marker.with_suffix('.tmp'), marker)
            time.sleep(30)
            """);
        try (WatchService changes = watch();
             NativeWorkerProcess child = launch(UUID.randomUUID(), script, marker)) {
            try {
                child.resume();
                awaitMarker(changes, marker);
                assertThat(Files.readString(marker)).isEqualTo("1816,5");
                assertThat(Files.exists(directory.resolve("limits.forbidden"))).isFalse();
            } finally {
                assertThat(child.terminateAndVerify(STOP_TIMEOUT)).isTrue();
            }
        }
    }

    @Test
    void duplicateNamedJobCannotAdoptExistingWorker() throws Exception {
        Path script = fixture("raise SystemExit(0)\n");
        UUID id = UUID.randomUUID();
        try (NativeWorkerProcess original = launch(id, script, directory.resolve("unused"))) {
            try {
                assertThatThrownBy(() -> launch(id, script, directory.resolve("duplicate")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("identity already exists");
                assertThat(original.hasTerminatedTree()).isFalse();
                assertThat(original.ownsProcess(original.pid(), original.creationTimeFileTime())).isTrue();
            } finally {
                assertThat(original.terminateAndVerify(STOP_TIMEOUT)).isTrue();
            }
        }
    }

    @Test
    void finalHandleCloseActuallyStopsRecordedProcessButMissingJobIsNotRecoveryProof() throws Exception {
        Path script = fixture("import time; time.sleep(30)\n");
        UUID id = UUID.randomUUID();
        NativeWorkerProcess child = launch(id, script, directory.resolve("unused"));
        long pid = child.pid();
        long fileTime = child.creationTimeFileTime();
        ProcessHandle actual = ProcessHandle.of(pid).orElseThrow();
        child.close();
        actual.onExit().get(5, TimeUnit.SECONDS);
        assertThat(NativeWorkerProcess.isRecordedProcessTerminated(pid, fileTime, STOP_TIMEOUT)).isTrue();
        assertThat(NativeWorkerProcess.recoverTerminated(id, STOP_TIMEOUT)).isFalse();
        assertThatThrownBy(child::hasTerminatedTree).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void explicitlyOwnedRecoveryWaitsUntilActualTreeIsEmpty() throws Exception {
        Path script = fixture("import time; time.sleep(30)\n");
        UUID id = UUID.randomUUID();
        try (NativeWorkerProcess child = launch(id, script, directory.resolve("unused"))) {
            try {
                child.resume();
                assertThat(NativeWorkerProcess.recoverTerminated(id, STOP_TIMEOUT)).isTrue();
                assertThat(child.hasTerminatedTree()).isTrue();
            } finally {
                assertThat(child.terminateAndVerify(STOP_TIMEOUT)).isTrue();
            }
        }
    }

    @Test
    void windowsArgumentQuotingPreservesEmptySpaceQuoteAndTrailingSlash() throws Exception {
        Path marker = directory.resolve("arguments.txt");
        Path script = fixture("""
            import json, os, pathlib, sys, time
            marker = pathlib.Path(sys.argv[1])
            marker.with_suffix('.tmp').write_text(json.dumps(sys.argv[2:]), encoding='ascii')
            os.replace(marker.with_suffix('.tmp'), marker)
            time.sleep(30)
            """);
        List<String> arguments = new ArrayList<>(List.of(script.toString(), marker.toString()));
        arguments.addAll(List.of("", "a b", "quote\"value", "trailing\\"));
        try (WatchService changes = watch();
             NativeWorkerProcess child = NativeWorkerProcess.launchSuspended(
                 UUID.randomUUID(), python(), arguments, environment(), directory, MEMORY_BYTES, 1)) {
            try {
                child.resume();
                awaitMarker(changes, marker);
                assertThat(Files.readString(marker))
                    .isEqualTo("[\"\", \"a b\", \"quote\\\"value\", \"trailing\\\\\"]");
            } finally {
                assertThat(child.terminateAndVerify(STOP_TIMEOUT)).isTrue();
            }
        }
    }

    @Test
    void stoppedOwnedJobRetainsActualCpuCommittedMemoryAndIoAccounting() throws Exception {
        Path marker = directory.resolve("accounted.txt");
        Path script = fixture("""
            import hashlib, os, pathlib, sys, time
            marker = pathlib.Path(sys.argv[1])
            content = bytearray(b'A' * (8 * 1024 * 1024))
            data = marker.with_suffix('.data')
            data.write_bytes(content)
            assert len(data.read_bytes()) == len(content)
            assert hashlib.sha256(content).digest()
            data.unlink()
            marker.with_suffix('.tmp').write_text('ready', encoding='ascii')
            os.replace(marker.with_suffix('.tmp'), marker)
            time.sleep(30)
            """);
        try (WatchService changes = watch();
             NativeWorkerProcess child = launch(UUID.randomUUID(), script, marker)) {
            try {
                assertThatThrownBy(child::resourceUsage).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("verified termination");
                child.resume();
                awaitMarker(changes, marker);
            } finally {
                assertThat(child.terminateAndVerify(STOP_TIMEOUT)).isTrue();
            }
            NativeWorkerProcess.ResourceUsage usage = child.resourceUsage();
            assertThat(usage.peakJobMemoryBytes()).isPositive().isLessThanOrEqualTo(MEMORY_BYTES);
            assertThat(usage.peakProcessMemoryBytes()).isPositive().isLessThanOrEqualTo(MEMORY_BYTES);
            assertThat(usage.totalUserCpu100ns()).isGreaterThanOrEqualTo(0);
            assertThat(usage.totalKernelCpu100ns()).isGreaterThanOrEqualTo(0);
            assertThat(usage.totalUserCpu100ns() + usage.totalKernelCpu100ns()).isPositive();
            assertThat(usage.readOperations()).isPositive();
            assertThat(usage.writeOperations()).isPositive();
            assertThat(usage.readTransferBytes()).isGreaterThanOrEqualTo(8L * 1024 * 1024);
            assertThat(usage.writeTransferBytes()).isGreaterThanOrEqualTo(8L * 1024 * 1024);
            assertThat(child.resourceUsage()).isEqualTo(usage);
        }
    }

    @Test
    void invalidEnvironmentAndUnboundedWaitFailWithoutClaimingDeath() throws Exception {
        Path script = fixture("raise SystemExit(0)\n");
        Map<String, String> invalid = new HashMap<>(environment());
        invalid.put("INVALID=KEY", "value");
        assertThatThrownBy(() -> NativeWorkerProcess.launchSuspended(UUID.randomUUID(),
            python(), List.of(script.toString()), invalid, directory, MEMORY_BYTES, 1))
            .isInstanceOf(IllegalArgumentException.class);
        try (NativeWorkerProcess child = launch(UUID.randomUUID(), script, directory.resolve("unused"))) {
            try {
                assertThatThrownBy(() -> child.terminateAndVerify(Duration.ofMinutes(1)))
                    .isInstanceOf(IllegalArgumentException.class);
                assertThat(child.hasTerminatedTree()).isFalse();
            } finally {
                assertThat(child.terminateAndVerify(STOP_TIMEOUT)).isTrue();
            }
        }
    }

    @Test
    void liveConsumerUnprovenTerminationReturnsFalseAndDoesNotClaimTreeTerminated() throws Exception {
        Path marker = directory.resolve("live.txt");
        Path script = fixture("""
            import os, pathlib, sys, time
            marker = pathlib.Path(sys.argv[1])
            marker.with_suffix('.tmp').write_text(str(os.getpid()), encoding='ascii')
            os.replace(marker.with_suffix('.tmp'), marker)
            time.sleep(30)
            """);
        UUID jobId = UUID.randomUUID();
        try (WatchService changes = watch();
             NativeWorkerProcess child = launch(jobId, script, marker)) {
            try {
                child.resume();
                awaitMarker(changes, marker);
                assertThat(child.hasTerminatedTree()).isFalse();
                assertThat(child.ownsProcess(child.pid(), child.creationTimeFileTime())).isTrue();
                assertThat(NativeWorkerProcess.isRecordedProcessTerminated(
                    child.pid(), child.creationTimeFileTime())).isFalse();

                assertThatThrownBy(() -> child.terminateAndVerify(Duration.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
                assertThat(child.hasTerminatedTree()).isFalse();
                assertThat(NativeWorkerProcess.isRecordedProcessTerminated(
                    child.pid(), child.creationTimeFileTime())).isFalse();

                assertThat(NativeWorkerProcess.recoverTerminated(UUID.randomUUID(), Duration.ofMillis(100))).isFalse();
            } finally {
                assertThat(child.terminateAndVerify(STOP_TIMEOUT)).isTrue();
            }
            assertThat(child.hasTerminatedTree()).isTrue();
            assertThat(NativeWorkerProcess.isRecordedProcessTerminated(
                child.pid(), child.creationTimeFileTime())).isTrue();
        }
    }

    private NativeWorkerProcess launch(UUID job, Path script, Path marker) throws IOException {
        return NativeWorkerProcess.launchSuspended(job, python(),
            List.of(script.toString(), marker.toString()), environment(), directory, MEMORY_BYTES, 1);
    }

    private Path fixture(String contents) throws IOException {
        Path script = directory.resolve(UUID.randomUUID() + ".py");
        Files.writeString(script, contents);
        return script;
    }

    private WatchService watch() throws IOException {
        WatchService watcher = directory.getFileSystem().newWatchService();
        directory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        return watcher;
    }

    private void awaitMarker(WatchService watcher, Path marker) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!Files.exists(marker)) {
            long remaining = end - System.nanoTime();
            assertThat(remaining).as("native worker readiness deadline").isPositive();
            var key = watcher.poll(remaining, TimeUnit.NANOSECONDS);
            assertThat(key).as("actual child readiness event").isNotNull();
            key.pollEvents();
            assertThat(key.reset()).isTrue();
        }
    }

    private static Path repository() {
        return Files.isDirectory(Path.of("rag-engine")) ? Path.of(".").toAbsolutePath()
            : Path.of("..").toAbsolutePath().normalize();
    }

    private static Path venvPath() {
        Path d17 = repository().resolve("rag-engine/.venv-d17");
        if (Files.isDirectory(d17) && Files.isRegularFile(d17.resolve("Scripts/python.exe"))) {
            return d17;
        }
        return repository().resolve("rag-engine/.venv");
    }

    private static Path python() throws IOException {
        assertThat(System.getProperty("os.name")).startsWith("Windows");
        Path config = venvPath().resolve("pyvenv.cfg");
        String home = Files.readAllLines(config).stream().filter(line -> line.startsWith("home = "))
            .map(line -> line.substring(7)).findFirst().orElseThrow();
        Path executable = Path.of(home).resolve("python.exe");
        assertThat(executable).isRegularFile();
        return executable;
    }

    private static Map<String, String> environment() {
        Map<String, String> result = new HashMap<>();
        for (String key : List.of("SystemRoot", "WINDIR", "TEMP", "TMP")) {
            String value = System.getenv(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        result.put("__PYVENV_LAUNCHER__",
            venvPath().resolve("Scripts/python.exe").toString());
        result.put("PYTHONNOUSERSITE", "1");
        result.put("PYTHONDONTWRITEBYTECODE", "1");
        return result;
    }
}
