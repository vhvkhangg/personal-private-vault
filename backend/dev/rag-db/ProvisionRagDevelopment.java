import org.flywaydb.core.Flyway;

import java.sql.*;

/**
 * Separate local development migration/provision process; never part of the long-running app.
 */
class ProvisionRagDevelopment {
    static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Required development credential unavailable");
        return value;
    }

    public static void main(String[] args) {
        try {
            String url = required("PPV_RAG_MIGRATOR_URL"), user = required("PPV_RAG_MIGRATOR_USERNAME"), password = required("PPV_RAG_MIGRATOR_PASSWORD");
            if (!url.startsWith("jdbc:postgresql://127.0.0.1:"))
                throw new IllegalArgumentException("Development database must use loopback");
            Flyway.configure().dataSource(url, user, password).locations("filesystem:backend/src/main/resources/db/migration").load().migrate();
            try (var connection = DriverManager.getConnection(url, user, password)) {
                for (String role : new String[]{"rag_runtime", "rag_worker"}) {
                    String secret = required(role.equals("rag_runtime") ? "PPV_RAG_RUNTIME_PASSWORD" : "PPV_RAG_WORKER_PASSWORD");
                    if (secret.length() < 32)
                        throw new IllegalArgumentException("Development credentials must have at least 32 characters");
                    try (var query = connection.prepareStatement("SELECT format('ALTER ROLE %I LOGIN PASSWORD %L',?,?)")) {
                        query.setString(1, role);
                        query.setString(2, secret);
                        try (var result = query.executeQuery(); var statement = connection.createStatement()) {
                            result.next();
                            statement.execute(result.getString(1));
                        }
                    }
                }
            }
            System.out.println("Phase 17 local database migrated; isolated roles provisioned. No credentials emitted.");
        } catch (Exception failure) {
            System.err.println("Phase 17 local provisioning failed. Check database access, migrations and required environment.");
            System.exit(1);
        }
    }
}

