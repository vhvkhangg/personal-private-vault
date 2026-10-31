package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagQaOperations;
import com.vhvkhangg.personalprivatevault.rag.api.RagRetrievalCancelResult;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaContextPacker;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaService;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaSupervisor;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.RagQaValidator;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalService;
import com.vhvkhangg.personalprivatevault.rag.internal.web.controller.RagQaController;
import com.vhvkhangg.personalprivatevault.rag.internal.web.controller.RagRetrievalController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY.
 * Authority: docs/implementation/phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md.
 * Checks registration and MVC routing only: no database, worker, model, CUDA or QA evaluation.
 */
class RagQaArchiveGuardTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(ArchiveBoundaryConfiguration.class)
            .withBean(RagRetrievalService.class, () -> mock(RagRetrievalService.class));

    @Test
    void ordinaryDefaultsExcludeQaButKeepAcceptedRetrievalRoutes() {
        runner.run(RagQaArchiveGuardTest::verifyArchiveBoundary);
    }

    @Test
    void explicitNonTestExecutionExcludesQa() {
        runner.withPropertyValues("ppv.rag.test-scoped-execution=false")
                .run(RagQaArchiveGuardTest::verifyArchiveBoundary);
    }

    @Test
    void qaEnablementAloneCannotRegisterArchivedComponents() {
        runner.withPropertyValues(
                        "ppv.rag.qa.enabled=true",
                        "ppv.rag.retrieval.enabled=true",
                        "ppv.rag.qa.cuda-test-enabled=true",
                        "ppv.rag.qa.model-path=archive-model-must-never-be-opened.gguf")
                .run(RagQaArchiveGuardTest::verifyArchiveBoundary);
    }

    private static void verifyArchiveBoundary(AssertableWebApplicationContext context) throws Exception {
        assertThat(context).hasNotFailed()
                .doesNotHaveBean(RagQaController.class)
                .doesNotHaveBean(RagQaService.class)
                .doesNotHaveBean(RagQaOperations.class)
                .doesNotHaveBean(RagQaSupervisor.class)
                .doesNotHaveBean(RagQaContextPacker.class)
                .doesNotHaveBean(RagQaValidator.class)
                .hasSingleBean(RagRetrievalController.class);

        var routes = context.getBean(RequestMappingHandlerMapping.class).getHandlerMethods()
                .keySet().stream().flatMap(mapping -> mapping.getPatternValues().stream())
                .collect(Collectors.toSet());
        assertThat(routes).containsExactlyInAnyOrder(
                "/api/v1/rag/retrievals", "/api/v1/rag/retrievals/{requestId}/cancel");

        var mvc = MockMvcBuilders.webAppContextSetup(context.getSourceApplicationContext()).build();
        mvc.perform(post("/api/v1/rag/qa")).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/rag/qa/{requestId}/cancel", UUID.randomUUID()))
                .andExpect(status().isNotFound());

        var retrieval = context.getBean(RagRetrievalService.class);
        var requestId = UUID.randomUUID();
        when(retrieval.cancel(requestId)).thenReturn(
                new RagRetrievalCancelResult(requestId, "TERMINAL", "Archive route check"));
        mvc.perform(post("/api/v1/rag/retrievals/{requestId}/cancel", requestId))
                .andExpect(status().isOk());
        verify(retrieval).cancel(requestId);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({RagRetrievalController.class, RagQaController.class, RagQaService.class,
            RagQaSupervisor.class, RagQaContextPacker.class, RagQaValidator.class})
    static class ArchiveBoundaryConfiguration {
    }
}
