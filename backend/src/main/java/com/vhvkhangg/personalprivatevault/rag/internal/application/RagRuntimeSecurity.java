package com.vhvkhangg.personalprivatevault.rag.internal.application;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Unsafe legacy/migrator identities can never authorize foundation source or worker work.
 */
@Component
public class RagRuntimeSecurity {
    private final JdbcTemplate jdbc;
    private final RagRuntimeIdentity runtime;

    public RagRuntimeSecurity(JdbcTemplate jdbc, RagRuntimeIdentity runtime) {
        this.jdbc = jdbc;
        this.runtime = runtime;
    }

    public boolean isIsolatedRole() {
        Boolean safe = jdbc.queryForObject("""
            SELECT current_user='rag_runtime' AND NOT (rolsuper OR rolcreatedb OR rolcreaterole OR rolinherit OR rolbypassrls OR rolreplication)
             AND NOT EXISTS(SELECT 1 FROM pg_catalog.pg_auth_members WHERE member=r.oid)
             AND NOT EXISTS(SELECT 1 FROM pg_catalog.pg_class WHERE relowner=r.oid)
             AND NOT EXISTS(SELECT 1 FROM pg_catalog.pg_namespace WHERE nspowner=r.oid)
             AND NOT EXISTS(SELECT 1 FROM pg_catalog.pg_proc WHERE proowner=r.oid)
             AND NOT EXISTS(SELECT 1 FROM pg_catalog.pg_database WHERE datdba=r.oid)
             AND NOT EXISTS(SELECT 1 FROM pg_catalog.pg_type WHERE typowner=r.oid)
                 AND NOT has_database_privilege(current_user,current_database(),'CREATE')
                 AND NOT has_database_privilege(current_user,current_database(),'TEMP')
            FROM pg_catalog.pg_roles r WHERE rolname=current_user
            """, Boolean.class);
        return Boolean.TRUE.equals(safe);
    }

    public boolean isCurrentRuntime() {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM rag.runtime_state WHERE singleton AND current_instance=?)",
            Boolean.class, runtime.instance()));
    }

    public void requireIsolatedRuntime() {
        if (!isIsolatedRole() || !isCurrentRuntime())
            throw new IllegalStateException("RAG runtime identity is not isolated");
    }
}
