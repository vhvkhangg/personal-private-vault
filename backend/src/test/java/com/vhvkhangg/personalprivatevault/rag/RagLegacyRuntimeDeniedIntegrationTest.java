package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagOperations;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.*;

class RagLegacyRuntimeDeniedIntegrationTest extends AbstractWebIntegrationTest {
    @Autowired RagOperations rag;
    @Test void legacyAdminDatasourceCannotAuthorizeOrExposeRagCatalog() {
        assertThat(jdbcTemplate.queryForObject("SELECT current_user",String.class)).isEqualTo(POSTGRES.getUsername());
        assertThatThrownBy(()->RagTestCommands.register(rag, RagSourceType.FICTION,"1"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("identity is not isolated");
        assertThatThrownBy(()->rag.listSources(0,10)).isInstanceOf(IllegalStateException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.sources",Integer.class)).isZero();
    }
}

