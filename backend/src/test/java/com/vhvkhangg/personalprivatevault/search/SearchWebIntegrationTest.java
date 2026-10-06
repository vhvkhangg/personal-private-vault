package com.vhvkhangg.personalprivatevault.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.vhvkhangg.personalprivatevault.search.enums.SearchDomain;
import com.vhvkhangg.personalprivatevault.search.internal.web.controller.GlobalSearchController;
import com.vhvkhangg.personalprivatevault.search.query.GlobalSearchOperations;
import com.vhvkhangg.personalprivatevault.search.query.GlobalSearchQuery;
import com.vhvkhangg.personalprivatevault.search.view.GlobalSearchPage;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchWebIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    @DisplayName("GET /api/v1/search requires authentication and returns search results with truthful page metadata")
    void searchEndpointOverHttp() throws Exception {
        // Unauthenticated -> 401
        mockMvc.perform(get("/api/v1/search").param("q", "architecture"))
                .andExpect(status().isUnauthorized());

        // Authenticated -> 200 OK with truthful ApiPageMeta and default limit 50, offset 0
        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", bearerHeader())
                        .param("q", "architecture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.meta.page.offset").value(0))
                .andExpect(jsonPath("$.meta.page.limit").value(50))
                .andExpect(jsonPath("$.meta.page.hasMore").isBoolean());
    }

    @Test
    @DisplayName("GET /api/v1/search validates offset and limit bounds strictly without silent rewriting")
    void searchValidatesOffsetAndLimitBoundsStrictly() throws Exception {
        // offset > 500 -> 400
        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", bearerHeader())
                        .param("q", "test")
                        .param("offset", "501")
                        .param("limit", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // offset < 0 -> 400
        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", bearerHeader())
                        .param("q", "test")
                        .param("offset", "-1")
                        .param("limit", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // limit > 100 -> 400
        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", bearerHeader())
                        .param("q", "test")
                        .param("offset", "0")
                        .param("limit", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // limit <= 0 -> 400
        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", bearerHeader())
                        .param("q", "test")
                        .param("offset", "0")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Pagination bounds across Knowledge and Media reject invalid values without silent clamping")
    void paginationAcrossModulesRejectsInvalidBounds() throws Exception {
        // Knowledge vocabulary-due limit invalid -> 400
        mockMvc.perform(get("/api/v1/knowledge/vocabulary/due")
                        .header("Authorization", bearerHeader())
                        .param("limit", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/api/v1/knowledge/vocabulary/due")
                        .header("Authorization", bearerHeader())
                        .param("limit", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        // Media images offset/limit invalid -> 400
        mockMvc.perform(get("/api/v1/albums/1/images")
                        .header("Authorization", bearerHeader())
                        .param("offset", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/api/v1/albums/1/images")
                        .header("Authorization", bearerHeader())
                        .param("limit", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("GET /api/v1/search passes through singular repeated filters with discriminating results and truthful pagination")
    void searchPassesThroughFiltersAndNonZeroOffset() throws Exception {
        // 1. Seed 3 films and 1 person sharing keyword "Matrix"
        String filmPayloadTemplate = """
                {
                  "title": "%s",
                  "format": "MOVIE",
                  "productionStyle": "LIVE_ACTION",
                  "isNsfw": false,
                  "progressStatus": "COMPLETED",
                  "consumptionStatus": "CONSUMED"
                }
                """;

        String f1Resp = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmPayloadTemplate.formatted("Matrix Reloaded")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long film1Id = objectMapper.readTree(f1Resp).path("data").path("id").asLong();

        String f2Resp = mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmPayloadTemplate.formatted("Matrix Revolutions")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long film2Id = objectMapper.readTree(f2Resp).path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/films")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmPayloadTemplate.formatted("Matrix Resurrections")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/people")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Matrix Wachowski\"}"))
                .andExpect(status().isCreated());

        // 2. Create tag and attach only to film1 and film2
        String tagResp = mockMvc.perform(post("/api/v1/vault/tags")
                        .header("Authorization", bearerHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"cyberpunk\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long tagId = objectMapper.readTree(tagResp).path("data").path("id").asLong();

        mockMvc.perform(put("/api/v1/vault/entries/{id}/tags/{tagId}", film1Id, tagId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/vault/entries/{id}/tags/{tagId}", film2Id, tagId)
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // 2b. Independent domain filter proof: q=Matrix & domain=FILM (no tag filter)
        // Discriminates and proves domain filtering independently excludes Person "Matrix Wachowski" while returning all 3 films
        String domainOnlyResp = mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", bearerHeader())
                        .param("q", "Matrix")
                        .param("domain", "FILM")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode domainOnlyRoot = objectMapper.readTree(domainOnlyResp);
        assertThat(domainOnlyRoot.path("data").size()).isEqualTo(3);
        for (JsonNode item : domainOnlyRoot.path("data")) {
            assertThat(item.path("domain").asText()).isEqualTo("FILM");
            assertThat(item.path("primaryText").asText()).isNotEqualTo("Matrix Wachowski");
        }

        // 3. Page 0: q=Matrix, domain=FILM, entryType=FILM, tagId=<tagId>, offset=0, limit=1
        // Discriminating proof: excludes Person "Matrix Wachowski" and untagged Film "Matrix Resurrections"
        String page0Resp = mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", bearerHeader())
                        .param("q", "Matrix")
                        .param("domain", "FILM")
                        .param("entryType", "FILM")
                        .param("tagId", tagId.toString())
                        .param("offset", "0")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode page0Root = objectMapper.readTree(page0Resp);
        assertThat(page0Root.path("data").isArray()).isTrue();
        assertThat(page0Root.path("data").size()).isEqualTo(1);
        assertThat(page0Root.path("meta").path("page").path("offset").asInt()).isEqualTo(0);
        assertThat(page0Root.path("meta").path("page").path("limit").asInt()).isEqualTo(1);
        assertThat(page0Root.path("meta").path("page").path("hasMore").asBoolean()).isTrue();
        String item0Text = page0Root.path("data").get(0).path("primaryText").asText();
        assertThat(item0Text).isIn("Matrix Reloaded", "Matrix Revolutions");
        assertThat(page0Root.path("data").get(0).path("domain").asText()).isEqualTo("FILM");
        assertThat(page0Root.path("data").get(0).path("entryType").asText()).isEqualTo("FILM");

        // 4. Page 1: same filters with non-zero offset=1, limit=1
        String page1Resp = mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", bearerHeader())
                        .param("q", "Matrix")
                        .param("domain", "FILM")
                        .param("entryType", "FILM")
                        .param("tagId", tagId.toString())
                        .param("offset", "1")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode page1Root = objectMapper.readTree(page1Resp);
        assertThat(page1Root.path("data").isArray()).isTrue();
        assertThat(page1Root.path("data").size()).isEqualTo(1);
        assertThat(page1Root.path("meta").path("page").path("offset").asInt()).isEqualTo(1);
        assertThat(page1Root.path("meta").path("page").path("limit").asInt()).isEqualTo(1);
        assertThat(page1Root.path("meta").path("page").path("hasMore").asBoolean()).isFalse();
        String item1Text = page1Root.path("data").get(0).path("primaryText").asText();
        assertThat(item1Text).isIn("Matrix Reloaded", "Matrix Revolutions");
        assertThat(item1Text).isNotEqualTo(item0Text);
        assertThat(page1Root.path("data").get(0).path("domain").asText()).isEqualTo("FILM");
        assertThat(page1Root.path("data").get(0).path("entryType").asText()).isEqualTo("FILM");
    }

    @Test
    @DisplayName("Limit-only queries (e.g. knowledge vocabulary due, reference catalogs) do not fabricate page metadata")
    void limitOnlyQueriesDoNotFabricatePageMetadata() throws Exception {
        String vocabResponse = mockMvc.perform(get("/api/v1/knowledge/vocabulary/due")
                        .header("Authorization", bearerHeader())
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode vocabRoot = objectMapper.readTree(vocabResponse);
        assertThat(vocabRoot.has("data")).isTrue();
        assertThat(vocabRoot.path("data").isArray()).isTrue();
        assertThat(vocabRoot.has("meta")).isTrue();
        assertThat(vocabRoot.get("meta").isNull()).isTrue();

        String refResponse = mockMvc.perform(get("/api/v1/reference/countries")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode refRoot = objectMapper.readTree(refResponse);
        assertThat(refRoot.has("data")).isTrue();
        assertThat(refRoot.path("data").isArray()).isTrue();
        assertThat(refRoot.has("meta")).isTrue();
        assertThat(refRoot.get("meta").isNull()).isTrue();
    }

    @Test
    @DisplayName("Search adapter correctly binds repeated query parameter filter sets into GlobalSearchQuery unchanged")
    void searchAdapterBindsRepeatedFilterSetsToGlobalSearchQueryUnchanged() throws Exception {
        GlobalSearchOperations mockOps = Mockito.mock(GlobalSearchOperations.class);
        Mockito.when(mockOps.search(Mockito.any()))
                .thenReturn(new GlobalSearchPage(10, 25, List.of(), false));

        MockMvc standaloneMvc = MockMvcBuilders
                .standaloneSetup(new GlobalSearchController(mockOps))
                .build();

        standaloneMvc.perform(get("/api/v1/search")
                        .param("q", "repeated filter test")
                        .param("domain", "FILM")
                        .param("domain", "FICTION")
                        .param("entryType", "FILM")
                        .param("entryType", "FICTION")
                        .param("tagId", "101")
                        .param("tagId", "202")
                        .param("offset", "10")
                        .param("limit", "25"))
                .andExpect(status().isOk());

        ArgumentCaptor<GlobalSearchQuery> queryCaptor = ArgumentCaptor.forClass(GlobalSearchQuery.class);
        Mockito.verify(mockOps).search(queryCaptor.capture());

        GlobalSearchQuery captured = queryCaptor.getValue();
        assertThat(captured.query()).isEqualTo("repeated filter test");
        assertThat(captured.domains()).containsExactlyInAnyOrder(SearchDomain.FILM, SearchDomain.FICTION);
        assertThat(captured.entryTypes()).containsExactlyInAnyOrder(VaultEntryType.FILM, VaultEntryType.FICTION);
        assertThat(captured.requiredTagIds()).containsExactlyInAnyOrder(101L, 202L);
        assertThat(captured.offset()).isEqualTo(10);
        assertThat(captured.limit()).isEqualTo(25);
    }
}
