package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.internal.application.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.qa.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.*;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;

public class RagRecoveryQuiescenceUnitTest {

    @Test
    @DisplayName("isQuiescentReader selects ownership-aware between retrieval and QA")
    void ownershipAwareQuiescentSelection() throws Exception {
        RagRetrievalService retrieval = Mockito.mock(RagRetrievalService.class);
        RagQaService qa = Mockito.mock(RagQaService.class);
        RagRuntimeSecurity security = Mockito.mock(RagRuntimeSecurity.class);
        RagSnapshotInspections inspections = Mockito.mock(RagSnapshotInspections.class);
        RagNoChangeService noChange = Mockito.mock(RagNoChangeService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<RagQaService> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(provider.getIfAvailable()).thenReturn(qa);
        PlatformTransactionManager manager = Mockito.mock(PlatformTransactionManager.class);

        RagRecoveryService recovery = new RagRecoveryService(
                null, null, null, manager, security, null, null, noChange, null, retrieval, inspections, provider
        );

        Method method = RagRecoveryService.class.getDeclaredMethod("isQuiescentReader", UUID.class);
        method.setAccessible(true);

        UUID retrievalReader = UUID.randomUUID();
        Mockito.when(retrieval.hasSender(retrievalReader)).thenReturn(true);
        Mockito.when(retrieval.isQuiescentReader(retrievalReader)).thenReturn(true);
        Mockito.when(qa.hasSender(retrievalReader)).thenReturn(false);

        boolean retrievalResult = (boolean) method.invoke(recovery, retrievalReader);
        assertThat(retrievalResult).isTrue();

        UUID qaReader = UUID.randomUUID();
        Mockito.when(retrieval.hasSender(qaReader)).thenReturn(false);
        Mockito.when(qa.hasSender(qaReader)).thenReturn(true);
        Mockito.when(qa.isQuiescentReader(qaReader)).thenReturn(true);

        boolean qaResult = (boolean) method.invoke(recovery, qaReader);
        assertThat(qaResult).isTrue();

        UUID unownedReader = UUID.randomUUID();
        Mockito.when(retrieval.hasSender(unownedReader)).thenReturn(false);
        Mockito.when(qa.hasSender(unownedReader)).thenReturn(false);

        boolean unownedResult = (boolean) method.invoke(recovery, unownedReader);
        assertThat(unownedResult).isFalse();
    }

    @Test
    @DisplayName("Periodic reconcile invokes noChange.reconcile() before inspection recovery")
    void periodicReconcileRestoresNoChange() {
        RagRetrievalService retrieval = Mockito.mock(RagRetrievalService.class);
        RagQaService qa = Mockito.mock(RagQaService.class);
        RagRuntimeSecurity security = Mockito.mock(RagRuntimeSecurity.class);
        RagSnapshotInspections inspections = Mockito.mock(RagSnapshotInspections.class);
        RagNoChangeService noChange = Mockito.mock(RagNoChangeService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<RagQaService> provider = Mockito.mock(ObjectProvider.class);
        Mockito.when(provider.getIfAvailable()).thenReturn(qa);
        PlatformTransactionManager manager = Mockito.mock(PlatformTransactionManager.class);

        RagRecoveryService recovery = new RagRecoveryService(
                null, null, null, manager, security, null, null, noChange, null, retrieval, inspections, provider
        );

        Mockito.when(security.isIsolatedRole()).thenReturn(true);
        Mockito.when(security.isCurrentRuntime()).thenReturn(true);

        Mockito.doThrow(new IllegalStateException("Stop after inspection reconcile ordering check"))
                .when(inspections).reconcile(anySet(), any());

        try {
            recovery.reconcile();
        } catch (IllegalStateException expected) {
            // expected
        }

        Mockito.verify(noChange, Mockito.times(1)).reconcile();
    }
}
