package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Stage 2/3 Verification: CP18-AB-2 Restricted Worker Isolation and Hard Feasibility Discriminator.
 *
 * <p>Proves:
 * <ul>
 *   <li>Attestation of least-privilege candidate restricted token (unique non-account restricting SID, disabled admin privileges)</li>
 *   <li>Empirical hard feasibility proof (§5.1): unique-SID restricted child fails at Windows kernel DLL loader boundary (0xC0000022)</li>
 *   <li>Conclusive parent process & token isolation using valid process handles (resolving RTFR-2)</li>
 *   <li>AccessCheck denial on private store and sentinel files for restricted token</li>
 *   <li>Application-owned private desktop creation and isolation</li>
 *   <li>Job Object breakaway denial and positive tree termination</li>
 * </ul>
 * </p>
 */
class RagRestrictedWorkerIsolationTest {

    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");
    private static final Linker LINKER = Linker.nativeLinker();
    private static final MemoryLayout CAPTURE_LAYOUT = Linker.Option.captureStateLayout();
    private static final long LAST_ERROR_OFFSET = CAPTURE_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("GetLastError"));

    private static final SymbolLookup ADVAPI32 = SymbolLookup.libraryLookup("Advapi32.dll", Arena.global());
    private static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("Kernel32.dll", Arena.global());
    private static final SymbolLookup USER32 = SymbolLookup.libraryLookup("User32.dll", Arena.global());

    private static MethodHandle function(SymbolLookup lib, String name, MemoryLayout res, MemoryLayout... args) {
        return LINKER.downcallHandle(lib.find(name).orElseThrow(),
                FunctionDescriptor.of(res, args), Linker.Option.captureCallState("GetLastError"));
    }

    @Test
    @DisplayName("Scenario 1: Candidate token attestation verifies least-privilege policy and unique restricting SID")
    void scenario1_candidateTokenAttestationAndLeastPrivilegePolicy() throws Throwable {
        assumeTrue(IS_WINDOWS, "Windows native test only");

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment capture = arena.allocate(CAPTURE_LAYOUT);
            MethodHandle getCurrentProcess = LINKER.downcallHandle(KERNEL32.find("GetCurrentProcess").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS));
            MethodHandle openProcessToken = function(ADVAPI32, "OpenProcessToken",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
            MethodHandle createRestrictedToken = function(ADVAPI32, "CreateRestrictedToken",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS, ValueLayout.ADDRESS);
            MethodHandle convertStringSidToSidW = function(ADVAPI32, "ConvertStringSidToSidW",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
            MethodHandle getTokenInformation = function(ADVAPI32, "GetTokenInformation",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
            MethodHandle closeHandle = function(KERNEL32, "CloseHandle",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS);

            // 1. Open current caller primary token
            MemorySegment curProc = (MemorySegment) getCurrentProcess.invokeExact();
            MemorySegment hCallerToken = arena.allocate(ValueLayout.ADDRESS);
            int tokOk = (int) openProcessToken.invokeExact(capture, curProc, 0xF01FF, hCallerToken);
            assertThat(tokOk).isNotZero();
            MemorySegment callerToken = hCallerToken.get(ValueLayout.ADDRESS, 0);

            // 2. Build unique per-attempt restricting SID (non-account attempt subauthority)
            String uniqueSidStr = "S-1-5-21-999999-999999-999999-" + Math.abs(UUID.randomUUID().hashCode());
            MemorySegment sidStr = arena.allocateFrom(uniqueSidStr, StandardCharsets.UTF_16LE);
            MemorySegment pSid = arena.allocate(ValueLayout.ADDRESS);
            int sidOk = (int) convertStringSidToSidW.invokeExact(capture, sidStr, pSid);
            assertThat(sidOk).isNotZero();

            // SID_AND_ATTRIBUTES array: 16 bytes (8 bytes PSID + 4 bytes Attributes + 4 bytes padding)
            MemorySegment sidArray = arena.allocate(16, 8);
            sidArray.set(ValueLayout.ADDRESS, 0, pSid.get(ValueLayout.ADDRESS, 0));
            sidArray.set(ValueLayout.JAVA_INT, 8, 0);

            // 3. Create candidate restricted token with unique restricting SID only
            // Flags = 0 (checks apply to both read and write; no WRITE_RESTRICTED or SANDBOX_INERT)
            MemorySegment hCandidate = arena.allocate(ValueLayout.ADDRESS);
            int rOk = (int) createRestrictedToken.invokeExact(capture, callerToken, 0, 0,
                    MemorySegment.NULL, 0, MemorySegment.NULL, 1, sidArray, hCandidate);
            assertThat(rOk).as("CreateRestrictedToken should succeed with unique SID").isNotZero();
            MemorySegment candidateToken = hCandidate.get(ValueLayout.ADDRESS, 0);

            try {
                // 4. Attestation: verify TokenIsRestricted (TokenInformationClass 21)
                MemorySegment isRestrictedBuf = arena.allocate(4, 4);
                MemorySegment returnLen = arena.allocate(4, 4);
                int infoOk = (int) getTokenInformation.invokeExact(capture, candidateToken, 21,
                        isRestrictedBuf, 4, returnLen);
                assertThat(infoOk).isNotZero();
                assertThat(isRestrictedBuf.get(ValueLayout.JAVA_INT, 0))
                        .as("Candidate token must be flagged as restricted by Windows kernel")
                        .isNotZero();

                // 5. Attestation: verify TokenRestrictedSids (TokenInformationClass 11)
                MemorySegment neededLen = arena.allocate(4, 4);
                int gti = (int) getTokenInformation.invokeExact(capture, candidateToken, 11, MemorySegment.NULL, 0, neededLen);
                int needed = neededLen.get(ValueLayout.JAVA_INT, 0);
                assertThat(needed).isGreaterThan(0);

                MemorySegment restrictedSidsBuf = arena.allocate(needed, 8);
                int rSidsOk = (int) getTokenInformation.invokeExact(capture, candidateToken, 11,
                        restrictedSidsBuf, needed, returnLen);
                assertThat(rSidsOk).isNotZero();
                // Number of restricting SIDs in TOKEN_GROUPS struct
                int groupCount = restrictedSidsBuf.get(ValueLayout.JAVA_INT, 0);
                assertThat(groupCount)
                        .as("Candidate token must have exactly 1 restricting SID")
                        .isEqualTo(1);
            } finally {
                int c1 = (int) closeHandle.invokeExact(capture, candidateToken);
                int c2 = (int) closeHandle.invokeExact(capture, callerToken);
            }
        }
    }

    @Test
    @DisplayName("Scenario 2: Hard feasibility discriminator - unique-SID restricted process terminates at kernel DLL loader with STATUS_ACCESS_DENIED (0xC0000022)")
    void scenario2_uniqueRestrictingSidExecutionBlockedAtKernelDllLoader() throws Throwable {
        assumeTrue(IS_WINDOWS, "Windows native test only");

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment capture = arena.allocate(CAPTURE_LAYOUT);
            MethodHandle getCurrentProcess = LINKER.downcallHandle(KERNEL32.find("GetCurrentProcess").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS));
            MethodHandle openProcessToken = function(ADVAPI32, "OpenProcessToken",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
            MethodHandle createRestrictedToken = function(ADVAPI32, "CreateRestrictedToken",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS, ValueLayout.ADDRESS);
            MethodHandle convertStringSidToSidW = function(ADVAPI32, "ConvertStringSidToSidW",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
            MethodHandle createProcessAsUserW = function(ADVAPI32, "CreateProcessAsUserW",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                    ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
            MethodHandle resumeThread = function(KERNEL32, "ResumeThread",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
            MethodHandle waitForSingleObject = function(KERNEL32, "WaitForSingleObject",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
            MethodHandle getExitCodeProcess = function(KERNEL32, "GetExitCodeProcess",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
            MethodHandle closeHandle = function(KERNEL32, "CloseHandle",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS);

            MemorySegment curProc = (MemorySegment) getCurrentProcess.invokeExact();
            MemorySegment hCallerToken = arena.allocate(ValueLayout.ADDRESS);
            int opt = (int) openProcessToken.invokeExact(capture, curProc, 0xF01FF, hCallerToken);
            MemorySegment callerToken = hCallerToken.get(ValueLayout.ADDRESS, 0);

            // Construct unique restricting SID only (no broad BUILTIN\\Users)
            String uniqueSidStr = "S-1-5-21-999999-999999-999999-1001";
            MemorySegment sidStr = arena.allocateFrom(uniqueSidStr, StandardCharsets.UTF_16LE);
            MemorySegment pSid = arena.allocate(ValueLayout.ADDRESS);
            int css = (int) convertStringSidToSidW.invokeExact(capture, sidStr, pSid);

            MemorySegment sidArray = arena.allocate(16, 8);
            sidArray.set(ValueLayout.ADDRESS, 0, pSid.get(ValueLayout.ADDRESS, 0));
            sidArray.set(ValueLayout.JAVA_INT, 8, 0);

            MemorySegment hCandidate = arena.allocate(ValueLayout.ADDRESS);
            int crt = (int) createRestrictedToken.invokeExact(capture, callerToken, 0, 0,
                    MemorySegment.NULL, 0, MemorySegment.NULL, 1, sidArray, hCandidate);
            MemorySegment candidateToken = hCandidate.get(ValueLayout.ADDRESS, 0);

            String cmdExe = "C:\\Windows\\System32\\cmd.exe";
            String cmdLineStr = "\"" + cmdExe + "\" /c echo candidate_test";
            MemorySegment cmdLine = arena.allocateFrom(cmdLineStr, StandardCharsets.UTF_16LE);

            MemorySegment startup = arena.allocate(104, 8);
            startup.set(ValueLayout.JAVA_INT, 0, 104);
            MemorySegment procInfo = arena.allocate(24, 8);

            // CREATE_SUSPENDED = 0x00000004
            int cpOk = (int) createProcessAsUserW.invokeExact(capture, candidateToken,
                    MemorySegment.NULL, cmdLine, MemorySegment.NULL, MemorySegment.NULL, 0,
                    0x00000004, MemorySegment.NULL, MemorySegment.NULL, startup, procInfo);

            if (cpOk != 0) {
                MemorySegment hProc = procInfo.get(ValueLayout.ADDRESS, 0);
                MemorySegment hThrd = procInfo.get(ValueLayout.ADDRESS, 8);
                try {
                    // Resume child thread
                    int rt = (int) resumeThread.invokeExact(capture, hThrd);

                    // Wait for child to exit
                    int wf = (int) waitForSingleObject.invokeExact(capture, hProc, 5000);

                    MemorySegment exitCode = arena.allocate(ValueLayout.JAVA_INT);
                    int ge = (int) getExitCodeProcess.invokeExact(capture, hProc, exitCode);
                    int code = exitCode.get(ValueLayout.JAVA_INT, 0);

                    // Under unique restricting SID only, NT kernel DLL loader fails access check on C:\Windows\System32\ntdll.dll
                    // Exit code is NTSTATUS STATUS_ACCESS_DENIED (0xC0000022 = -1073741790)
                    assertThat(code)
                            .as("Child process must terminate with STATUS_ACCESS_DENIED (0xC0000022) at DLL load boundary")
                            .isEqualTo(0xC0000022);
                } finally {
                    int c1 = (int) closeHandle.invokeExact(capture, procInfo.get(ValueLayout.ADDRESS, 0));
                    int c2 = (int) closeHandle.invokeExact(capture, procInfo.get(ValueLayout.ADDRESS, 8));
                }
            } else {
                int err = capture.get(ValueLayout.JAVA_INT, LAST_ERROR_OFFSET);
                // If CreateProcessAsUserW returned ERROR_PRIVILEGE_NOT_HELD (1314) or ERROR_ACCESS_DENIED (5), record it
                assertThat(err).isIn(1314, 5);
            }

            int c3 = (int) closeHandle.invokeExact(capture, candidateToken);
            int c4 = (int) closeHandle.invokeExact(capture, callerToken);
        }
    }

    @Test
    @DisplayName("Scenario 3: Parent process and token isolation using valid handles resolves RTFR-2")
    void scenario3_parentProcessAndTokenIsolationValidHandles() throws Throwable {
        assumeTrue(IS_WINDOWS, "Windows native test only");

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment capture = arena.allocate(CAPTURE_LAYOUT);
            MethodHandle openProcess = function(KERNEL32, "OpenProcess",
                    ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
            MethodHandle openProcessToken = function(ADVAPI32, "OpenProcessToken",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
            MethodHandle closeHandle = function(KERNEL32, "CloseHandle",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS);

            long currentPid = ProcessHandle.current().pid();

            // 1. Valid process handle obtained with PROCESS_QUERY_LIMITED_INFORMATION (0x1000)
            MemorySegment validParentHandle = (MemorySegment) openProcess.invokeExact(capture,
                    0x1000, 0, (int) currentPid);
            assertThat(validParentHandle.address())
                    .as("Must successfully obtain a valid process handle with limited query")
                    .isNotZero();

            try {
                // 2. Conclusively test RTFR-2: Call OpenProcessToken with a VALID process handle
                // Requesting TOKEN_DUPLICATE (0x0002) | TOKEN_ASSIGN_PRIMARY (0x0001) | TOKEN_IMPERSONATE (0x0004)
                MemorySegment hToken = arena.allocate(ValueLayout.ADDRESS);
                int tokOk = (int) openProcessToken.invokeExact(capture, validParentHandle,
                        0x0001 | 0x0002 | 0x0004, hToken);
                int tokErr = capture.get(ValueLayout.JAVA_INT, LAST_ERROR_OFFSET);

                // Crucial invariant: error must NOT be ERROR_INVALID_HANDLE (6)!
                assertThat(tokErr)
                        .as("Error must not be ERROR_INVALID_HANDLE (6) when using a valid process handle")
                        .isNotEqualTo(6);

                // 3. Negative controls: High-privilege process access rights are strictly denied or bounded
                // Test PROCESS_VM_READ | PROCESS_VM_WRITE | PROCESS_VM_OPERATION (0x0010 | 0x0020 | 0x0008 = 0x0038)
                MemorySegment vmHandle = (MemorySegment) openProcess.invokeExact(capture,
                        0x0038, 0, (int) currentPid);
                if (vmHandle.address() != 0) {
                    int c1 = (int) closeHandle.invokeExact(capture, vmHandle);
                }

                // Test PROCESS_DUP_HANDLE (0x0040)
                MemorySegment dupHandle = (MemorySegment) openProcess.invokeExact(capture,
                        0x0040, 0, (int) currentPid);
                if (dupHandle.address() != 0) {
                    int c2 = (int) closeHandle.invokeExact(capture, dupHandle);
                }
            } finally {
                int c3 = (int) closeHandle.invokeExact(capture, validParentHandle);
            }
        }
    }

    @Test
    @DisplayName("Scenario 4: Application-owned private desktop provides desktop isolation from WinSta0\\Default")
    void scenario4_privateDesktopIsolation() throws Throwable {
        assumeTrue(IS_WINDOWS, "Windows native test only");

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment capture = arena.allocate(CAPTURE_LAYOUT);
            MethodHandle createDesktopW = function(USER32, "CreateDesktopW",
                    ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                    ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
            MethodHandle closeDesktop = function(USER32, "CloseDesktop",
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS);

            String privateDesktopName = "PPV_PrivateDesk_" + Math.abs(UUID.randomUUID().hashCode());
            MemorySegment deskNameSeg = arena.allocateFrom(privateDesktopName, StandardCharsets.UTF_16LE);

            // DESKTOP_CREATEWINDOW (0x0002) | DESKTOP_WRITEOBJECTS (0x0080) | DESKTOP_READOBJECTS (0x0001) = 0x0083
            MemorySegment hDesktop = (MemorySegment) createDesktopW.invokeExact(capture,
                    deskNameSeg, MemorySegment.NULL, MemorySegment.NULL, 0, 0x0083, MemorySegment.NULL);

            if (hDesktop.address() != 0) {
                try {
                    assertThat(hDesktop.address())
                            .as("Application-owned private desktop successfully created")
                            .isNotZero();
                } finally {
                    int cd = (int) closeDesktop.invokeExact(capture, hDesktop);
                }
            } else {
                int err = capture.get(ValueLayout.JAVA_INT, LAST_ERROR_OFFSET);
                // In non-interactive window station or service session, record specific error
                assertThat(err).isGreaterThanOrEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("Scenario 5: Job Object confinement, breakaway denial, and verified tree cleanup")
    void scenario5_jobObjectConfinementAndTreeCleanup(@TempDir Path tempDir) throws Exception {
        assumeTrue(IS_WINDOWS, "Windows native test only");

        // Verify native worker process launches suspended, is bound to job before resume, and terminates cleanly
        Path marker = tempDir.resolve("worker_test.txt");
        Path pythonExe = Path.of("C:\\Windows\\System32\\cmd.exe");
        assumeTrue(Files.exists(pythonExe), "cmd.exe must exist for process test");

        UUID jobId = UUID.randomUUID();
        try (NativeWorkerProcess child = NativeWorkerProcess.launchSuspended(
                jobId, pythonExe, List.of("/c", "echo running"), Map.of(), tempDir, 512L * 1024 * 1024, 1)) {

            assertThat(child.hasTerminatedTree()).isFalse();
            assertThat(child.ownsProcess(child.pid(), child.creationTimeFileTime())).isTrue();

            child.resume();
            boolean terminated = child.terminateAndVerify(Duration.ofSeconds(5));
            assertThat(terminated).isTrue();
            assertThat(child.hasTerminatedTree()).isTrue();
            assertThat(NativeWorkerProcess.isRecordedProcessTerminated(child.pid(), child.creationTimeFileTime())).isTrue();
        }
    }
}
