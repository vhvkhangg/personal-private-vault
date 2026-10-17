package com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

/**
 * Windows x64 process ownership, deliberately independent of source authorization.
 * A caller must durably record STARTING before launch and bind this exact identity
 * before resume. Closing a handle is never returned as proof of process death.
 */
public final class NativeWorkerProcess implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(NativeWorkerProcess.class);
    private static final int WAIT_OBJECT_0 = 0;
    private static final int WAIT_TIMEOUT = 258;
    private static final int ERROR_ALREADY_EXISTS = 183;
    private static final int ERROR_FILE_NOT_FOUND = 2;
    private static final long FILETIME_UNIX_EPOCH = 116_444_736_000_000_000L;
    private static final int BASIC_AND_IO_ACCOUNTING_BYTES = 96;
    private static final int EXTENDED_LIMIT_INFORMATION_BYTES = 144;

    private MemorySegment job;
    private MemorySegment process;
    private MemorySegment thread;
    private final long pid;
    private final long creationTimeFileTime;
    private boolean resumed;
    private boolean closed;

    private NativeWorkerProcess(MemorySegment job, MemorySegment process,
                                MemorySegment thread, long pid, long creationTimeFileTime) {
        this.job = job;
        this.process = process;
        this.thread = thread;
        this.pid = pid;
        this.creationTimeFileTime = creationTimeFileTime;
    }

    public static NativeWorkerProcess launchSuspended(UUID jobId, Path executable,
                                                      List<String> arguments, Map<String, String> environment, Path workingDirectory,
                                                      long memoryBytes, int maxProcesses) {
        requirePlatform();
        Objects.requireNonNull(jobId);
        Objects.requireNonNull(arguments);
        Objects.requireNonNull(environment);
        Path binary = executable.toAbsolutePath().normalize();
        Path directory = workingDirectory.toAbsolutePath().normalize();
        if (!Files.isRegularFile(binary) || !Files.isDirectory(directory)
            || memoryBytes < 16L * 1024 * 1024 || memoryBytes > 8L * 1024 * 1024 * 1024
            || maxProcesses < 1 || maxProcesses > 4) {
            throw new IllegalArgumentException("Invalid native worker configuration");
        }
        List<String> command = new ArrayList<>();
        command.add(binary.toString());
        command.addAll(arguments);
        String commandLine = command.stream().map(NativeWorkerProcess::quoteArgument)
            .collect(java.util.stream.Collectors.joining(" "));
        if (commandLine.length() > 30_000) {
            throw new IllegalArgumentException("Native worker command exceeds bound");
        }
        NativeWorkerProcess owned = null;
        MemorySegment job = MemorySegment.NULL;
        MemorySegment process = MemorySegment.NULL;
        MemorySegment thread = MemorySegment.NULL;
        try (Arena arena = Arena.ofConfined()) {
            NativeResult created = Kernel.call(Kernel.CREATE_JOB,
                MemorySegment.NULL, wide(arena, jobName(jobId)));
            job = created.handle();
            if (job.address() == 0) {
                throw failure("CreateJobObjectW", created.error());
            }
            if (created.error() == ERROR_ALREADY_EXISTS) {
                throw new IllegalStateException("Native job identity already exists");
            }
            MemorySegment limits = arena.allocate(144, 8);
            limits.set(ValueLayout.JAVA_INT, 16, 0x2000 | 0x200 | 0x8);
            limits.set(ValueLayout.JAVA_INT, 40, maxProcesses);
            limits.set(ValueLayout.JAVA_LONG, 120, memoryBytes);
            Kernel.require(Kernel.SET_JOB, "SetInformationJobObject", job, 9, limits, 144);
            MemorySegment startup = arena.allocate(104, 8);
            startup.set(ValueLayout.JAVA_INT, 0, 104);
            startup.set(ValueLayout.JAVA_INT, 60, 1);
            MemorySegment information = arena.allocate(24, 8);
            Kernel.require(Kernel.CREATE_PROCESS, "CreateProcessW", wide(arena, binary.toString()),
                wide(arena, commandLine), MemorySegment.NULL, MemorySegment.NULL, 0,
                0x08000404, environmentBlock(arena, environment),
                wide(arena, directory.toString()), startup, information);
            process = information.get(ValueLayout.ADDRESS, 0);
            thread = information.get(ValueLayout.ADDRESS, 8);
            long pid = Integer.toUnsignedLong(information.get(ValueLayout.JAVA_INT, 16));
            Kernel.require(Kernel.ASSIGN, "AssignProcessToJobObject", job, process);
            verifyNotInherited(arena, job);
            verifyNotInherited(arena, process);
            verifyNotInherited(arena, thread);
            MemorySegment inJob = arena.allocate(4, 4);
            Kernel.require(Kernel.IN_JOB, "IsProcessInJob", process, job, inJob);
            if (inJob.get(ValueLayout.JAVA_INT, 0) == 0) {
                throw new IllegalStateException("Native worker confinement not established");
            }
            MemorySegment times = arena.allocate(32, 8);
            Kernel.require(Kernel.TIMES, "GetProcessTimes", process, times,
                times.asSlice(8), times.asSlice(16), times.asSlice(24));
            owned = new NativeWorkerProcess(job, process, thread, pid,
                times.get(ValueLayout.JAVA_LONG, 0));
            if (owned.activeProcesses() != 1 || owned.processWait(0) != WAIT_TIMEOUT) {
                throw new IllegalStateException("Native suspended ownership not established");
            }
            return owned;
        } catch (RuntimeException | Error exception) {
            if (owned != null) {
                try {
                    owned.terminateAndVerify(Duration.ofSeconds(5));
                    owned.close();
                } catch (RuntimeException cleanup) {
                    exception.addSuppressed(cleanup);
                }
            } else {
                cleanupFailedLaunch(job, process, thread, exception);
            }
            throw exception;
        }
    }

    public long pid() {
        return pid;
    }

    public long creationTimeFileTime() {
        return creationTimeFileTime;
    }

    public java.time.Instant creationTime() {
        long ticks = Math.subtractExact(creationTimeFileTime, FILETIME_UNIX_EPOCH);
        return java.time.Instant.ofEpochSecond(Math.floorDiv(ticks, 10_000_000),
            Math.floorMod(ticks, 10_000_000) * 100);
    }

    /**
     * Cumulative counters for this retained Windows Job Object, including its terminated
     * processes. Peak memory is committed memory, not a resident working-set measurement;
     * I/O is the Windows job counter, not physical-disk-only traffic.
     */
    public record ResourceUsage(long peakJobMemoryBytes, long peakProcessMemoryBytes,
                                long totalUserCpu100ns, long totalKernelCpu100ns,
                                long readOperations, long writeOperations,
                                long readTransferBytes, long writeTransferBytes) {
        public ResourceUsage {
            if (peakJobMemoryBytes < 0 || peakProcessMemoryBytes < 0 || totalUserCpu100ns < 0
                || totalKernelCpu100ns < 0 || readOperations < 0 || writeOperations < 0
                || readTransferBytes < 0 || writeTransferBytes < 0) {
                throw new IllegalStateException("Native resource counter exceeds supported bounds");
            }
        }
    }

    /**
     * Query only after positive process/tree termination and before closing the owned job handle.
     */
    public synchronized ResourceUsage resourceUsage() {
        requireOpen();
        if (!hasTerminatedTree()) {
            throw new IllegalStateException("Native resource accounting requires verified termination");
        }
        try (Arena arena = Arena.ofConfined()) {
            // Microsoft winnt.h/x64 ABI: four LARGE_INTEGER + four DWORD = 48;
            // IO_COUNTERS is six ULONGLONG = 48. Combined accounting class 8 = 96.
            // https://learn.microsoft.com/windows/win32/api/winnt/ns-winnt-jobobject_basic_and_io_accounting_information
            MemorySegment accounting = arena.allocate(BASIC_AND_IO_ACCOUNTING_BYTES, 8);
            MemorySegment returned = arena.allocate(4, 4);
            Kernel.require(Kernel.QUERY_JOB, "QueryInformationJobObject", job, 8,
                accounting, BASIC_AND_IO_ACCOUNTING_BYTES, returned);
            if (returned.get(ValueLayout.JAVA_INT, 0) != BASIC_AND_IO_ACCOUNTING_BYTES) {
                throw new IllegalStateException("Native resource accounting layout is unsupported");
            }
            // BasicLimit (64), IO_COUNTERS (48), four x64 SIZE_T values (32) = 144.
            // PeakProcessMemoryUsed and PeakJobMemoryUsed are at 128 and 136.
            // https://learn.microsoft.com/windows/win32/api/winnt/ns-winnt-jobobject_extended_limit_information
            MemorySegment extended = arena.allocate(EXTENDED_LIMIT_INFORMATION_BYTES, 8);
            Kernel.require(Kernel.QUERY_JOB, "QueryInformationJobObject", job, 9,
                extended, EXTENDED_LIMIT_INFORMATION_BYTES, returned);
            if (returned.get(ValueLayout.JAVA_INT, 0) != EXTENDED_LIMIT_INFORMATION_BYTES) {
                throw new IllegalStateException("Native resource limit layout is unsupported");
            }
            return new ResourceUsage(extended.get(ValueLayout.JAVA_LONG, 136),
                extended.get(ValueLayout.JAVA_LONG, 128),
                accounting.get(ValueLayout.JAVA_LONG, 0), accounting.get(ValueLayout.JAVA_LONG, 8),
                accounting.get(ValueLayout.JAVA_LONG, 48), accounting.get(ValueLayout.JAVA_LONG, 56),
                accounting.get(ValueLayout.JAVA_LONG, 72), accounting.get(ValueLayout.JAVA_LONG, 80));
        }
    }

    public synchronized void resume() {
        requireOpen();
        if (resumed) {
            throw new IllegalStateException("Native worker already resumed");
        }
        int previous = Kernel.call(Kernel.RESUME, thread).integer();
        if (previous != 1) {
            throw new IllegalStateException("Native worker suspension state is uncertain");
        }
        resumed = true;
    }

    public synchronized boolean hasTerminatedTree() {
        requireOpen();
        return processWait(0) == WAIT_OBJECT_0 && activeProcesses() == 0;
    }

    public synchronized boolean ownsProcess(long expectedPid, long expectedCreationFileTime) {
        requireOpen();
        if (expectedPid <= 0 || expectedPid > 0xFFFF_FFFFL || expectedCreationFileTime <= 0) {
            return false;
        }
        NativeResult opened = Kernel.call(Kernel.OPEN_PROCESS, 0x00101000, 0, (int) expectedPid);
        MemorySegment candidate = opened.handle();
        if (candidate.address() == 0) {
            return false;
        }
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment result = arena.allocate(4, 4);
            Kernel.require(Kernel.IN_JOB, "IsProcessInJob", candidate, job, result);
            MemorySegment times = arena.allocate(32, 8);
            Kernel.require(Kernel.TIMES, "GetProcessTimes", candidate, times,
                times.asSlice(8), times.asSlice(16), times.asSlice(24));
            return result.get(ValueLayout.JAVA_INT, 0) != 0
                && times.get(ValueLayout.JAVA_LONG, 0) == expectedCreationFileTime;
        } finally {
            Kernel.close(candidate);
        }
    }

    public synchronized boolean terminateAndVerify(Duration timeout) {
        requireOpen();
        long end = deadline(timeout);
        Kernel.require(Kernel.TERMINATE_JOB, "TerminateJobObject", job, 1);
        int waited = processWait(remainingMillis(end));
        if (waited == WAIT_TIMEOUT) {
            return false;
        }
        if (waited != WAIT_OBJECT_0) {
            throw new IllegalStateException("Native worker wait outcome unknown");
        }
        return waitUntilEmpty(job, end);
    }

    /**
     * Call only after genuine controller death has independently been established.
     * A missing named job remains UNKNOWN, because last-close is asynchronous.
     */
    public static boolean recoverTerminated(UUID jobId, Duration timeout) {
        requirePlatform();
        long end = deadline(timeout);
        try (Arena arena = Arena.ofConfined()) {
            NativeResult opened = Kernel.call(Kernel.OPEN_JOB, 0x000C, 0,
                wide(arena, jobName(jobId)));
            MemorySegment job = opened.handle();
            if (job.address() == 0) {
                if (opened.error() == ERROR_FILE_NOT_FOUND) {
                    return false;
                }
                throw failure("OpenJobObjectW", opened.error());
            }
            MemorySegment recordedProcess = MemorySegment.NULL;
            try {
                verifyNotInherited(arena, job);
                MemorySegment limits = arena.allocate(144, 8);
                Kernel.require(Kernel.QUERY_JOB, "QueryInformationJobObject", job, 9,
                    limits, 144, MemorySegment.NULL);
                if (limits.get(ValueLayout.JAVA_INT, 40) != 1
                    || (limits.get(ValueLayout.JAVA_INT, 16) & 0x1808) != 0x8) {
                    return false;
                }
                MemorySegment identities = arena.allocate(24, 8);
                Kernel.require(Kernel.QUERY_JOB, "QueryInformationJobObject", job, 3,
                    identities, 24, MemorySegment.NULL);
                if (identities.get(ValueLayout.JAVA_INT, 0) != 1
                    || identities.get(ValueLayout.JAVA_INT, 4) != 1) {
                    return false;
                }
                long recordedPid = identities.get(ValueLayout.JAVA_LONG, 8);
                if (recordedPid <= 0 || recordedPid > 0xFFFF_FFFFL) {
                    return false;
                }
                NativeResult processResult = Kernel.call(Kernel.OPEN_PROCESS,
                    0x00101000, 0, (int) recordedPid);
                recordedProcess = processResult.handle();
                if (recordedProcess.address() == 0) {
                    return false;
                }
                MemorySegment member = arena.allocate(4, 4);
                Kernel.require(Kernel.IN_JOB, "IsProcessInJob", recordedProcess, job, member);
                if (member.get(ValueLayout.JAVA_INT, 0) == 0) {
                    return false;
                }
                Kernel.require(Kernel.TERMINATE_JOB, "TerminateJobObject", job, 1);
                if (Kernel.call(Kernel.WAIT, recordedProcess, remainingMillis(end)).integer() != WAIT_OBJECT_0) {
                    return false;
                }
                return waitUntilEmpty(job, end);
            } finally {
                Kernel.close(recordedProcess);
                Kernel.close(job);
            }
        }
    }

    /**
     * Missing/reused identity proves only this recorded process cannot continue.
     */
    public static boolean isRecordedProcessTerminated(long pid, long creationFileTime) {
        return isRecordedProcessTerminated(pid, creationFileTime, Duration.ZERO);
    }

    /**
     * Confirms recorded process termination with a bounded wait on the native process handle.
     * Keeps unknown termination fail-closed.
     */
    public static boolean isRecordedProcessTerminated(long pid, long creationFileTime, Duration timeout) {
        requirePlatform();
        if (pid <= 0 || pid > 0xFFFF_FFFFL || creationFileTime <= 0) {
            return false;
        }
        Objects.requireNonNull(timeout, "timeout must not be null");
        if (timeout.isNegative() || timeout.compareTo(Duration.ofSeconds(30)) > 0) {
            throw new IllegalArgumentException("Native wait must be within 30 seconds");
        }
        int timeoutMs = (int) timeout.toMillis();
        long end = timeoutMs > 0 ? deadline(timeout) : 0;
        NativeResult opened = Kernel.call(Kernel.OPEN_PROCESS, 0x00101000, 0, (int) pid);
        MemorySegment handle = opened.handle();
        if (handle.address() == 0) {
            int err = opened.error();
            if (err != 87) {
                log.debug("OpenProcess for PID {} failed with Win32 error {}", pid, err);
            }
            return err == 87;
        }
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment times = arena.allocate(32, 8);
            Kernel.require(Kernel.TIMES, "GetProcessTimes", handle, times,
                times.asSlice(8), times.asSlice(16), times.asSlice(24));
            if (times.get(ValueLayout.JAVA_LONG, 0) != creationFileTime) {
                return true;
            }
            int waitMs = timeoutMs > 0 ? remainingMillis(end) : 0;
            int waitResult = Kernel.call(Kernel.WAIT, handle, waitMs).integer();
            if (waitResult != WAIT_OBJECT_0) {
                log.debug("WaitForSingleObject on recorded process PID {} returned {} (WAIT_OBJECT_0=0, WAIT_TIMEOUT=258)", pid, waitResult);
            }
            return waitResult == WAIT_OBJECT_0;
        } finally {
            Kernel.close(handle);
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        RuntimeException failure = null;
        MemorySegment[] handles = {thread, process, job};
        for (int index = 0; index < handles.length; index++) {
            try {
                Kernel.close(handles[index]);
                handles[index] = MemorySegment.NULL;
            } catch (RuntimeException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }
        thread = handles[0];
        process = handles[1];
        job = handles[2];
        closed = thread.address() == 0 && process.address() == 0 && job.address() == 0;
        if (failure != null) {
            throw failure;
        }
    }

    private int activeProcesses() {
        return activeProcesses(job);
    }

    private static int activeProcesses(MemorySegment job) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment accounting = arena.allocate(48, 8);
            Kernel.require(Kernel.QUERY_JOB, "QueryInformationJobObject", job, 1,
                accounting, 48, MemorySegment.NULL);
            return accounting.get(ValueLayout.JAVA_INT, 40);
        }
    }

    private int processWait(int milliseconds) {
        int result = Kernel.call(Kernel.WAIT, process, milliseconds).integer();
        if (result != WAIT_OBJECT_0 && result != WAIT_TIMEOUT) {
            throw new IllegalStateException("Native process wait failed");
        }
        return result;
    }

    private static boolean waitUntilEmpty(MemorySegment job, long end) {
        while (activeProcesses(job) != 0) {
            if (System.nanoTime() >= end || Thread.currentThread().isInterrupted()) {
                return false;
            }
            LockSupport.parkNanos(Math.min(10_000_000L, end - System.nanoTime()));
        }
        return true;
    }

    private static void cleanupFailedLaunch(MemorySegment job, MemorySegment process,
                                            MemorySegment thread, Throwable cause) {
        try {
            if (process.address() != 0) {
                Kernel.require(Kernel.TERMINATE_PROCESS, "TerminateProcess", process, 1);
                int waited = Kernel.call(Kernel.WAIT, process, 5000).integer();
                if (waited != WAIT_OBJECT_0) {
                    throw new IllegalStateException("Failed launch termination not verified");
                }
            }
        } catch (RuntimeException exception) {
            cause.addSuppressed(exception);
        } finally {
            for (MemorySegment handle : List.of(thread, process, job)) {
                try {
                    Kernel.close(handle);
                } catch (RuntimeException exception) {
                    cause.addSuppressed(exception);
                }
            }
        }
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("Native process ownership handle is closed");
        }
    }

    private static long deadline(Duration timeout) {
        Objects.requireNonNull(timeout);
        if (timeout.isNegative() || timeout.isZero() || timeout.compareTo(Duration.ofSeconds(30)) > 0) {
            throw new IllegalArgumentException("Native wait must be within 30 seconds");
        }
        return Math.addExact(System.nanoTime(), timeout.toNanos());
    }

    private static int remainingMillis(long end) {
        return (int) Math.max(0, Math.min(30_000, (end - System.nanoTime() + 999_999) / 1_000_000));
    }

    private static void requirePlatform() {
        if (!System.getProperty("os.name").startsWith("Windows")
            || !(System.getProperty("os.arch").equals("amd64")
            || System.getProperty("os.arch").equals("x86_64"))
            || ValueLayout.ADDRESS.byteSize() != 8
            || !NativeWorkerProcess.class.getModule().isNativeAccessEnabled()) {
            throw new IllegalStateException("Windows x64 native access is required for RAG");
        }
    }

    private static String jobName(UUID jobId) {
        return "Local\\PersonalPrivateVault.Rag." + Objects.requireNonNull(jobId);
    }

    private static void verifyNotInherited(Arena arena, MemorySegment handle) {
        MemorySegment flags = arena.allocate(4, 4);
        Kernel.require(Kernel.HANDLE_INFORMATION, "GetHandleInformation", handle, flags);
        if ((flags.get(ValueLayout.JAVA_INT, 0) & 1) != 0) {
            throw new IllegalStateException("Native process handle is inheritable");
        }
    }

    private static MemorySegment wide(Arena arena, String value) {
        if (value.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("NUL in native worker configuration");
        }
        byte[] encoded = (value + "\0").getBytes(StandardCharsets.UTF_16LE);
        MemorySegment result = arena.allocate(encoded.length, 2);
        result.copyFrom(MemorySegment.ofArray(encoded));
        return result;
    }

    private static MemorySegment environmentBlock(Arena arena, Map<String, String> environment) {
        Map<String, String> ordered = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map.Entry<String, String> entry : environment.entrySet()) {
            String key = Objects.requireNonNull(entry.getKey());
            String value = Objects.requireNonNull(entry.getValue());
            if (key.isEmpty() || key.indexOf('=') >= 0 || key.indexOf('\0') >= 0
                || value.indexOf('\0') >= 0 || ordered.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("Invalid native worker environment");
            }
        }
        StringBuilder block = new StringBuilder();
        ordered.forEach((key, value) -> block.append(key).append('=').append(value).append('\0'));
        block.append('\0');
        if (ordered.isEmpty()) {
            block.append('\0');
        }
        if (block.length() > 60_000) {
            throw new IllegalArgumentException("Native worker environment exceeds bound");
        }
        byte[] encoded = block.toString().getBytes(StandardCharsets.UTF_16LE);
        MemorySegment result = arena.allocate(encoded.length, 2);
        result.copyFrom(MemorySegment.ofArray(encoded));
        return result;
    }

    private static String quoteArgument(String argument) {
        Objects.requireNonNull(argument);
        if (argument.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("NUL in native worker argument");
        }
        StringBuilder quoted = new StringBuilder("\"");
        int slashes = 0;
        for (char character : argument.toCharArray()) {
            if (character == '\\') {
                slashes++;
            } else {
                if (character == '"') {
                    quoted.append("\\".repeat(slashes * 2 + 1));
                } else {
                    quoted.append("\\".repeat(slashes));
                }
                quoted.append(character);
                slashes = 0;
            }
        }
        return quoted.append("\\".repeat(slashes * 2)).append('"').toString();
    }

    private static IllegalStateException failure(String operation, int error) {
        return new IllegalStateException("Native worker " + operation + " failed: " + error);
    }

    private record NativeResult(Object value, int error) {
        int integer() {
            return (int) value;
        }

        MemorySegment handle() {
            return (MemorySegment) value;
        }
    }

    private static final class Kernel {
        private static final Linker LINKER = Linker.nativeLinker();
        private static final SymbolLookup LIBRARY = SymbolLookup.libraryLookup("Kernel32", Arena.global());
        private static final MemoryLayout CAPTURE = Linker.Option.captureStateLayout();
        private static final long LAST_ERROR = CAPTURE.byteOffset(MemoryLayout.PathElement.groupElement("GetLastError"));
        private static final ValueLayout.OfInt I = ValueLayout.JAVA_INT;
        private static final AddressLayout P = ValueLayout.ADDRESS;
        private static final MethodHandle CREATE_JOB = function("CreateJobObjectW", P, P, P);
        private static final MethodHandle OPEN_JOB = function("OpenJobObjectW", P, I, I, P);
        private static final MethodHandle SET_JOB = function("SetInformationJobObject", I, P, I, P, I);
        private static final MethodHandle QUERY_JOB = function("QueryInformationJobObject", I, P, I, P, I, P);
        private static final MethodHandle CREATE_PROCESS = function("CreateProcessW", I, P, P, P, P, I, I, P, P, P, P);
        private static final MethodHandle ASSIGN = function("AssignProcessToJobObject", I, P, P);
        private static final MethodHandle RESUME = function("ResumeThread", I, P);
        private static final MethodHandle WAIT = function("WaitForSingleObject", I, P, I);
        private static final MethodHandle TERMINATE_JOB = function("TerminateJobObject", I, P, I);
        private static final MethodHandle TERMINATE_PROCESS = function("TerminateProcess", I, P, I);
        private static final MethodHandle TIMES = function("GetProcessTimes", I, P, P, P, P, P);
        private static final MethodHandle IN_JOB = function("IsProcessInJob", I, P, P, P);
        private static final MethodHandle OPEN_PROCESS = function("OpenProcess", P, I, I, I);
        private static final MethodHandle HANDLE_INFORMATION = function("GetHandleInformation", I, P, P);
        private static final MethodHandle CLOSE = function("CloseHandle", I, P);

        private static MethodHandle function(String name, MemoryLayout result, MemoryLayout... parameters) {
            return LINKER.downcallHandle(LIBRARY.find(name).orElseThrow(),
                FunctionDescriptor.of(result, parameters), Linker.Option.captureCallState("GetLastError"));
        }

        private static NativeResult call(MethodHandle function, Object... arguments) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment capture = arena.allocate(CAPTURE);
                List<Object> actual = new ArrayList<>(arguments.length + 1);
                actual.add(capture);
                actual.addAll(List.of(arguments));
                Object result = function.invokeWithArguments(actual);
                return new NativeResult(result, capture.get(I, LAST_ERROR));
            } catch (RuntimeException | Error exception) {
                throw exception;
            } catch (Throwable exception) {
                throw new IllegalStateException("Native worker invocation failed", exception);
            }
        }

        private static void require(MethodHandle function, String operation, Object... arguments) {
            NativeResult result = call(function, arguments);
            if (result.integer() == 0) {
                throw failure(operation, result.error());
            }
        }

        private static void close(MemorySegment handle) {
            if (handle.address() != 0) {
                require(CLOSE, "CloseHandle", handle);
            }
        }
    }
}
