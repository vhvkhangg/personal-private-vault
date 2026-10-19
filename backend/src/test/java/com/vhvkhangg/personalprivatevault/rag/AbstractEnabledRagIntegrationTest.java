package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Actual isolated Windows native/HTTP/worker-role runtime; default containment is unchanged. */
abstract class AbstractEnabledRagIntegrationTest extends AbstractRagIntegrationTest {
    private static final String WORKER_PASSWORD = UUID.randomUUID().toString().replace("-", "");
    private static final String SERVICE_KEY = UUID.randomUUID() + "-" + UUID.randomUUID();
    private static final int WORKER_PORT = freePort();

    @Autowired
    protected RagOperations enabledRag;

    @DynamicPropertySource
    static void enableIsolatedWorker(DynamicPropertyRegistry properties) {
        Path worker = repository().resolve("rag-engine");
        properties.add("ppv.rag.test-scoped-execution", () -> "true");
        properties.add("ppv.rag.engine.url", () -> "http://127.0.0.1:" + WORKER_PORT);
        properties.add("ppv.rag.engine.service-key", () -> SERVICE_KEY);
        properties.add("ppv.rag.worker.executable", () -> basePython(worker).toString());
        properties.add("ppv.rag.worker.venv-launcher", () -> worker.resolve(".venv/Scripts/python.exe").toString());
        properties.add("ppv.rag.worker.working-directory", worker::toString);
        properties.add("ppv.rag.worker.dsn", () -> "postgresql://rag_worker:" + WORKER_PASSWORD
                + "@127.0.0.1:" + POSTGRES.getMappedPort(5432) + "/" + POSTGRES.getDatabaseName());
    }

    @BeforeEach
    void configureActualWorkerCredential() throws Exception {
        ensureWorkerCredential();
    }

    static void ensureWorkerCredential() throws SQLException {
        try (var connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
                var statement = connection.createStatement()) {
            statement.execute("ALTER ROLE rag_worker LOGIN PASSWORD '" + WORKER_PASSWORD + "'");
        }
    }

    /** Do not administratively erase unresolved reservations or silently leave a consumer alive. */
    @Override
    @AfterEach
    protected void cleanUpDatabase() {
        var unfinished = jdbcTemplate.queryForList("""
                SELECT job_id FROM rag.job_leases
                WHERE status IN ('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING')
                ORDER BY id
                """, UUID.class);
        for (UUID job : unfinished) {
            enabledRag.cancelJob(job);
        }
        assertThat(jdbcTemplate.queryForObject("""
                SELECT count(*) FROM rag.job_leases
                WHERE status IN ('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING')
                """, Integer.class)).as("actual consumers/SQL must be safely terminal before fixture erasure").isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL",
                Integer.class)).as("unresolved reservations must not be administratively hidden").isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM rag.external_file_sources WHERE reserved_by IS NOT NULL",
                Integer.class)).as("unresolved external file reservations must not be administratively hidden").isZero();
        super.cleanUpDatabase();
    }


    protected static Path repository() {
        return Files.isDirectory(Path.of("rag-engine")) ? Path.of(".").toAbsolutePath().normalize()
                : Path.of("..").toAbsolutePath().normalize();
    }

    protected static Connection workerConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), "rag_worker", WORKER_PASSWORD);
        try (var statement = connection.createStatement()) {
            statement.execute("SET statement_timeout='5s'");
            statement.execute("SET lock_timeout='3s'");
        } catch (SQLException failure) {
            connection.close();
            throw failure;
        }
        return connection;
    }

    private static Path basePython(Path worker) {
        assertThat(System.getProperty("os.name")).startsWith("Windows");
        try {
            String home = Files.readAllLines(worker.resolve(".venv/pyvenv.cfg")).stream()
                    .filter(line -> line.startsWith("home = ")).map(line -> line.substring(7))
                    .findFirst().orElseThrow();
            Path executable = Path.of(home).resolve("python.exe");
            assertThat(executable).isRegularFile();
            return executable;
        } catch (IOException failure) {
            throw new IllegalStateException("Actual isolated Python installation is unavailable", failure);
        }
    }

    private static int freePort() {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            return socket.getLocalPort();
        } catch (IOException failure) {
            throw new IllegalStateException("Isolated worker loopback port unavailable", failure);
        }
    }
}
