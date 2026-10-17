package com.vhvkhangg.personalprivatevault.rag;

import com.sun.net.httpserver.HttpServer;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagSourceAuthority;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagWorkerGrantService;
import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagWorkerGrant;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.client.RagEngineClient;
import jakarta.persistence.EntityManager;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/** Temporary denial proof; this does not certify an enabled consumption protocol. */
@Timeout(10)
class RagConsumptionContainmentTest {
    @Test
    void issuanceCannotReadContentOrPersistAGrantBeforeTheFenceIsProven() {
        var authority = mock(RagSourceAuthority.class);
        var entityManager = mock(EntityManager.class);
        var grants = new RagWorkerGrantService(authority, entityManager);
        assertThatThrownBy(() -> grants.issue(UUID.randomUUID(), UUID.randomUUID(), "SYNTHETIC_TEST"))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("execution is disabled");
        verifyNoInteractions(authority, entityManager);
    }

    @Test
    void previouslyIssuedGrantCannotSendContentOverHttp() throws Exception {
        var requests = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            exchange.close();
        });
        server.start();
        try {
            var client = new RagEngineClient("http://127.0.0.1:" + server.getAddress().getPort(), "x".repeat(32), 1);
            var grant = new RagWorkerGrant(UUID.randomUUID(), UUID.randomUUID(), "a".repeat(64),
                "SYNTHETIC_TEST", "Synthetic restricted marker", "b".repeat(64), "c".repeat(64), "d".repeat(64));
            assertThatThrownBy(() -> client.triggerWorkerStage(grant))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("execution is disabled");
            assertThat(requests.get()).isZero();
        } finally {
            server.stop(0);
        }
    }
}
