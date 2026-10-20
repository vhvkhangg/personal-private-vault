package com.vhvkhangg.personalprivatevault.rag.api;

import com.vhvkhangg.personalprivatevault.rag.source.RagSnapshotExpectation;
import java.util.Objects;

/** One explicit server-local file update, bound to its owner's preview. */
public record RagSnapshotCaptureCommand(String filePath, RagSnapshotExpectation expectedSnapshot) {
    public RagSnapshotCaptureCommand {
        if (filePath == null || filePath.isBlank()) throw new IllegalArgumentException("File intent required");
        Objects.requireNonNull(expectedSnapshot, "Snapshot expectation required");
    }

    @Override
    public String toString() { return "RagSnapshotCaptureCommand[redacted]"; }
}
