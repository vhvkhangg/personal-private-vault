package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.*;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

/** Real restricted SQL-route tests; synthetic process metadata is not native lifecycle proof. */
@Timeout(20)
class RagStagingCommitFenceIntegrationTest extends AbstractPostgresIntegrationTest {
    @Autowired private JdbcTemplate jdbcTemplate;
    private Long sourceId;
    private final UUID jobId=UUID.randomUUID(),attemptId=UUID.randomUUID(),sessionId=UUID.randomUUID();
    private final String token="a".repeat(64);
    private String manifest,inputDigest,scopeDigest,outputDigest,receipt;

    @BeforeEach void setUp() throws Exception {
        Long owner=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES('FILM') RETURNING id",Long.class);
        jdbcTemplate.update("INSERT INTO films(id,title,format,production_style) VALUES(?,'Synthetic SQL stage','MOVIE','ANIMATION')",owner);
        sourceId=jdbcTemplate.queryForObject("""
                INSERT INTO rag.sources(source_type,source_id,classification,authorization_epoch,field_policy_version,
                    approved_fields_text,access_state,source_revision_hash,description_revision_hash,approved_owner_reset_version,created_at,updated_at)
                VALUES('FILM',?,'NON_PRIVATE',1,1,'title','APPROVED','synthetic-source','',1,clock_timestamp(),clock_timestamp()) RETURNING id
                """,Long.class,owner.toString());
        long version=jdbcTemplate.queryForObject("SELECT source_version FROM vault_rag_source_state WHERE source_id=?",Long.class,owner);
        var node=new TreeMap<String,Object>();
        node.put("source_id",sourceId);node.put("source_type","FILM");node.put("domain_id",owner.toString());
        node.put("epoch",1);node.put("field_version",1);node.put("fields",List.of("title"));
        node.put("content_revision","synthetic-source");node.put("description_revision","");
        node.put("owner_source_version",version);node.put("owner_reset_version",1);
        manifest=new ObjectMapper().writeValueAsString(Map.of("contract_version","2.0","root_id",sourceId,"nodes",List.of(node),"edges",List.of()));
        inputDigest=digest("synthetic");scopeDigest=digest(manifest);outputDigest=digest("SYNTHETIC_TEST:synthetic");
        receipt=new ObjectMapper().writeValueAsString(Map.of("contract_version","2.0","status","STAGED","job_id",jobId.toString(),
                "attempt_id",attemptId.toString(),"stage_type","SYNTHETIC_TEST","input_digest",inputDigest,"scope_digest",scopeDigest,
                "output_digest",outputDigest,"output_kind","SYNTHETIC_DIGEST"));
    }
    @AfterEach void cleanUp() {
        // Administrative fixture reset only, never a production history deletion route.
        jdbcTemplate.execute("TRUNCATE vault_rag_source_state,rag.runs,rag.dependencies,rag.staged_outputs,rag.worker_grants,rag.job_leases,rag.index_generations,rag.sources,vault_entries CASCADE");
    }
    private void lease(String state,boolean expired) throws Exception {
        UUID runtime=UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO rag.runtime_state(singleton,current_instance) VALUES(true,?) ON CONFLICT(singleton) DO UPDATE SET current_instance=excluded.current_instance",runtime);
        String controller=new ObjectMapper().writeValueAsString(Map.of("process_id",42,"started_at",Instant.now().toString(),"runtime_id",runtime.toString()));
        String worker=new ObjectMapper().writeValueAsString(Map.of("process_id",43,"started_at",Instant.now().toString(),"endpoint_nonce",UUID.randomUUID().toString()));
        var deadline=Timestamp.from(Instant.now().plusSeconds(expired?-5:600));
        jdbcTemplate.update("""
                INSERT INTO rag.job_leases(job_id,attempt_id,source_id,status,lease_token,leased_until,created_at,updated_at,
                    reservation_token,controller_identity,worker_identity,dependency_manifest)
                VALUES(?,?,?,?,?,?,clock_timestamp(),clock_timestamp(),?,CAST(? AS jsonb),CAST(? AS jsonb),?)
                """,jobId,attemptId,sourceId,state,token,deadline,attemptId,controller,worker,manifest);
        jdbcTemplate.update("""
                INSERT INTO rag.worker_grants(job_id,attempt_id,session_id,session_until,input_digest,scope_digest,output_digest,stage_type,consumed_at)
                VALUES(?,?,?,?,?,?,?,'SYNTHETIC_TEST',?)
                """,jobId,attemptId,sessionId,deadline,inputDigest,scopeDigest,outputDigest,
                expired?Timestamp.from(Instant.now().minusSeconds(10)):null);
    }
    private void claim() throws SQLException {
        try(var worker=RagWorkerFixture.connection();var statement=worker.prepareStatement("SELECT rag.claim_worker_input(?,?,?,?)")) {
            statement.setObject(1,jobId);statement.setObject(2,attemptId);statement.setString(3,token);statement.setObject(4,sessionId);
            try(var result=statement.executeQuery()) { assertThat(result.next()).isTrue();assertThat(result.getBoolean(1)).isTrue(); }
        }
    }
    private long stage(UUID job,UUID attempt,String presentedToken,boolean legacy) throws SQLException {
        try(var worker=RagWorkerFixture.connection();var statement=worker.prepareStatement(legacy
                ?"SELECT rag.stage_worker_output(?,?,?,'SYNTHETIC_TEST',?)":"SELECT rag.stage_worker_receipt(?,?,?,?)")) {
            String offered=receipt.replace(jobId.toString(),job.toString()).replace(attemptId.toString(),attempt.toString());
            statement.setObject(1,job);statement.setObject(2,attempt);statement.setString(3,presentedToken);statement.setString(4,offered);
            try(var result=statement.executeQuery()) { if(!result.next())throw new AssertionError("Missing stage result");return result.getLong(1); }
        }
    }
    private void denied(UUID job,UUID attempt,String presentedToken) {
        assertThatThrownBy(() -> stage(job,attempt,presentedToken,false)).isInstanceOf(PSQLException.class)
                .satisfies(error -> assertThat(((SQLException)error).getSQLState()).isEqualTo("23514"));
    }
    private static String digest(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    @Test @DisplayName("Restricted SQL permits the exact typed issued receipt on an active unexpired attempt")
    void allowsStagingWhenLeaseActiveAndUnexpired() throws Exception {
        lease("ACTIVE",false);claim();
        long row=stage(jobId,attemptId,token,false);
        assertThat(row).isPositive();assertThat(stage(jobId,attemptId,token,true)).isEqualTo(row);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=? AND worker_receipt",Integer.class,jobId)).isEqualTo(1);
    }
    @Test @DisplayName("Original no-lease negative: restricted receipt route denies an unknown attempt")
    void blocksStagingWhenNoLeaseRecord() { denied(UUID.randomUUID(),UUID.randomUUID(),token); }
    @Test @DisplayName("Original forged-token negative remains denied on the actual restricted route")
    void blocksStagingWhenTokenMismatched() throws Exception { lease("ACTIVE",false);claim();denied(jobId,attemptId,"forged-token"); }
    @Test @DisplayName("Original revoked-attempt identity: approved cancel-first STOPPING state denies staging")
    void blocksStagingWhenLeaseRevoked() throws Exception { lease("STOPPING",false);denied(jobId,attemptId,token); }
    @Test @DisplayName("Expired execution still denies a newly observed staging gate under E1")
    void blocksStagingWhenLeaseExpired() throws Exception { lease("ACTIVE",true);denied(jobId,attemptId,token); }
    @Test @DisplayName("Private source is rejected even with deliberately stale trusted issuance fixture metadata")
    void blocksStagingWhenSourceIsPrivate() throws Exception {
        lease("ACTIVE",false);claim();jdbcTemplate.update("UPDATE rag.sources SET classification='PRIVATE' WHERE id=?",sourceId);
        denied(jobId,attemptId,token);
    }
    @Test @DisplayName("Revoked source is rejected even with deliberately stale trusted issuance fixture metadata")
    void blocksStagingWhenSourceIsRevoked() throws Exception {
        lease("ACTIVE",false);claim();jdbcTemplate.update("UPDATE rag.sources SET access_state='REVOKED' WHERE id=?",sourceId);
        denied(jobId,attemptId,token);
    }
}
