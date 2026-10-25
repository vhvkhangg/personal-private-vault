package com.vhvkhangg.personalprivatevault.rag;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.*;

/** Sanitized actual source -> immutable R -> native worker -> trusted replay -> PostgreSQL. */
@Timeout(240)
class RagSlice4ResourceIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @TempDir Path work;
    @Autowired RagSourceAuthority authority;
    @AfterEach void nativeBindings(TestInfo test) throws Exception {RagSlice3Evidence.capture(jdbcTemplate,objectMapper,test.getDisplayName());}
    RagSourceView source(String name,String text) throws Exception {
        Path path=work.resolve(name); Files.createDirectories(path.getParent());
        Files.writeString(path,text,StandardCharsets.UTF_8);
        var v=enabledRag.registerSnapshotCandidate(path.toString());
        return enabledRag.approveSource(v.id(),new RagConsentCommand(Set.of("content"),v.sourceVersion(),
                v.consentResetVersion(),v.authorizationEpoch(),v.fieldPolicyVersion(),null,v.snapshot()));
    }
    RagJobAdmissionResult stage(RagSourceView... views) {
        var roots=new ArrayList<RagDocumentRootItem>(); var tasks=new ArrayList<RagDocumentBatchTask>();
        for (int i=0;i<views.length;i++) {
            var v=views[i]; String key="root-"+i;
            roots.add(new RagDocumentRootItem(key,v.sourceType(),v.sourceId(),v.sourceVersion(),v.authorizationEpoch(),v.fieldPolicyVersion(),v.approvedFields()));
            tasks.add(new RagDocumentBatchTask(key,v.sourceId(),"MD",null,null,null,List.of("content"),Map.of()));
        }
        var job=enabledRag.admitDocumentBatch(new RagDocumentBatchCommand(roots,Map.of()));
        enabledRag.executeDocumentBatch(job.jobId(),job.attemptId(),tasks); return job;
    }
    RagBatchFinalizationResult finish(RagJobAdmissionResult job) {
        String manifest=jdbcTemplate.queryForObject("SELECT dependency_manifest FROM rag.job_leases WHERE job_id=?",String.class,job.jobId());
        return enabledRag.finalizeDocumentBatch(job.jobId(),job.attemptId(),new RagDocumentBatchFinalizeCommand(RagSourceAuthority.digest(manifest)));
    }

    String structured(String label,int i) {
        return "## "+label+" "+i+"\n\nEnglish source records preserve exact authority and distinct item "+i+".\n\nTi\u1ebfng Vi\u1ec7t gi\u1eef nguy\u00ean n\u1ed9i dung v\u00e0 v\u1ecb tr\u00ed. \u7b80\u4f53\u4e2d\u6587\u4e0e\u7e41\u9ad4\u4e2d\u6587\u4fdd\u7559\u4f86\u6e90\u3002\n\n"
            +"| Key | Value |\n| --- | --- |\n| item-"+i+" | inert table cell |\n\n> [!note] Context\n> Approved inert content "+i+".\n\n```text\n![[literal-no-child-"+i+"]]\n```\n\n";
    }
    void record(String scenario,RagJobAdmissionResult job,List<RagSourceView> inputs,Map<String,Object> extra) throws Exception {
        String output=System.getenv("PPV_S3_EVIDENCE");if(output==null)return;
        Path dir=repository().resolve(output).resolve("resource-workloads");Files.createDirectories(dir);
        var roots=new ArrayList<Map<String,Object>>();
        for(var v:inputs) {
            var p=authority.prepareSource(v.id(),Map.of());
            roots.add(Map.of("source_id",v.sourceId(),"source_version",v.sourceVersion(),"snapshot",v.snapshot(),"manifest_sha256",RagSourceAuthority.digest(p.manifest()),"prepared_utf8_bytes",p.payload().getBytes(StandardCharsets.UTF_8).length));
        }
        var result=new LinkedHashMap<String,Object>();result.put("scenario",scenario);result.put("job_id",job.jobId());result.put("attempt_id",job.attemptId());result.put("roots",roots);result.put("pipeline",RagDocumentFinalizationGate.PIPELINE);result.put("limits",Map.of("java_input_bytes",1048576,"producer_source_characters",1000000,"producer_root_deadline_seconds",60));result.put("extra",extra);
        result.put("producer_result_rows",jdbcTemplate.queryForList("SELECT root_key,success,reason_code,resource_report::text FROM rag.staged_document_outputs WHERE job_id=? ORDER BY id",job.jobId()));
        result.put("grant",jdbcTemplate.queryForList("SELECT input_digest,scope_digest,output_digest FROM rag.worker_grants WHERE job_id=?",job.jobId()));
        Files.writeString(dir.resolve(scenario+"-"+UUID.randomUUID()+".json"),objectMapper.writeValueAsString(result),StandardOpenOption.CREATE_NEW);
    }

    @Test void actualColdWarmRootsApprovedNestedChildrenAndSameRevisionReindex() throws Exception {
        var grand=source("Grand.md","# Grand\n\n"+structured("grand",1));
        var child=source("Child.md","---\naliases: [\u5b50\u8cc7\u6599]\n---\n# Child\n\n![[Grand]]\n\n"+structured("child",2));
        var root=source("Root.md","# Root\n\n![[\u5b50\u8cc7\u6599]]\n\n"+structured("root",3));
        var warm=source("Warm.md","# Independent warm root\n\n"+structured("warm",4));
        enabledRag.addDependency(child.id(),grand.id());enabledRag.addDependency(root.id(),child.id());
        root=enabledRag.findSource(root.id()).orElseThrow();warm=enabledRag.findSource(warm.id()).orElseThrow();
        var job=stage(root,warm);record("cold-first-warm-second-shared-model-session",job,List.of(root,warm),Map.of("warm_definition","Second root in the same native process/session, not cross-job cache"));
        var first=finish(job);assertThat(first.publishedCount()).isEqualTo(2);
        var selected=enabledRag.findSource(root.id()).orElseThrow();
        Files.delete(work.resolve("Root.md"));Files.delete(work.resolve("Child.md"));Files.delete(work.resolve("Grand.md"));
        var reindex=stage(selected);record("selected-R-reindex",reindex,List.of(selected),Map.of("original_removed_before_admission",true));
        assertThat(finish(reindex).publishedCount()).isEqualTo(1);
        assertThat(enabledRag.findSource(root.id()).orElseThrow().snapshot().selectedRevisionId()).isEqualTo(selected.snapshot().selectedRevisionId());
    }

    @Test void actualNearInputByteCeilingIncludesStructuredSourceAndApprovedChild() throws Exception {
        var child=source("Child.md","# Child near bound\n\n"+structured("child",42).repeat(4));
        var text=new StringBuilder("# Near admitted input ceiling\n\n![[Child]]\n\n");int bytes=text.toString().getBytes(StandardCharsets.UTF_8).length;
        for(int i=0;;i++) {StringBuilder passage=new StringBuilder(structured("Distinct note section",i));
            for(int j=0;j<24;j++)passage.append("Entry ").append(i).append(" item ").append(j).append(" records an independent archive observation with explicit source boundaries, retained context and exact immutable provenance. This synthetic note compares chronology, material dependencies and the recorded decision for that entry. ");
            passage.append("\n\n");String section=passage.toString();int size=section.getBytes(StandardCharsets.UTF_8).length;if(bytes+size>(1048576-32768)/2)break;text.append(section);bytes+=size;}
        var root=source("Near.md",text.toString());enabledRag.addDependency(root.id(),child.id());root=enabledRag.findSource(root.id()).orElseThrow();
        // The prepared compatibility envelope contains text twice; its 1 MiB ceiling is unchanged.
        var proof=authority.prepareSource(root.id(),Map.of());int transported=proof.payload().getBytes(StandardCharsets.UTF_8).length;assertThat(transported).isLessThanOrEqualTo(1048576);
        var job=stage(root);record("near-admitted-byte-bound",job,List.of(root),Map.of("original_bytes",bytes,"source_characters",text.length(),"source_sha256",RagSourceAuthority.digest(text.toString()),"prepared_bytes",transported,"fraction_of_java_input_ceiling",transported/1048576.0));
        assertThat(finish(job).publishedCount()).isEqualTo(1);
    }
}
