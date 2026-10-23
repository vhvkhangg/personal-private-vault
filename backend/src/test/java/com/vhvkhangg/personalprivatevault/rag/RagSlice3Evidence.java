package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import java.nio.file.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

/** Synthetic integration receipts only; never enabled in the application. */
final class RagSlice3Evidence {
    static void capture(JdbcTemplate jdbc,ObjectMapper json,String test) throws Exception {
        String configured=System.getenv("PPV_S3_EVIDENCE");if(configured==null)return;
        Path directory=Path.of(configured).resolve("native-bindings");Files.createDirectories(directory);
        var jobs=new ArrayList<Map<String,Object>>();
        for(var row:jdbc.queryForList("SELECT job_id,attempt_id,status,staging_closed,dependency_manifest,worker_identity::text AS identity FROM rag.job_leases ORDER BY id")) {
            UUID job=(UUID)row.get("job_id");var item=new LinkedHashMap<String,Object>(row);
            item.put("manifest_sha256",RagSourceAuthority.digest((String)item.remove("dependency_manifest")));
            item.put("grants",jdbc.queryForList("SELECT stage_type,input_digest,scope_digest,output_digest,consumed_at IS NOT NULL AS consumed FROM rag.worker_grants WHERE job_id=?",job));
            var roster=new ArrayList<Map<String,Object>>();
            for(var output:jdbc.queryForList("SELECT root_key,success,reason_code,worker_receipt,jsonb_array_length(chunks_payload) AS chunk_count,chunks_payload::text AS chunks,resource_report::text AS resources FROM rag.staged_document_outputs WHERE job_id=? ORDER BY id",job)) {
                var staged=new LinkedHashMap<String,Object>(output);String chunks=(String)staged.remove("chunks");staged.put("chunks_sha256",RagSourceAuthority.digest(chunks));
                // Preserve untrusted raw JSON even when Jackson deliberately
                // refuses an oversized number in the rejection fixture.
                staged.put("resources_sha256",RagSourceAuthority.digest((String)staged.get("resources")));roster.add(staged);
            }
            item.put("actual_roster",roster);item.put("published",jdbc.queryForList("SELECT generation_id,status,is_active,dependency_manifest_digest FROM rag.index_generations WHERE job_id=? ORDER BY id",job));
            item.put("outcomes",jdbc.queryForList("SELECT root_key,result,candidate_generation_id FROM rag.file_outcomes WHERE job_id=? ORDER BY root_key",job));jobs.add(item);
        }
        var sources=jdbc.queryForList("SELECT file_source_id,source_version,consent_reset_version,selected_revision_id,pending_revision_id,approved_pending_revision_id,pending_revision_hash,pending_byte_length,selected_revision_hash,selected_byte_length,reserved_by FROM rag.external_file_sources ORDER BY file_source_id");
        var value=Map.of("test",test,"jobs",jobs,"sources",sources);
        Files.write(directory.resolve(UUID.randomUUID()+".json"),json.writerWithDefaultPrettyPrinter().writeValueAsBytes(value),StandardOpenOption.CREATE_NEW);
    }
}
