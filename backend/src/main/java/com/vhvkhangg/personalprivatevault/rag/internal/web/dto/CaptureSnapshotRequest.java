package com.vhvkhangg.personalprivatevault.rag.internal.web.dto;

import com.vhvkhangg.personalprivatevault.rag.source.RagSnapshotExpectation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CaptureSnapshotRequest(@NotBlank String filePath, @Valid @NotNull RagSnapshotExpectation expectedSnapshot) {
    @Override public String toString() { return "CaptureSnapshotRequest[redacted]"; }
}
