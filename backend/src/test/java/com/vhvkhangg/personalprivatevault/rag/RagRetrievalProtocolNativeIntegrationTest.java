package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagQueryUnavailableException;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.NativeWorkerProcess;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagRetrievalReaderRepository;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.*;
import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/** Actual owned Windows processes; controlled negative peers do not certify embedding quality. */
class RagRetrievalProtocolNativeIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @TempDir Path work;
    @Autowired Environment environment;
    @Autowired RagRetrievalAuthority authority;
    @Autowired RagRuntimeIdentity runtime;
    @Autowired RagRetrievalReaderRepository readers;

    @DynamicPropertySource static void enabled(DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.retrieval.enabled",()->true);
        properties.add("ppv.rag.reconciliation-delay-ms",()->"3600000");
    }

    @ParameterizedTest
    @ValueSource(strings={"wrong_nonce","wrong_pid","wrong_contract","oversized_response","duplicate_json","invalid_vector","wrong_embedding_nonce","wrong_tokenizer_artifact","stalled_embedding"})
    void realNativeProtocolRejectsWrongIdentityAndBoundedMalformedResponses(String mode) throws Exception {
        Path packageDir=work.resolve("src/ppv_rag");Files.createDirectories(packageDir);
        Files.writeString(packageDir.resolve("__init__.py"),"");
        String script="""
                from __future__ import annotations
                import json
                import math
                import os
                import time
                from pathlib import Path
                from fastapi import FastAPI
                from fastapi.responses import Response

                app = FastAPI()
                mode = %s

                def identity() -> dict[str, object]:
                    return {
                        "reader_id": os.environ["PPV_RAG_READER_ID"],
                        "endpoint_nonce": os.environ["PPV_RAG_NONCE"],
                        "process_id": os.getpid(), "contract_version": "1.0", "dimensions": 384,
                        "model_version": "2.0.0",
                        "artifact_hash": "98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f",
                        "tokenizer_version": "hf-tokenizers-0.23.3",
                        "tokenizer_artifact_hash": "2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8",
                    }

                @app.get("/v1/query/readiness")
                def readiness() -> Response:
                    data = identity()
                    data["status"] = "READY"
                    if mode == "wrong_nonce":
                        data["endpoint_nonce"] = "wrong"
                    if mode == "wrong_pid":
                        data["process_id"] = os.getpid() + 1
                    if mode == "wrong_contract":
                        data["contract_version"] = "0.9"
                    return Response(json.dumps(data), media_type="application/json")

                @app.post("/v1/query/embed")
                def embed() -> Response:
                    Path(__file__).resolve().parents[2].joinpath("question-transferred").write_text("1")
                    if mode == "stalled_embedding":
                        time.sleep(30)
                    data = identity()
                    data["vector"] = [1 / math.sqrt(384)] * 384
                    if mode == "wrong_embedding_nonce":
                        data["endpoint_nonce"] = "wrong"
                    if mode == "wrong_tokenizer_artifact":
                        data["tokenizer_artifact_hash"] = "0" * 64
                    if mode == "invalid_vector":
                        data["vector"] = [0.0] * 384
                    body = json.dumps(data)
                    if mode == "duplicate_json":
                        body = '{"reader_id":"duplicate",' + body[1:]
                    if mode == "oversized_response":
                        body = '{"padding":"' + 'x' * 65537 + '"}'
                    return Response(body, media_type="application/json")
                """.formatted(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(mode));
        Files.writeString(packageDir.resolve("query_embedding.py"),script);
        runRejected(work,"stalled_embedding".equals(mode));
        if(Set.of("wrong_nonce","wrong_pid","wrong_contract").contains(mode)) {
            assertThat(work.resolve("question-transferred")).doesNotExist();
        } else {
            assertThat(work.resolve("question-transferred")).hasContent("1");
        }
    }

    @ParameterizedTest @ValueSource(booleans={false,true})
    void actualPinnedPythonRejectsMissingAndHashMismatchedArtifactsWithoutFetch(boolean wrongHash) throws Exception {
        Path original=repository().resolve("rag-engine/src/ppv_rag");
        try(var files=Files.walk(original)) {
            for(Path file:files.filter(Files::isRegularFile).filter(p->p.toString().endsWith(".py")).toList()) {
                Path target=work.resolve("src/ppv_rag").resolve(original.relativize(file));
                Files.createDirectories(target.getParent());Files.copy(file,target);
            }
        }
        Path model=work.resolve("models/paraphrase-multilingual-MiniLM-L12-v2");
        if(wrongHash) {
            Files.createDirectories(model);Files.writeString(model.resolve("model.onnx"),"INVALID_MODEL_ARTIFACT");
            Files.writeString(model.resolve("tokenizer.json"),"{}");
        }
        runRejected(work);
        if(wrongHash) {
            try(var files=Files.list(model)) { assertThat(files.map(p->p.getFileName().toString()).toList()).containsExactlyInAnyOrder("model.onnx","tokenizer.json"); }
            assertThat(model.resolve("model.onnx")).hasContent("INVALID_MODEL_ARTIFACT");
        } else {
            assertThat(model).doesNotExist();
        }
    }

    private void runRejected(Path directory) {
        runRejected(directory,false);
    }

    private void runRejected(Path directory,boolean stalled) {
        UUID file=UUID.randomUUID(),reader=UUID.randomUUID(),token=UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO rag.external_file_sources(file_source_id,canonical_identity_digest) VALUES(?,?)",file,"native-protocol:"+file);
        var scope=completeRetrievalFixtureScope(Map.of(),List.of(file));
        authority.admit(reader,token,"protocol-negative",scope,List.of(file),runtime.instance().toString());
        var supervisor=new RagQueryEmbeddingSupervisor(true,true,environment.getRequiredProperty("ppv.rag.worker.executable"),
                environment.getRequiredProperty("ppv.rag.worker.venv-launcher"),directory.toString(),authority,runtime);
        long started=System.nanoTime();
        try {
            if(stalled) {
                try(var executor=java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
                    var computing=executor.submit(()->{
                        try { supervisor.computeEmbedding(reader,"QUESTION_PRIVATE_SENTINEL");return false; }
                        catch(RagQueryUnavailableException stopped) { return true; }
                    });
                    org.awaitility.Awaitility.await().atMost(Duration.ofSeconds(8)).until(()->Files.exists(directory.resolve("question-transferred")));
                    assertThatThrownBy(()->jdbcTemplate.queryForObject("SELECT rag.external_file_require_editable(ARRAY[?]::uuid[])",Object.class,file))
                            .hasMessageContaining("RAG_SOURCE_BUSY");
                    authority.requestStop(reader,"USER_CANCELLED");long stopStarted=System.nanoTime();
                    supervisor.stopWorker(reader);
                    long stoppedNanos=System.nanoTime()-stopStarted;
                    assertThat(stoppedNanos).isLessThan(Duration.ofSeconds(5).toNanos());
                    assertThat(computing.get(6,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
                    assertThat(readers.findByReaderId(reader).orElseThrow().getState()).isNotEqualTo("TERMINAL");
                    System.out.println("S19_STALLED_NATIVE_STOP nanos="+stoppedNanos+" exact_stop=true java_joined=true exclusion_retained=true");
                } catch(Exception failure) { throw new AssertionError("Native stop proof failed",failure); }
                return;
            }
            assertThatThrownBy(()->supervisor.computeEmbedding(reader,"QUESTION_PRIVATE_SENTINEL"))
                    .isInstanceOf(RagQueryUnavailableException.class).hasMessageNotContaining("QUESTION_PRIVATE_SENTINEL")
                    .hasMessageNotContaining(directory.toString());
            assertThat(System.nanoTime()-started).isLessThan(Duration.ofSeconds(45).toNanos());
        } finally {
            supervisor.stopWorker(reader);
            var bound=readers.findByReaderId(reader).orElseThrow();
            assertThat(bound.getWorkerPid()).isNotNull();assertThat(bound.getWorkerCreationFileTime()).isNotNull();
            assertThat(NativeWorkerProcess.isRecordedProcessTerminated(bound.getWorkerPid(),bound.getWorkerCreationFileTime(),Duration.ofSeconds(5))).isTrue();
            authority.finish(reader,token,"EXECUTION_FAILURE","ABORTED",noSenderEvidence("NATIVE_PROTOCOL_STOP_PROVEN"),null,null);
            supervisor.forgetStopped(reader);
        }
    }
}
