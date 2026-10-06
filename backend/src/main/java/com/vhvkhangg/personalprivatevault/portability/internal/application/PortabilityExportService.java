package com.vhvkhangg.personalprivatevault.portability.internal.application;

import com.vhvkhangg.personalprivatevault.portability.internal.infrastructure.snapshot.PortabilitySnapshotAdapter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service orchestrating the creation of portable vault export archives.
 */
@Service
public class PortabilityExportService {

    private final PortabilitySnapshotAdapter snapshotAdapter;

    public PortabilityExportService(PortabilitySnapshotAdapter snapshotAdapter) {
        this.snapshotAdapter = snapshotAdapter;
    }

    /**
     * Creates a temporary ZIP export archive of the vault.
     * The caller is responsible for deleting the returned file once consumed.
     *
     * @return Path to the generated ZIP archive
     */
    public Path createExportArchive() {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("personal-private-vault-export-", ".zip");
            snapshotAdapter.exportSnapshot(tempFile);
            return tempFile;
        } catch (Exception ex) {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
            if (ex instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException("Failed to generate export archive", ex);
        }
    }
}
