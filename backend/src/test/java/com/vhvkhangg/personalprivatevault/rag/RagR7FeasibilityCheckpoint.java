package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.windows.WindowsNativeFileGuard;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.sql.DriverManager;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Explicit source-launch diagnostic, never a production activation or synthetic publication. */
public class RagR7FeasibilityCheckpoint {
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !WindowsNativeFileGuard.isWindows())
            throw new IllegalArgumentException("Requires native Windows and one owned evidence directory");
        Path evidence = Path.of(args[0]).toAbsolutePath().normalize();
        Files.createDirectories(evidence);
        Path owned = Files.createTempDirectory(evidence, "r7-owned-");
        Map<String, Object> results = new LinkedHashMap<>();
        results.put("candidate", "existing WindowsNativeFileGuard retained around physical JDBC commit; prerequisite diagnostic only");
        results.put("full_authority_worker_publication", "NOT_VERIFIED; never bypass live-source denial or forge staging");
        var guard = new WindowsNativeFileGuard(4096);
        boolean failed = false;
        try {
            Path file = Files.writeString(owned.resolve("approved.txt"), "Approved synthetic content");
            var postgres = AbstractPostgresIntegrationTest.POSTGRES;
            try (var connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
                connection.setAutoCommit(false);
                try (var statement = connection.createStatement()) {
                    statement.execute("CREATE TABLE checkpoint_marker(id int PRIMARY KEY)");
                }
                connection.commit();
                try (var handle = guard.openAndGuard(file)) {
                    boolean blockedBefore = writerDenied(file);
                    try (var statement = connection.createStatement()) {
                        statement.executeUpdate("INSERT INTO checkpoint_marker VALUES(1)");
                    }
                    connection.commit(); // Actual driver/server physical COMMIT, handle still in lexical scope.
                    boolean blockedAfter = writerDenied(file);
                    int visible;
                    try (var other = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                         var statement = other.createStatement(); var rows = statement.executeQuery("SELECT count(*) FROM checkpoint_marker")) {
                        rows.next(); visible = rows.getInt(1);
                    }
                    results.put("ordinary_physical_commit_prerequisite", Map.of("writer_denied_before", blockedBefore,
                            "writer_denied_after", blockedAfter, "separate_connection_visible_rows", visible,
                            "identity", handle.getFileIdentity(), "content_hash", handle.getContentHash()));
                    failed |= !blockedBefore || !blockedAfter || visible != 1;
                }
                Files.writeString(file, "Released after physical commit");
                results.put("handle_release_after_commit", true);
            } finally { postgres.stop(); }

            Path alias = Files.createLink(owned.resolve("alias.txt"), file);
            boolean hardlinkDenied;
            try (var ignored = guard.openAndGuard(file)) { hardlinkDenied = false; }
            catch (IllegalStateException denied) { hardlinkDenied = true; }
            results.put("hardlink_denied", hardlinkDenied);
            failed |= !hardlinkDenied;
            Files.delete(alias);

            Path parent = Files.createDirectory(owned.resolve("ancestor"));
            Path child = Files.writeString(parent.resolve("source.txt"), "Original approved namespace");
            Path moved = owned.resolve("ancestor-moved");
            boolean ancestorDenied;
            try (var handle = guard.openAndGuard(child)) {
                try { Files.move(parent, moved); ancestorDenied = false; }
                catch (java.io.IOException denied) { ancestorDenied = true; }
                results.put("ancestor_rename_denied", ancestorDenied);
                if (!ancestorDenied) {
                    Files.createDirectory(parent);
                    Files.writeString(child, "Unapproved replacement namespace");
                    results.put("approved_path_can_resolve_to_other_bytes", !Files.readString(child).equals(handle.getContentAsUtf8()));
                }
            }
            failed |= !ancestorDenied;

            Path mapped = Files.writeString(owned.resolve("mapping.txt"), "AAAA");
            try (var arena = Arena.ofConfined()) {
                java.lang.foreign.MemorySegment mapping;
                try (var channel = FileChannel.open(mapped, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                    mapping = channel.map(FileChannel.MapMode.READ_WRITE, 0, 4, arena);
                }
                try (var handle = guard.openAndGuard(mapped)) {
                    mapping.set(ValueLayout.JAVA_BYTE, 0, (byte) 'B'); mapping.force();
                    boolean changed = !handle.getContentAsUtf8().equals(Files.readString(mapped));
                    results.put("preexisting_mapping", changed ? "UNSAFE_MUTATION" : "NO_MUTATION_OBSERVED");
                    failed |= changed;
                } catch (IllegalStateException denied) {
                    results.put("preexisting_mapping", "DENIED: " + denied.getMessage());
                }
            }
            results.put("remaining_full_R7_cases", "NOT_VERIFIED: authority/worker/index physical C, rollback/unknown-C, revoke orders, child scope; stop on failed namespace/link prerequisites");
        } catch (Exception failure) {
            results.put("exception", failure.getClass().getName() + ": " + failure.getMessage());
            failed = true;
        } finally {
            try (var paths = Files.walk(owned)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.delete(path);
            }
            results.put("owned_fixture_cleanup", !Files.exists(owned));
            results.put("verdict", failed ? "FAIL" : "NOT_VERIFIED");
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(evidence.resolve("r7-result.json").toFile(), results);
            System.out.println(new ObjectMapper().writeValueAsString(results));
        }
        if (failed) System.exit(1);
    }

    private static boolean writerDenied(Path path) throws Exception {
        try { Files.writeString(path, "writer mutation"); return false; }
        catch (java.nio.file.FileSystemException denied) { return true; }
    }
}
