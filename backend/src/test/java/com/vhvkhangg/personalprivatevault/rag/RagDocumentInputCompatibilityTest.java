package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.application.RagDocumentInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

@Timeout(15)
class RagDocumentInputCompatibilityTest {
    @Test void javaIssuedBytesMatchNativePythonTypedRequestDigest() throws Exception {
        Map<String, Object> root = new TreeMap<>();
        root.put("root_key", "root"); root.put("source_id", "source-42");
        root.put("source_version", 7); root.put("format", "DOMAIN");
        root.put("content_text", null); root.put("content_b64", null);
        root.put("ocr_languages", List.of("en")); root.put("approved_fields", List.of("title"));
        root.put("transclusions", Map.of());
        root.put("prepared_fields", List.of(Map.of("source_key", "42", "source_version", 7,
                "approved_field", "title", "text", "Ti\u1ebfng Vi\u1ec7t \u4e2d\u6587 \nquote \" slash \\")));
        String input = RagDocumentInput.encode(List.of(root));
        String expected = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8)));
        Path projectRoot = Files.isDirectory(Path.of("rag-engine")) ? Path.of(".") : Path.of("..");
        Path python = projectRoot.resolve("rag-engine/.venv/Scripts/python.exe").toAbsolutePath();
        assertThat(python).isRegularFile();
        var builder = new ProcessBuilder(python.toString(), "-c", """
                import json, sys
                from ppv_rag.contracts import DocumentRootTask, document_input_digest
                roots = [DocumentRootTask(**r) for r in json.loads(sys.stdin.buffer.read().decode('utf-8'))]
                print(document_input_digest(roots))
                """);
        builder.environment().put("PYTHONPATH", projectRoot.resolve("rag-engine/src").toAbsolutePath().toString());
        var process = builder.start();
        try {
            try (var stdin = process.getOutputStream()) { stdin.write(input.getBytes(StandardCharsets.UTF_8)); }
            assertThat(process.waitFor(10, TimeUnit.SECONDS)).isTrue();
            assertThat(process.exitValue()).isZero();
            assertThat(new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim())
                    .isEqualTo(expected);
        } finally {
            if (process.isAlive()) { process.destroyForcibly(); process.waitFor(2, TimeUnit.SECONDS); }
        }
    }
}
