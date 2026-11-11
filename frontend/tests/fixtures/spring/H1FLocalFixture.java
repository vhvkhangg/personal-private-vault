package com.vhvkhangg.personalprivatevault.fixture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand;
import com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations;
import com.vhvkhangg.personalprivatevault.authentication.fixture.FakeGoogleSignInServer;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInService;
import com.vhvkhangg.personalprivatevault.authentication.internal.web.controller.GoogleSignInController;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleDriveServer;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleOAuthServer;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleThumbnailServer;
import com.vhvkhangg.personalprivatevault.drive.internal.application.DriveConnectionService;
import com.vhvkhangg.personalprivatevault.drive.internal.config.DriveProperties;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileResources;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.OAuthAttemptManager;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.SubjectLifecycleManager;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class H1FLocalFixture {

    private static final int SPRING_PORT = 19080;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        System.out.println("[h1f-fixture] Starting PostgreSQL container...");
        @SuppressWarnings("resource")
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
                DockerImageName.parse("ppv-rag-postgres:18.6-pgvector0.8.6")
                        .asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("personal_private_vault")
                .withUsername("ppv_test")
                .withPassword("ppv_test_password");
        postgres.start();

        System.out.println("[h1f-fixture] Generating crypto keys and spool...");
        byte[] googleKeyBytes = new byte[32];
        new SecureRandom().nextBytes(googleKeyBytes);
        Path tempGoogleKey = Files.createTempFile("ppv-google-txn-", ".key");
        Files.write(tempGoogleKey, googleKeyBytes);
        tempGoogleKey.toFile().deleteOnExit();

        byte[] driveKeyBytes = new byte[32];
        new SecureRandom().nextBytes(driveKeyBytes);
        Path tempDriveKey = Files.createTempFile("s2-http-key-", ".bin");
        Files.write(tempDriveKey, driveKeyBytes);
        tempDriveKey.toFile().deleteOnExit();

        Path tempSpoolDir = Files.createTempDirectory("s2-http-spool-");

        System.out.println("[h1f-fixture] Starting fake Google/OAuth/Drive/Thumbnail servers...");
        FakeGoogleSignInServer fakeGoogle = new FakeGoogleSignInServer();
        FakeGoogleOAuthServer fakeOAuth = new FakeGoogleOAuthServer();
        FakeGoogleDriveServer fakeDrive = new FakeGoogleDriveServer(fakeOAuth.accessTokenSubjects::get);
        FakeGoogleThumbnailServer fakeThumbnail = new FakeGoogleThumbnailServer();

        H1FFixtureConfiguration.fakeGoogle = fakeGoogle;
        H1FFixtureConfiguration.fakeOAuth = fakeOAuth;
        H1FFixtureConfiguration.fakeDrive = fakeDrive;
        H1FFixtureConfiguration.fakeThumbnail = fakeThumbnail;
        H1FFixtureConfiguration.spoolDir = tempSpoolDir;

        System.setProperty("server.port", String.valueOf(SPRING_PORT));
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
        System.setProperty("spring.jpa.hibernate.ddl-auto", "validate");
        System.setProperty("spring.datasource.hikari.maximum-pool-size", "5");
        System.setProperty("spring.datasource.hikari.minimum-idle", "1");
        System.setProperty("spring.datasource.hikari.idle-timeout", "10000");
        System.setProperty("ppv.security.jwt.secret", "dGVzdC1zZWNyZXQta2V5LWZvci1wZXJzb25hbC1wcml2YXRlLXZhdWx0LXRlc3RzLTEyMzQ1Ng==");
        System.setProperty("ppv.security.google-signin.enabled", "true");
        System.setProperty("ppv.security.google-signin.client-id", "test-google-client-id");
        System.setProperty("ppv.security.google-signin.client-secret", "test-google-client-secret");
        System.setProperty("ppv.security.google-signin.txn-key-file", tempGoogleKey.toAbsolutePath().toString());
        System.setProperty("ppv.drive.enabled", "true");
        System.setProperty("ppv.drive.files.enabled", "true");
        System.setProperty("ppv.drive.token-key-file", tempDriveKey.toAbsolutePath().toString());
        System.setProperty("ppv.drive.files.spool-dir", tempSpoolDir.toAbsolutePath().toString());
        System.setProperty("ppv.drive.oauth.client-id", "s2-http-client");
        System.setProperty("ppv.drive.oauth.client-secret", "synthetic-secret");
        System.setProperty("ppv.drive.oauth.redirect-uri", "http://localhost:8080/api/v1/drive/oauth/callback");

        System.out.println("[h1f-fixture] Launching Spring Boot context on port " + SPRING_PORT + "...");
        ConfigurableApplicationContext context = SpringApplication.run(H1FFixtureConfiguration.class);

        GoogleSignInController gController = context.getBean(GoogleSignInController.class);
        gController.setAllowedPort(SPRING_PORT);
        GoogleSignInService gService = context.getBean(GoogleSignInService.class);
        gService.setCustomCallbackUri("http://localhost:" + SPRING_PORT + "/api/v1/auth/google/callback");
        DriveProperties driveProps = context.getBean(DriveProperties.class);
        driveProps.getOauth().setRedirectUri("http://localhost:" + SPRING_PORT + "/api/v1/drive/oauth/callback");

        BootstrapOperations bootstrap = context.getBean(BootstrapOperations.class);
        if (!bootstrap.isBootstrapped()) {
            bootstrap.bootstrap(new BootstrapCommand("owner@vault.local", "vaultowner", "MasterVaultPassword123!", "123456"));
        }

        JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
        DriveConnectionService s1 = context.getBean(DriveConnectionService.class);
        OAuthAttemptManager attempts = context.getBean(OAuthAttemptManager.class);
        SubjectLifecycleManager lifecycle = context.getBean(SubjectLifecycleManager.class);

        HttpServer controlServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        controlServer.setExecutor(Executors.newCachedThreadPool());

        controlServer.createContext("/api/test/reset", exchange -> {
            try {
                if (H1FFixtureConfiguration.refreshPauseLatch != null) {
                    H1FFixtureConfiguration.refreshPauseLatch.countDown();
                    H1FFixtureConfiguration.refreshPauseLatch = null;
                }
                if (H1FFixtureConfiguration.refreshEnteredLatch != null) {
                    H1FFixtureConfiguration.refreshEnteredLatch.countDown();
                    H1FFixtureConfiguration.refreshEnteredLatch = null;
                }
                if (H1FFixtureConfiguration.handoffPauseLatch != null) {
                    H1FFixtureConfiguration.handoffPauseLatch.countDown();
                    H1FFixtureConfiguration.handoffPauseLatch = null;
                }
                if (H1FFixtureConfiguration.handoffEnteredLatch != null) {
                    H1FFixtureConfiguration.handoffEnteredLatch.countDown();
                    H1FFixtureConfiguration.handoffEnteredLatch = null;
                }

                jdbc.update("DELETE FROM drive_child_folder_intents");
                jdbc.update("DELETE FROM drive_upload_intents");
                jdbc.update("DELETE FROM drive_folder_roots");
                jdbc.update("DELETE FROM drive_connections");
                jdbc.execute("TRUNCATE TABLE ppv_google_handoff_tickets, ppv_google_login_attempts, ppv_owner_google_identities CASCADE");

                lifecycle.initializeFromDatabase(List.of());
                attempts.invalidateSlotAttempts((short) 1);
                attempts.invalidateSlotAttempts((short) 2);

                fakeGoogle.reset();
                fakeOAuth.reset();
                fakeDrive.reset();
                fakeThumbnail.reset();
                H1FFixtureConfiguration.clockOffsetSeconds = 0;
                H1FFixtureConfiguration.failRevoke = false;
                H1FFixtureConfiguration.refreshRequests.set(0);
                H1FFixtureConfiguration.springRequests.set(0);
                if (H1FFixtureConfiguration.responseRelease != null) H1FFixtureConfiguration.responseRelease.countDown();
                H1FFixtureConfiguration.heldResponsePath = null;

                if (!bootstrap.isBootstrapped()) {
                    bootstrap.bootstrap(new BootstrapCommand("owner@vault.local", "vaultowner", "MasterVaultPassword123!", "123456"));
                }

                sendJson(exchange, 200, Map.of("status", "OK"));
            } catch (Exception e) {
                sendJson(exchange, 500, Map.of("error", e.getMessage()));
            }
        });

        controlServer.createContext("/api/test/seed-owner-google", exchange -> {
            try {
                JsonNode body = readJson(exchange);
                String issuer = body.path("issuer").asText("https://accounts.google.com");
                String sub = body.path("sub").asText("browser-owner-sub-1001");
                jdbc.update("""
                        INSERT INTO ppv_owner_google_identities (owner_id, issuer, google_sub, linked_at)
                        VALUES (1, ?, ?, NOW())
                        ON CONFLICT (owner_id) DO UPDATE SET google_sub = EXCLUDED.google_sub
                        """, issuer, sub);
                sendJson(exchange, 200, Map.of("status", "OK"));
            } catch (Exception e) {
                sendJson(exchange, 500, Map.of("error", e.getMessage()));
            }
        });

        controlServer.createContext("/api/test/issue-google-code", exchange -> {
            try {
                JsonNode body = readJson(exchange);
                String code = body.path("code").asText(UUID.randomUUID().toString());
                String nonce = body.path("nonce").asText("test-nonce");
                String sub = body.path("sub").asText("browser-owner-sub-1001");
                fakeGoogle.nonceByCode.put(code, nonce);
                fakeGoogle.subjectByCode.put(code, sub);
                sendJson(exchange, 200, Map.of("status", "OK", "code", code));
            } catch (Exception e) {
                sendJson(exchange, 500, Map.of("error", e.getMessage()));
            }
        });

        controlServer.createContext("/api/test/connect-slot", exchange -> {
            try {
                JsonNode body = readJson(exchange);
                int slot = body.path("slot").asInt(1);
                String sub = body.path("sub").asText("google-sub-" + slot);
                var consent = s1.authorize(slot, "1");
                String url = consent.response().authorizationUrl();
                String code = UUID.randomUUID().toString();
                String nonce = extractParam(url, "nonce");
                String state = extractParam(url, "state");
                fakeOAuth.issueCode(code, sub, nonce, "refresh-" + code);
                boolean success = s1.callback(state, code, null, null, consent.cookieValue()).success();
                sendJson(exchange, 200, Map.of("status", success ? "OK" : "FAILED", "slot", slot));
            } catch (Exception e) {
                sendJson(exchange, 500, Map.of("error", e.getMessage()));
            }
        });

        controlServer.createContext("/api/test/issue-drive-code", exchange -> {
            JsonNode body = readJson(exchange);
            String code = body.path("code").asText();
            fakeOAuth.issueCode(code, body.path("sub").asText(), body.path("nonce").asText(), "refresh-" + UUID.randomUUID());
            sendJson(exchange, 200, Map.of("issued", true));
        });

        controlServer.createContext("/api/test/advance-clock", exchange -> {
            long seconds = readJson(exchange).path("seconds").asLong();
            if (seconds < 0 || seconds > 3600) { sendJson(exchange, 400, Map.of("error", "bounded clock delta required")); return; }
            H1FFixtureConfiguration.clockOffsetSeconds += seconds;
            sendJson(exchange, 200, Map.of("advancedSeconds", seconds));
        });

        controlServer.createContext("/api/test/drive-behavior", exchange -> {
            JsonNode body = readJson(exchange);
            if (body.has("stableRefresh")) fakeOAuth.omitRefreshTokenOnRefresh = body.path("stableRefresh").asBoolean();
            if (body.has("loseUploadResponse")) fakeDrive.loseUploadResponse = body.path("loseUploadResponse").asBoolean();
            if (body.has("holdUpload") && body.path("holdUpload").asBoolean()) {
                fakeDrive.uploadEntered = new CountDownLatch(1);
                fakeDrive.uploadRelease = new CountDownLatch(1);
            }
            if (body.has("releaseUpload") && fakeDrive.uploadRelease != null) fakeDrive.uploadRelease.countDown();
            if (body.has("loseFolderResponse")) fakeDrive.loseRootResponse = body.path("loseFolderResponse").asBoolean();
            if (body.has("shortMedia")) fakeDrive.shortMedia = body.path("shortMedia").asLong();
            if (body.has("extraMedia")) fakeDrive.extraMedia = body.path("extraMedia").asInt();
            if (body.has("mediaChunked")) fakeDrive.mediaChunked = body.path("mediaChunked").asBoolean();
            if (body.has("mediaMode")) {
                fakeDrive.mediaMode = FakeGoogleDriveServer.Mode.valueOf(body.path("mediaMode").asText());
                fakeDrive.bodyRelease = new CountDownLatch(1);
            }
            if (body.has("releaseBody")) fakeDrive.bodyRelease.countDown();
            if (body.has("quotaOverride")) fakeDrive.quotaOverride = body.path("quotaOverride").asText();
            sendJson(exchange, 200, Map.of("configured", true));
        });

        controlServer.createContext("/api/test/update-remote", exchange -> {
            JsonNode changes = readJson(exchange).path("changes");
            if (!changes.isArray() || changes.size() > 64) { sendJson(exchange,400,Map.of("error","bounded changes required")); return; }
            for (JsonNode change : changes) {
                var remote = fakeDrive.files.get(change.path("id").asText());
                if (remote == null) { sendJson(exchange,404,Map.of("error","existing synthetic remote required")); return; }
                for (String field : List.of("modifiedTime","name","thumbnailLink")) {
                      if (change.has(field)) {
                          String value = change.path(field).asText();
                          if (field.equals("thumbnailLink") && value.equals("CONTROLLED_FIXTURE_THUMBNAIL")) value = fakeThumbnail.origin().toString() + "/thumbnail/image";
                          remote.metadata().put(field,value);
                      }
                }
            }
            sendJson(exchange,200,Map.of("updated",changes.size()));
        });

        controlServer.createContext("/api/test/hold-response", exchange -> {
            String path = readJson(exchange).path("path").asText();
            if (!path.equals("/api/v1/auth/google/handoff") && !path.matches("/api/v1/drive/connections/1/(items|storage-quota|folders|files/[A-Za-z0-9_-]+/thumbnail)")) {
                sendJson(exchange,400,Map.of("error","selected test response path required")); return;
            }
            H1FFixtureConfiguration.heldResponsePath = path;
            H1FFixtureConfiguration.responseEntered = new CountDownLatch(1);
            H1FFixtureConfiguration.responseRelease = new CountDownLatch(1);
            sendJson(exchange,200,Map.of("armed",true));
        });
        controlServer.createContext("/api/test/release-response", exchange -> {
            H1FFixtureConfiguration.heldResponsePath = null;
            if (H1FFixtureConfiguration.responseRelease != null) H1FFixtureConfiguration.responseRelease.countDown();
            sendJson(exchange,200,Map.of("released",true));
        });

        controlServer.createContext("/api/test/set-thumbnail", exchange -> {
            try {
                JsonNode body = readJson(exchange);
                if (body.has("status")) fakeThumbnail.responseStatus = body.get("status").asInt();
                if (body.has("contentType")) fakeThumbnail.responseContentType = body.get("contentType").asText();
                if (body.has("useWebp") && body.get("useWebp").asBoolean()) {
                    fakeThumbnail.responseContentType = "image/webp";
                    fakeThumbnail.responseBody = FakeGoogleThumbnailServer.defaultWebp();
                } else if (body.has("usePng") && body.get("usePng").asBoolean()) {
                    fakeThumbnail.responseContentType = "image/png";
                    fakeThumbnail.responseBody = FakeGoogleThumbnailServer.defaultPng();
                } else if (body.has("useJpeg") && body.get("useJpeg").asBoolean()) {
                    fakeThumbnail.responseContentType = "image/jpeg";
                    fakeThumbnail.responseBody = FakeGoogleThumbnailServer.defaultJpeg();
                }
                sendJson(exchange, 200, Map.of("status", "OK"));
            } catch (Exception e) {
                sendJson(exchange, 500, Map.of("error", e.getMessage()));
            }
        });

        controlServer.createContext("/api/test/refresh-state", exchange -> {
            JsonNode body = readJson(exchange);
            if (body.path("revokeActive").asBoolean()) jdbc.update("UPDATE refresh_tokens SET revoked_at=now() WHERE revoked_at IS NULL");
            H1FFixtureConfiguration.failRevoke = body.path("failRevoke").asBoolean();
            sendJson(exchange,200,Map.of("configured",true));
        });

        controlServer.createContext("/api/test/pause-refresh", exchange -> {
            H1FFixtureConfiguration.refreshPauseLatch = new CountDownLatch(1);
            H1FFixtureConfiguration.refreshEnteredLatch = new CountDownLatch(1);
            sendJson(exchange, 200, Map.of("status", "OK"));
        });

        controlServer.createContext("/api/test/wait-refresh-entered", exchange -> {
            try {
                boolean entered = H1FFixtureConfiguration.refreshEnteredLatch != null
                        && H1FFixtureConfiguration.refreshEnteredLatch.await(5, TimeUnit.SECONDS);
                sendJson(exchange, 200, Map.of("entered", entered));
            } catch (Exception e) {
                sendJson(exchange, 500, Map.of("error", e.getMessage()));
            }
        });

        controlServer.createContext("/api/test/release-refresh", exchange -> {
            if (H1FFixtureConfiguration.refreshPauseLatch != null) {
                H1FFixtureConfiguration.refreshPauseLatch.countDown();
                H1FFixtureConfiguration.refreshPauseLatch = null;
            }
            sendJson(exchange, 200, Map.of("status", "OK"));
        });

        controlServer.createContext("/api/test/stats", exchange -> {
            try {
                int refreshCount = jdbc.queryForObject("SELECT count(*) FROM refresh_tokens", Integer.class);
                int googleTokens = fakeGoogle.tokenRequestCount.get();
                int oauthTokens = fakeOAuth.tokenExchangeCount.get();
                int driveCalls = fakeDrive.calls.size();
                int thumbDispatches = fakeThumbnail.dispatches.get();
                int pendingTickets = jdbc.queryForObject("SELECT count(*) FROM ppv_google_handoff_tickets WHERE status='PENDING' AND expires_at > now()", Integer.class);
                DriveFileResources resources = context.getBean(DriveFileResources.class);
                Map<String,Object> stats = new LinkedHashMap<>();
                stats.put("refreshTokensInDb",refreshCount); stats.put("googleTokenRequests",googleTokens);
                stats.put("oauthTokenExchanges",oauthTokens); stats.put("driveCalls",driveCalls);
                stats.put("thumbnailDispatches",thumbDispatches); stats.put("pendingTickets",pendingTickets);
                stats.put("fileCreates",fakeDrive.fileCreates.get()); stats.put("childFolderCreates",fakeDrive.childFolderCreates.get());
                stats.put("refreshRequests",H1FFixtureConfiguration.refreshRequests.get());
                stats.put("activeRefreshTokens",jdbc.queryForObject("SELECT count(*) FROM refresh_tokens WHERE revoked_at IS NULL AND expires_at > now()",Integer.class));
                stats.put("springRequests",H1FFixtureConfiguration.springRequests.get());
                stats.put("activeOperations",resources.activeOperations()); stats.put("activeSpools",resources.activeSpools());
                stats.put("responseHeld",H1FFixtureConfiguration.responseEntered != null && H1FFixtureConfiguration.responseEntered.getCount() == 0);
                sendJson(exchange,200,stats);
            } catch (Exception e) {
                sendJson(exchange, 500, Map.of("error", e.getMessage()));
            }
        });

        controlServer.createContext("/api/test/shutdown", exchange -> {
            sendJson(exchange, 200, Map.of("status", "SHUTTING_DOWN"));
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    controlServer.stop(0);
                    context.close();
                    fakeGoogle.close();
                    fakeOAuth.close();
                    fakeDrive.close();
                    fakeThumbnail.close();
                    postgres.stop();
                    deleteDir(tempSpoolDir);
                    Files.deleteIfExists(tempGoogleKey);
                    Files.deleteIfExists(tempDriveKey);
                    System.exit(0);
                } catch (Exception ignored) {
                }
            }).start();
        });

        controlServer.start();
        int controlPort = controlServer.getAddress().getPort();

        ObjectNode readyJson = MAPPER.createObjectNode();
        readyJson.put("status", "READY");
        readyJson.put("springPort", SPRING_PORT);
        readyJson.put("controlPort", controlPort);
        readyJson.put("googleTokenUri", fakeGoogle.getTokenUri());
        readyJson.put("googleJwksUri", fakeGoogle.getJwksUri());
        readyJson.put("oauthTokenUri", fakeOAuth.getTokenEndpoint());
        readyJson.put("oauthRevokeUri", fakeOAuth.getRevokeEndpoint());
        readyJson.put("driveOrigin", fakeDrive.origin().toString());
        readyJson.put("thumbnailOrigin", fakeThumbnail.origin().toString());
        readyJson.put("postgresContainerId", postgres.getContainerId());
        readyJson.put("postgresImage", postgres.getDockerImageName());
        readyJson.put("javaVersion", System.getProperty("java.version"));
        readyJson.set("ownedPaths", MAPPER.valueToTree(List.of(
                tempSpoolDir.toAbsolutePath().toString(), tempGoogleKey.toAbsolutePath().toString(),
                tempDriveKey.toAbsolutePath().toString())));

        System.out.println("PPV_FIXTURE_JSON:" + MAPPER.writeValueAsString(readyJson));
        System.out.println("PPV_FIXTURE_READY " + controlPort);
        System.out.flush();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                controlServer.stop(0);
                context.close();
                fakeGoogle.close();
                fakeOAuth.close();
                fakeDrive.close();
                fakeThumbnail.close();
                postgres.stop();
                deleteDir(tempSpoolDir);
                Files.deleteIfExists(tempGoogleKey);
                Files.deleteIfExists(tempDriveKey);
            } catch (Exception ignored) {
            }
        }));
    }

    private static String extractParam(String url, String key) {
        String[] parts = url.split("[?&]");
        for (String p : parts) {
            if (p.startsWith(key + "=")) {
                return p.substring(key.length() + 1);
            }
        }
        return "";
    }

    private static JsonNode readJson(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return MAPPER.readTree(is);
        }
    }

    private static void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void deleteDir(Path dir) {
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }
}
