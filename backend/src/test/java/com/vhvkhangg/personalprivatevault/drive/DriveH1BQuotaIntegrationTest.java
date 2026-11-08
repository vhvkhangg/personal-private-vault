package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.fixture.DriveH1BTestSupport;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriveH1BQuotaIntegrationTest extends DriveH1BTestSupport {

    @Test
    @DisplayName("B-07: Storage quota truthful projection with distinct all-service and Drive usage")
    void storageQuotaTruthfulProjection() throws Exception {
        connect(1, "SUB_B07");
        google.quotaOverride = """
            {"storageQuota":{"usage":"15000000000","usageInDrive":"5000000000","limit":"100000000000"}}
            """;

        try (var op = resources.open(false)) {
            var result = explorerService.storageQuota("1", (short) 1, op);
            var quota = result.data();
            assertThat(quota.usageBytes()).isEqualTo(15_000_000_000L);
            assertThat(quota.usageInDriveBytes()).isEqualTo(5_000_000_000L);
            assertThat(quota.limitBytes()).isEqualTo(100_000_000_000L);
            assertThat(quota.limitKnown()).isTrue();
            assertThat(quota.observedAt()).isNotNull();
        }
    }

    @Test
    @DisplayName("B-07: Absent limit field truthfully projected with limitKnown=false, limitBytes=null")
    void absentLimitFieldProjectedWithoutFabrication() throws Exception {
        connect(1, "SUB_B07_NOLIMIT");
        google.quotaOverride = """
            {"storageQuota":{"usage":"7000000","usageInDrive":"3000000"}}
            """;

        try (var op = resources.open(false)) {
            var result = explorerService.storageQuota("1", (short) 1, op);
            var quota = result.data();
            assertThat(quota.usageBytes()).isEqualTo(7_000_000L);
            assertThat(quota.usageInDriveBytes()).isEqualTo(3_000_000L);
            assertThat(quota.limitBytes()).isNull();
            assertThat(quota.limitKnown()).isFalse();
        }
    }

    @Test
    @DisplayName("B-07: Malformed about payload throws 502, never invents zero")
    void malformedAboutFailsHonestly() throws Exception {
        connect(1, "SUB_B07_ERR");
        google.quotaOverride = """
            {"storageQuota":"not-an-object"}
            """;

        try (var op = resources.open(false)) {
            assertThatThrownBy(() -> explorerService.storageQuota("1", (short) 1, op))
                    .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(502));
        }
    }

    @Test
    @DisplayName("B-07: Malformed quota member shapes ({}, [], \"\", \"abc\", -1) throw 502, never treated as missing/zero")
    void malformedQuotaMembersThrow502() throws Exception {
        connect(1, "SUB_B07_MEMBERS");

        List<String> badLimits = List.of("{}", "[]", "\"\"", "\"not-a-number\"", "\"-1\"");
        for (String bad : badLimits) {
            google.quotaOverride = "{\"storageQuota\":{\"usage\":\"1000\",\"usageInDrive\":\"500\",\"limit\":" + bad + "}}";
            try (var op = resources.open(false)) {
                assertThatThrownBy(() -> explorerService.storageQuota("1", (short) 1, op))
                        .as("bad limit member " + bad + " must fail with 502")
                        .isInstanceOfSatisfying(DriveFileException.class, e -> assertThat(e.getHttpStatus()).isEqualTo(502));
            }
        }
    }

    @Test
    @DisplayName("B-07: Late-epoch invalidation fails closed with 409")
    void lateEpochVerificationFailureFailsClosed() throws Exception {
        connect(1, "SUB_B07_LATE_EPOCH");

        google.metadataEntered = new CountDownLatch(1);
        google.metadataRelease = new CountDownLatch(1);

        try (var workers = Executors.newSingleThreadExecutor()) {
            var future = workers.submit(() -> {
                try (var op = resources.open(false)) {
                    explorerService.storageQuota("1", (short) 1, op);
                    return null;
                }
            });

            // Wait for request to enter HTTP provider call (after acquire)
            assertThat(google.metadataEntered.await(5, TimeUnit.SECONDS)).isTrue();

            // Advance epoch in database while request is in-flight!
            jdbc.update("UPDATE drive_connections SET credential_epoch = ? WHERE slot = 1",
                    UUID.randomUUID());

            // Release HTTP request
            google.metadataRelease.countDown();

            // Late verification fails closed with 409
            assertThatThrownBy(future::get)
                    .hasCauseInstanceOf(DriveFileException.class)
                    .satisfies(e -> assertThat(((DriveFileException) e.getCause()).getHttpStatus()).isEqualTo(409));
        }
    }
}
