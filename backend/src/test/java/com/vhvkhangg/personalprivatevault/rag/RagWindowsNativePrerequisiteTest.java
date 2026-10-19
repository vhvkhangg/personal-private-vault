package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.windows.WindowsNativeFileGuard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Real NTFS prerequisites only; none of these tests claims index publication/R7 acceptance. */
class RagWindowsNativePrerequisiteTest {
    @TempDir Path directory;
    private static MemorySegment deniedHandle;
    private static MemorySegment deniedInformation;
    private static int injectedLinkCount;

    private Path source() throws IOException {
        assumeTrue(WindowsNativeFileGuard.isWindows(), "Requires actual Windows NTFS");
        return Files.writeString(directory.resolve("source.txt"), "Approved synthetic TXT\n");
    }

    @Test
    void realApiReportsOneAndTwoLinksAtVerifiedLayoutOffsetAndDenialReleasesHandle() throws Throwable {
        Path file = source();
        var guard = new WindowsNativeFileGuard(4096);
        try (var held = guard.openAndGuard(file); var arena = Arena.ofConfined()) {
            assertThat(held.getContentAsUtf8()).isEqualTo("Approved synthetic TXT\n");
            var info = arena.allocate(52, 4);
            assertThat((int) query(guard).invokeExact(nativeHandle(held), info)).isNotZero();
            assertThat(info.get(ValueLayout.JAVA_INT, 40)).isEqualTo(1);
            assertThat(info.get(ValueLayout.JAVA_INT, 36)).isEqualTo(Files.size(file));
        }
        Path alias = Files.createLink(directory.resolve("existing-alias.txt"), file);
        // Query the real API independently with an ordinary read handle before testing denial.
        try (var arena = Arena.ofConfined()) {
            var create = field(guard, "createFileW", MethodHandle.class);
            MemorySegment name = arena.allocateFrom(file.toString(), java.nio.charset.StandardCharsets.UTF_16LE);
            MemorySegment nativeFile = (MemorySegment) create.invokeExact(name, 0x80000000, 1,
                    MemorySegment.NULL, 3, 0x80, MemorySegment.NULL);
            assertThat(nativeFile.address()).isNotIn(0L, -1L);
            try {
                var info = arena.allocate(52, 4);
                assertThat((int) query(guard).invokeExact(nativeFile, info)).isNotZero();
                assertThat(info.get(ValueLayout.JAVA_INT, 40)).isEqualTo(2);
            } finally {
                assertThat((int) field(guard, "closeHandle", MethodHandle.class).invokeExact(nativeFile)).isNotZero();
            }
        }
        assertThatThrownBy(() -> guard.openAndGuard(file)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Unsupported NTFS hard-link count: 2; exactly one link is required");
        Files.writeString(alias, "Writable after denied acquisition");
        Files.delete(alias);
        try (var held = guard.openAndGuard(file)) {
            assertThat(held.getContentAsUtf8()).isEqualTo("Writable after denied acquisition");
        }
    }

    @Test
    void failedInformationQueryClosesActualHandleAndArena() throws Throwable {
        verifyInjectedDenial("failedQuery", "Failed to query Win32 file information");
    }

    @Test
    void successfulButUnavailableInformationIsDeniedAndReleased() throws Throwable {
        injectedLinkCount = 0;
        verifyInjectedDenial("linkQuery", "Unsupported NTFS hard-link count: 0");
    }

    @ParameterizedTest
    @ValueSource(ints = {2, -1})
    void invalidOrUnsupportedInformationIsDeniedAndReleased(int count) throws Throwable {
        injectedLinkCount = count;
        verifyInjectedDenial("linkQuery", "Unsupported NTFS hard-link count:");
    }

    @Test
    void exceptionalQueryAlsoClosesActualHandleAndArena() throws Throwable {
        verifyInjectedDenial("throwingQuery", "Injected query failure");
    }

    private void verifyInjectedDenial(String method, String message) throws Throwable {
        Path file = source();
        var guard = new WindowsNativeFileGuard(4096);
        MethodHandle realQuery = query(guard);
        var replacement = MethodHandles.lookup().findStatic(getClass(), method,
                MethodType.methodType(int.class, MemorySegment.class, MemorySegment.class));
        var field = guard.getClass().getDeclaredField("getFileInformationByHandle");
        field.setAccessible(true);
        field.set(guard, replacement); // Test-only native-query fault; actual CreateFile/CloseHandle remain real.
        assertThatThrownBy(() -> guard.openAndGuard(file)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(message);
        assertThat(deniedInformation.scope().isAlive()).isFalse();
        try (var arena = Arena.ofConfined()) {
            var info = arena.allocate(52, 4);
            assertThat((int) realQuery.invokeExact(deniedHandle, info)).isZero();
        }
        Files.writeString(file, "Released immediately after denial");
        Files.delete(file);
    }

    private static int failedQuery(MemorySegment handle, MemorySegment information) {
        deniedHandle = handle;
        deniedInformation = information;
        return 0;
    }

    private static int linkQuery(MemorySegment handle, MemorySegment information) {
        failedQuery(handle, information);
        information.set(ValueLayout.JAVA_INT, 40, injectedLinkCount);
        return 1;
    }

    private static int throwingQuery(MemorySegment handle, MemorySegment information) {
        failedQuery(handle, information);
        throw new IllegalStateException("Injected query failure");
    }

    @Test
    void attemptsAliasCreationAndRemovalDuringProtectedLifetime() throws Exception {
        Path file = source();
        Path alias = directory.resolve("late-alias.txt");
        boolean created = false;
        boolean removed = false;
        String createDenial = "none";
        String removeDenial = "not attempted: creation denied";
        try (var held = new WindowsNativeFileGuard(4096).openAndGuard(file)) {
            try {
                Files.createLink(alias, file);
                created = true;
            } catch (java.nio.file.FileSystemException denial) {
                createDenial = denial.getReason();
            }
            if (created) {
                try {
                    Files.delete(alias);
                    removed = true;
                } catch (java.nio.file.FileSystemException denial) {
                    removeDenial = denial.getReason();
                }
                if (!removed) {
                    assertThatThrownBy(() -> Files.writeString(alias, "Opposing alias writer"))
                            .isInstanceOf(IOException.class);
                }
            }
            System.out.printf("NTFS alias attempt: created=%s removed=%s createDenial=%s removeDenial=%s%n",
                    created, removed, createDenial, removeDenial);
            assertThat(held.getContentAsUtf8()).isEqualTo("Approved synthetic TXT\n");
        }
        Files.deleteIfExists(alias);
        Files.createLink(alias, file); // Positive control: creation really is available after guard release.
        Files.delete(alias);
        assertThat(created).as("Unsupported alias creation must be blocked throughout the held lifetime").isFalse();
    }

    @Test
    void attemptsFileReplacementAndNestedAncestorRenameDuringProtectedLifetime() throws Exception {
        source();
        Path parent = Files.createDirectories(directory.resolve("ancestor/nested"));
        Path file = Files.writeString(parent.resolve("child.txt"), "Original namespace");
        Path replacement = Files.writeString(directory.resolve("replacement.txt"), "Substituted");
        try (var held = new WindowsNativeFileGuard(4096).openAndGuard(file)) {
            assertThatThrownBy(() -> Files.move(file, parent.resolve("renamed.txt"))).isInstanceOf(IOException.class);
            assertThatThrownBy(() -> Files.move(replacement, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING))
                    .isInstanceOf(IOException.class);
            assertThatThrownBy(() -> Files.move(parent, directory.resolve("moved-nested"))).isInstanceOf(IOException.class);
            assertThatThrownBy(() -> Files.move(parent.getParent(), directory.resolve("moved-ancestor")))
                    .isInstanceOf(IOException.class);
            assertThat(Files.readString(file)).isEqualTo(held.getContentAsUtf8());
        }
        Files.move(parent.getParent(), directory.resolve("released-ancestor"));
    }

    @Test
    void preopenedWritableMappingIsActuallyLiveAndAcquisitionIsDenied() throws Exception {
        Path file = source();
        try (var arena = Arena.ofConfined()) {
            MemorySegment mapping;
            try (var channel = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                mapping = channel.map(FileChannel.MapMode.READ_WRITE, 0, Files.size(file), arena);
            }
            mapping.set(ValueLayout.JAVA_BYTE, 0, (byte) 'M');
            mapping.force();
            assertThat(Files.readString(file)).startsWith("Mpproved");
            assertThatThrownBy(() -> new WindowsNativeFileGuard(4096).openAndGuard(file))
                    .isInstanceOf(IllegalStateException.class);
        }
        try (var held = new WindowsNativeFileGuard(4096).openAndGuard(file)) {
            assertThatThrownBy(() -> FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE))
                    .isInstanceOf(IOException.class);
            assertThat(held.getContentAsUtf8()).startsWith("Mpproved");
        }
    }

    private static MethodHandle query(WindowsNativeFileGuard guard) throws Exception {
        return field(guard, "getFileInformationByHandle", MethodHandle.class);
    }

    private static MemorySegment nativeHandle(WindowsNativeFileGuard.ProtectedFileHandle held) throws Exception {
        return field(held, "handle", MemorySegment.class);
    }

    private static <T> T field(Object target, String name, Class<T> type) throws Exception {
        var field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}
