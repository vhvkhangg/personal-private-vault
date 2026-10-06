package com.vhvkhangg.personalprivatevault.portability.internal.web.controller;

import com.vhvkhangg.personalprivatevault.portability.internal.application.PortabilityExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/portability")
@Tag(name = "Portability", description = "Vault portability and export operations")
public class PortabilityController {

    private static final Logger log = LoggerFactory.getLogger(PortabilityController.class);

    private static final DateTimeFormatter FILENAME_DATE_FORMAT = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);

    private final PortabilityExportService exportService;

    public PortabilityController(PortabilityExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/exports")
    @Operation(operationId = "exportVaultArchive", summary = "Export full vault as portable ZIP archive")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Portable ZIP archive",
                    content = @Content(
                            mediaType = "application/zip",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", format = "binary")
                    )
            )
    })
    public void exportSnapshotArchive(HttpServletResponse response) throws IOException {
        Path tempArchive = null;
        try {
            tempArchive = exportService.createExportArchive();
            long fileSize = Files.size(tempArchive);

            String timestamp = FILENAME_DATE_FORMAT.format(Instant.now());
            String filename = "personal-private-vault-export-v1-" + timestamp + ".zip";

            try (InputStream in = Files.newInputStream(tempArchive)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/zip");
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
                response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
                response.setContentLengthLong(fileSize);

                try (OutputStream out = response.getOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        out.flush();
                    }
                }
            } catch (Exception ex) {
                if (response.isCommitted()) {
                    log.warn("Export transfer aborted after response commitment: exception={}", ex.getClass().getSimpleName());
                    throw new IOException("Export transfer stream aborted: " + ex.getClass().getSimpleName());
                }
                response.reset();
                if (ex instanceof IOException ioEx) {
                    throw ioEx;
                }
                throw new IOException("Export transfer stream failed", ex);
            }
        } finally {
            if (tempArchive != null) {
                try {
                    Files.deleteIfExists(tempArchive);
                } catch (IOException ignored) {
                }
            }
        }
    }
}
