package com.vhvkhangg.personalprivatevault.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@Timeout(30)
class RagSourceAuthorityIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @Autowired RagOperations rag;
    @Autowired RagJobService jobs;
    @Autowired RagSourceAuthority authority;
    @Autowired PlatformTransactionManager transactions;
    @Autowired RagGenerationState current;
    @Autowired javax.sql.DataSource dataSource;

    @org.springframework.test.context.DynamicPropertySource
    static void isolateCurrentnessOrdering(org.springframework.test.context.DynamicPropertyRegistry properties) {
        properties.add("ppv.rag.reconciliation-delay-ms",()->"3600000");
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"affected","obsolete","sibling"})
    void committedInspectionIsRecheckedInsideFinalCurrentnessGuard(String scope) throws Exception {
        UUID a=externalCurrentFixture(), b=externalCurrentFixture();
        UUID generation=jdbcTemplate.queryForObject("SELECT generation_id FROM rag.index_generations WHERE source_id=(SELECT id FROM rag.sources WHERE source_id=?)",UUID.class,a.toString());
        assertThat(current.state(generation)).isEqualTo("CURRENT");
        UUID file="sibling".equals(scope)?b:a;
        UUID revision="obsolete".equals(scope)?UUID.randomUUID():jdbcTemplate.queryForObject("SELECT selected_revision_id FROM rag.external_file_sources WHERE file_source_id=?",UUID.class,file);
        UUID operation=UUID.randomUUID();
        try(var connection=dataSource.getConnection();var executor=java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            connection.setAutoCommit(false);
            try {
                int blocker=backendPid(connection);
                try(var lock=connection.createStatement()) {lock.execute("SELECT pg_advisory_xact_lock(5927097059089141831)");}
                var reading=executor.submit(()->current.state(generation));
                org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(4)).until(()->
                        jdbcTemplate.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE ?=ANY(pg_blocking_pids(pid)) AND wait_event='advisory'",Integer.class,blocker)>0);
                beginInspection(connection,operation,file,revision);
                connection.commit();
                assertThat(reading.get(6,java.util.concurrent.TimeUnit.SECONDS)).isEqualTo("affected".equals(scope)?"UNAVAILABLE":"CURRENT");
                assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,operation)).isEqualTo("UNRESOLVED");
                assertThat(jdbcTemplate.queryForObject("SELECT snapshot_integrity_failed FROM rag.external_file_sources WHERE file_source_id=?",Boolean.class,a)).isFalse();
                System.out.println("S19_CURRENTNESS_ORDER inspection_first=true scope="+scope+" committed_unresolved_checked=true");
            } finally {connection.rollback();}
        }
    }

    @Test void currentnessGuardFirstCompletesBeforeInspectionAndNextReadIsFenced() throws Exception {
        UUID file=externalCurrentFixture();
        UUID generation=jdbcTemplate.queryForObject("SELECT generation_id FROM rag.index_generations WHERE source_id=(SELECT id FROM rag.sources WHERE source_id=?)",UUID.class,file.toString());
        UUID revision=jdbcTemplate.queryForObject("SELECT selected_revision_id FROM rag.external_file_sources WHERE file_source_id=?",UUID.class,file);
        UUID operation=UUID.randomUUID();
        try(var leaseConnection=dataSource.getConnection();var inspectionConnection=dataSource.getConnection();
                var executor=java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            leaseConnection.setAutoCommit(false);inspectionConnection.setAutoCommit(false);
            try {
                int leasePid=backendPid(leaseConnection), inspectionPid=backendPid(inspectionConnection);
                try(var lock=leaseConnection.prepareStatement("SELECT l.id FROM rag.job_leases l JOIN rag.index_generations g ON g.job_id=l.job_id WHERE g.generation_id=? FOR UPDATE OF l")) {
                    lock.setObject(1,generation);lock.executeQuery().close();
                }
                var reading=executor.submit(()->current.state(generation));
                org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(4)).until(()->
                        jdbcTemplate.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE ?=ANY(pg_blocking_pids(pid)) AND wait_event_type='Lock'",Integer.class,leasePid)>0);
                int guardPid=jdbcTemplate.queryForObject("SELECT pid FROM pg_stat_activity WHERE ?=ANY(pg_blocking_pids(pid)) AND wait_event_type='Lock'",Integer.class,leasePid);
                var inspecting=executor.submit(()->{
                    beginInspection(inspectionConnection,operation,file,revision);inspectionConnection.commit();return operation;
                });
                org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(4)).until(()->
                        jdbcTemplate.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE pid=? AND ?=ANY(pg_blocking_pids(pid)) AND wait_event='advisory'",Integer.class,inspectionPid,guardPid)==1);
                leaseConnection.commit();
                assertThat(reading.get(6,java.util.concurrent.TimeUnit.SECONDS)).isEqualTo("CURRENT");
                assertThat(inspecting.get(6,java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(operation);
                assertThat(current.state(generation)).isEqualTo("UNAVAILABLE");
                assertThat(jdbcTemplate.queryForObject("SELECT state FROM rag.retrieval_inspections WHERE inspection_id=?",String.class,operation)).isEqualTo("UNRESOLVED");
                System.out.println("S19_CURRENTNESS_ORDER guard_first=true first=CURRENT next=UNAVAILABLE inspection=UNRESOLVED");
            } finally {leaseConnection.rollback();inspectionConnection.rollback();}
        }
    }

    private UUID externalCurrentFixture() {
        UUID file=UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO rag.external_file_sources(file_source_id,canonical_identity_digest) VALUES(?,?)",file,"currentness-order:"+file);
        completeRetrievalFixtureScope(Map.of(),List.of(file));return file;
    }

    private int backendPid(java.sql.Connection connection) throws java.sql.SQLException {
        try(var statement=connection.createStatement();var result=statement.executeQuery("SELECT pg_backend_pid()")) {result.next();return result.getInt(1);}
    }

    private void beginInspection(java.sql.Connection connection,UUID operation,UUID file,UUID revision) throws java.sql.SQLException {
        try(var statement=connection.prepareStatement("SELECT rag.retrieval_inspection_begin(?,NULL,'CURRENTNESS_ORDER_TEST','scope',?,?,?,?,?)")) {
            statement.setObject(1,operation);statement.setObject(2,file);statement.setObject(3,revision);
            statement.setLong(4,ProcessHandle.current().pid());
            statement.setLong(5,com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval.RagRetrievalControllerIdentity.creationFileTime());
            statement.setString(6,retrievalFixtureController());statement.execute();
        }
    }

    private RagSourceView approved(String title) {
        Long genre = jdbcTemplate.queryForObject("INSERT INTO fiction_genres(name) VALUES (?) RETURNING id", Long.class, UUID.randomUUID().toString());
        Long person = jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('PERSON') RETURNING id", Long.class);
        jdbcTemplate.update("INSERT INTO persons(id,name) VALUES (?,'Synthetic person')", person);
        Long id = jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FICTION') RETURNING id", Long.class);
        jdbcTemplate.update("INSERT INTO fictions(id,title,format,genre_id,author_person_id,description,is_nsfw) VALUES (?,?,'NOVEL',?,?,'Synthetic description',false)", id,title,genre,person);
        return RagTestCommands.approve(rag, RagTestCommands.register(rag, RagSourceType.FICTION,id.toString()).id(),Set.of("title","description"));
    }


    @Autowired com.vhvkhangg.personalprivatevault.fiction.source.FictionSourceOperations fictionSources;
    @Autowired com.vhvkhangg.personalprivatevault.film.source.FilmSourceOperations filmSources;
    @Autowired jakarta.persistence.EntityManagerFactory entityManagerFactory;

    @Test void everyAllowedFieldAndConsumptionStateHasTypedSelectedOnlyInput() throws Exception {
        Long filmId=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FILM') RETURNING id",Long.class);
        jdbcTemplate.update("INSERT INTO films(id,title,format,production_style,is_nsfw) VALUES (?,'Synthetic film','SERIES','LIVE_ACTION',false)",filmId);
        var fiction=approved("Synthetic fields");
        var film=RagTestCommands.register(rag, RagSourceType.FILM,filmId.toString());
        for(var source:List.of(fiction,film)) {
            String table=source.sourceType()==RagSourceType.FICTION?"fictions":"films";
            var states=jdbcTemplate.queryForList("SELECT unnest(enum_range(NULL::consumption_status))::text",String.class);
            for(String state:states) {
                jdbcTemplate.update("UPDATE "+table+" SET consumption_status=CAST(? AS consumption_status) WHERE id=?",state,Long.valueOf(source.sourceId()));
                for(String field:com.vhvkhangg.personalprivatevault.rag.internal.domain.RagFieldAllowlist.getAllowedFields(source.sourceType())) {
                    RagTestCommands.approve(rag, source.id(),Set.of(field));
                    var proof=new TransactionTemplate(transactions).execute(status->authority.lockSource(source.id()));
                    var values=new ObjectMapper().readTree(Objects.requireNonNull(proof).payload()).path("sources").get(0).path("fields");
                    assertThat(values.size()).isEqualTo(1);
                    assertThat(values.has(field)).as(field+" / "+state).isTrue();
                    if(field.equals("consumptionStatus"))assertThat(values.path(field).asText()).isEqualTo(state);
                }
            }
        }
    }

    @Test void nullEmptyAndWhitespaceDescriptionsHaveDistinctAuthoritativeRevisions() {
        var source=approved("Synthetic exact bytes");
        Long id=Long.valueOf(source.sourceId());
        Set<String> revisions=new HashSet<>();
        for(String value:Arrays.asList(null,""," ","Synthetic description","Synthetic description ")) {
            jdbcTemplate.update("UPDATE fictions SET description=? WHERE id=?",value,id);
            revisions.add(fictionSources.findSource(id).orElseThrow().descriptionRevisionHash());
        }
        assertThat(revisions).hasSize(5);
    }

    @Test void genuinelyFreshBatchProjectionsHaveConstantQueryCountsAndEnforceByteBudget() {
        List<Long> fictionIds=new ArrayList<>(),filmIds=new ArrayList<>();
        for(int i=0;i<8;i++) {
            fictionIds.add(Long.valueOf(approved("Synthetic batch "+i).sourceId()));
            Long id=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FILM') RETURNING id",Long.class);
            jdbcTemplate.update("INSERT INTO films(id,title,format,production_style,is_nsfw) VALUES (?,'Synthetic batch film','SERIES','LIVE_ACTION',false)",id);
            filmIds.add(id);
        }
        var statistics=entityManagerFactory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        boolean enabled=statistics.isStatisticsEnabled();statistics.setStatisticsEnabled(true);
        try {
            for(int n:List.of(1,8)) {
                statistics.clear();assertThat(fictionSources.findSources(fictionIds.subList(0,n))).hasSize(n);
                assertThat(statistics.getPrepareStatementCount()).isEqualTo(5);
                statistics.clear();assertThat(filmSources.findSources(filmIds.subList(0,n))).hasSize(n);
                assertThat(statistics.getPrepareStatementCount()).isEqualTo(6);
            }
        }finally {statistics.setStatisticsEnabled(enabled);}
        jdbcTemplate.update("UPDATE fictions SET description=repeat('x',1048577) WHERE id=?",fictionIds.getFirst());
        assertThatThrownBy(()->fictionSources.findSources(fictionIds)).isInstanceOf(IllegalStateException.class).hasMessageContaining("byte budget");
        assertThatThrownBy(()->fictionSources.findSources(java.util.stream.LongStream.range(1,102).boxed().toList()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("selection budget");
    }
    @Test
    void nsfwAfterAdmissionNeverReachesWorker() {
        var source = approved("Synthetic restricted title");
        var attempt = RagTestCommands.index(rag, source.id());
        assertThatThrownBy(()->jdbcTemplate.update("UPDATE fictions SET is_nsfw=true WHERE id=?",Long.valueOf(source.sourceId())))
                .satisfies(error->assertThat(sqlState(error)).isEqualTo("P1701"));
        rag.cancelJob(attempt.jobId());
        jdbcTemplate.update("UPDATE fictions SET is_nsfw=true WHERE id=?",Long.valueOf(source.sourceId()));
        assertThatThrownBy(() -> jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs",Integer.class)).isZero();
    }

    @Test
    void trashChangedRevisionAndFieldReductionDenyInput() {
        for (String mutation : List.of("UPDATE vault_entries SET deleted_at=clock_timestamp() WHERE id=?",
                "UPDATE fictions SET title='Changed title' WHERE id=?")) {
            var source = approved("Synthetic old title");
            var attempt = RagTestCommands.index(rag, source.id());
            assertThatThrownBy(()->jdbcTemplate.update(mutation,Long.valueOf(source.sourceId())))
                    .satisfies(error->assertThat(sqlState(error)).isEqualTo("P1701"));
            rag.cancelJob(attempt.jobId());
            jdbcTemplate.update(mutation,Long.valueOf(source.sourceId()));
            assertThatThrownBy(() -> jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST"))
                    .isInstanceOf(IllegalStateException.class);
        }
        var source = approved("Synthetic field title");
        var attempt = RagTestCommands.index(rag, source.id());
        assertThatThrownBy(()->RagTestCommands.approve(rag, source.id(),Set.of("title")))
                .satisfies(error->assertThat(sqlState(error)).isEqualTo("P1701"));
        rag.cancelJob(attempt.jobId());
        RagTestCommands.approve(rag, source.id(),Set.of("title"));
        assertThatThrownBy(() -> jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void newTransitiveChildInvalidatesOldGrantAndPublication() {
        var root = approved("Root"); var child = approved("Child"); var grandchild = approved("Grandchild");
        rag.addDependency(root.id(),child.id());
        var attempt = RagTestCommands.index(rag, root.id());
        assertThatThrownBy(()->rag.addDependency(child.id(),grandchild.id()))
                .satisfies(error->assertThat(sqlState(error)).isEqualTo("P1701"));
        rag.cancelJob(attempt.jobId());
        rag.addDependency(child.id(),grandchild.id());
        jdbcTemplate.update("UPDATE fictions SET is_nsfw=true WHERE id=?",Long.valueOf(grandchild.sourceId()));
        assertThatThrownBy(() -> jobs.executeSyntheticStage(attempt.jobId(),attempt.attemptId(),"SYNTHETIC_TEST"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> rag.finalizeGeneration(attempt.jobId(),attempt.attemptId(),null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE is_active",Integer.class)).isZero();
    }

    @Test
    void inputContainsExactlyApprovedTypedFieldsAndNormalJsonEscaping() throws Exception {
        var source = approved("Synthetic \"title\"\n\\backslash");
        var snapshot = new TransactionTemplate(transactions).execute(status -> authority.lockSource(source.id()));
        var node = new ObjectMapper().readTree(Objects.requireNonNull(snapshot).payload()).path("sources").get(0).path("fields");
        assertThat(node.size()).isEqualTo(2);
        assertThat(node.path("title").asText()).isEqualTo("Synthetic \"title\"\n\\backslash");
        assertThat(node.path("description").asText()).isEqualTo("Synthetic description");
        assertThat(node.has("review")).isFalse();
    }

    @Test
    void externalCapabilityCannotBeApprovedOrDispatchedEvenFromRestoredCatalog() {
        Long id = jdbcTemplate.queryForObject("INSERT INTO rag.sources(source_type,source_id,classification,approved_fields_text,access_state,source_revision_hash,description_revision_hash,created_at,updated_at) VALUES ('EXTERNAL_FILE','synthetic.txt','NON_PRIVATE','content','APPROVED','x','x',clock_timestamp(),clock_timestamp()) RETURNING id",Long.class);
        var restored=rag.findSource(id).orElseThrow();
        assertThatThrownBy(() -> rag.approveSource(id,new RagConsentCommand(Set.of("content"),1,1,
                restored.authorizationEpoch(),restored.fieldPolicyVersion(),null)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("disabled");
        assertThatThrownBy(() -> rag.admitJob(id,new RagIndexCommand(1,restored.authorizationEpoch(),restored.fieldPolicyVersion(),Map.of())))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("disabled");
    }

    @Test void admissionQueueCannotGrowBeyondTheReviewedGlobalBudget() {
        var source=approved("Synthetic queue budget");
        var unrelated=approved("Synthetic unrelated queue contender");
        RagTestCommands.index(rag, source.id());
        assertThatThrownBy(()->RagTestCommands.index(rag, unrelated.id())).isInstanceOf(RagJobBusyException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.job_leases",Integer.class)).isEqualTo(1);
    }

    private static String sqlState(Throwable error) {
        while (error != null) {
            if (error instanceof java.sql.SQLException sql) return sql.getSQLState();
            error = error.getCause();
        }
        throw new AssertionError("Expected actual PostgreSQL failure");
    }
}
