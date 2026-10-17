package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.*;
import com.vhvkhangg.personalprivatevault.rag.internal.application.*;
import com.vhvkhangg.personalprivatevault.rag.source.*;
import com.vhvkhangg.personalprivatevault.fiction.fiction.FictionOperations;
import com.vhvkhangg.personalprivatevault.fiction.fiction.command.UpdateFictionCommand;
import com.vhvkhangg.personalprivatevault.film.film.FilmOperations;
import com.vhvkhangg.personalprivatevault.film.film.command.UpdateFilmCommand;
import com.vhvkhangg.personalprivatevault.vault.entry.VaultEntryOperations;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Timeout(180)
class RagFinalizerOwnerConcurrencyIntegrationTest extends AbstractEnabledRagIntegrationTest {
    @Autowired RagOperations rag;
    @Autowired RagRecoveryService recovery;
    @Autowired FictionOperations fiction;
    @Autowired FilmOperations film;
    @Autowired VaultEntryOperations vault;
    @Autowired PlatformTransactionManager transactions;
    @Autowired EntityManager entityManager;
    @MockitoSpyBean RagSourceAuthority authority;
    private record Fixture(Long domain,RagSourceView source,RagJobAdmissionResult attempt,RagSourceType type) {}

    private Fixture fixture(RagSourceType type) {
        return fixture(type,true);
    }
    private Fixture fixture(RagSourceType type,boolean dispatched) {
        Long id=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES (?::vault_entry_type) RETURNING id",Long.class,type.name());
        if(type==RagSourceType.FICTION) {
            Long genre=jdbcTemplate.queryForObject("INSERT INTO fiction_genres(name) VALUES (?) RETURNING id",Long.class,UUID.randomUUID().toString());
            Long person=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('PERSON') RETURNING id",Long.class);
            jdbcTemplate.update("INSERT INTO persons(id,name) VALUES (?,'Synthetic writer')",person);
            jdbcTemplate.update("INSERT INTO fictions(id,title,format,genre_id,author_person_id,is_nsfw) VALUES (?,'Synthetic work','NOVEL',?,?,false)",id,genre,person);
        }else jdbcTemplate.update("INSERT INTO films(id,title,format,production_style,is_nsfw) VALUES (?,'Synthetic work','MOVIE','LIVE_ACTION',false)",id);
        var source=RagTestCommands.approve(rag, RagTestCommands.register(rag, type,id.toString()).id(),Set.of("title","description"));
        var attempt=dispatched?RagTestCommands.index(rag, source.id()):null;
        if(dispatched)RagWorkerFixture.stage(rag,attempt);
        return new Fixture(id,source,attempt,type);
    }
    private void mutate(Fixture fixture,String action) {
        Long id=fixture.domain();
        if(action.equals("TRASH")){vault.moveToTrash(id);return;}
        if(action.equals("TRASH_RESTORE")){vault.moveToTrash(id);vault.restore(id);return;}
        if(action.equals("STORY")){
            Long value=jdbcTemplate.queryForObject("SELECT min(id) FROM story_archetypes",Long.class);
            if(fixture.type()==RagSourceType.FICTION)fiction.addStoryArchetype(id,value);else film.addStoryArchetype(id,value);return;
        }
        if(action.equals("WORLD")){
            Long value=jdbcTemplate.queryForObject("SELECT min(id) FROM world_settings",Long.class);
            if(fixture.type()==RagSourceType.FICTION)fiction.addWorldSetting(id,value);else film.addWorldSetting(id,value);return;
        }
        if(action.equals("GENRE")){
            Long value=jdbcTemplate.queryForObject("INSERT INTO film_genres(name) VALUES (?) RETURNING id",Long.class,UUID.randomUUID().toString());
            film.addGenre(id,value);return;
        }
        if(fixture.type()==RagSourceType.FICTION) {
            var view=fiction.find(id).orElseThrow();
            fiction.update(new UpdateFictionCommand(id,action.equals("CONTENT")?"Changed work":view.title(),view.originalTitle(),
                    view.nationalityCode(),view.posterUrl(),view.format(),action.equals("NSFW"),view.genreId(),
                    view.authorPersonId(),view.authorGroupId(),view.description(),view.totalChapters(),view.progressStatus(),
                    view.consumptionStatus(),view.currentProgressText(),view.review()));
        }else {
            var view=film.find(id).orElseThrow();
            film.update(new UpdateFilmCommand(id,action.equals("CONTENT")?"Changed work":view.title(),view.originalTitle(),
                    view.nationalityCode(),view.posterUrl(),view.format(),view.productionStyle(),action.equals("NSFW"),view.directorPersonId(),
                    view.description(),view.totalEpisodes(),view.progressStatus(),view.consumptionStatus(),view.currentProgressText(),view.review()));
        }
    }
    private List<String> actions(RagSourceType type) {
        return type==RagSourceType.FILM?List.of("NSFW","CONTENT","STORY","WORLD","GENRE","TRASH","TRASH_RESTORE")
                :List.of("NSFW","CONTENT","STORY","WORLD","TRASH","TRASH_RESTORE");
    }
    @Test void actualFinalizerLastReadGuardBlocksAllOwnerMutationsThroughCommit() throws Exception {
        for(var type:List.of(RagSourceType.FICTION,RagSourceType.FILM))for(String action:actions(type)) {
            Fixture fixture=fixture(type);
            var read=new CountDownLatch(1);var release=new CountDownLatch(1);
            var writerCommitted=new AtomicBoolean();
            doAnswer(invocation->{
                Object proof=invocation.callRealMethod();
                read.countDown();awaitLatch(release);return proof;
            }).when(authority).lockPreparedAttempt(eq(fixture.attempt().jobId()),eq(fixture.attempt().attemptId()),any(),eq(true));
            try(var executor=Executors.newFixedThreadPool(2)) {
                var finalizer=executor.submit(()->rag.finalizeGeneration(fixture.attempt().jobId(),fixture.attempt().attemptId(),null));
                Future<?> writer=null;
                try {
                    assertThat(read.await(5,TimeUnit.SECONDS)).as(type+" "+action).isTrue();
                    writer=executor.submit(()->{
                        assertThatThrownBy(()->mutate(fixture,action))
                                .satisfies(error->assertThat(sqlState(error)).isEqualTo("P1701"));
                    });
                    writer.get(5,TimeUnit.SECONDS);
                    assertThat(writerCommitted.get()).isFalse();
                }finally{release.countDown();}
                assertThat(finalizer.get(10,TimeUnit.SECONDS).active()).isTrue();
                if(writer!=null)writer.get(10,TimeUnit.SECONDS);
                mutate(fixture,action);
                writerCommitted.set(true);
                assertThat(writerCommitted.get()).isTrue();
                reset(authority);
                assertThat(rag.finalizeGeneration(fixture.attempt().jobId(),fixture.attempt().attemptId(),null).active())
                        .as(type+" "+action).isFalse();
                recovery.reconcile();
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE source_id=? AND is_active",
                        Integer.class,fixture.source().id())).isZero();
            }finally{release.countDown();reset(authority);}
        }
    }
    @Test void writerFirstForEveryOwnerPathForcesFreshDenialAfterActualCommit() throws Exception {
        for(var type:List.of(RagSourceType.FICTION,RagSourceType.FILM))for(String action:actions(type)) {
            Fixture fixture=fixture(type);
            assertThatThrownBy(()->mutate(fixture,action))
                    .satisfies(error->assertThat(sqlState(error)).isEqualTo("P1701"));
            rag.cancelJob(fixture.attempt().jobId());
            var changed=new CountDownLatch(1);var release=new CountDownLatch(1);
            var writerPid=new AtomicInteger();
            try(var executor=Executors.newFixedThreadPool(2)){
                var writer=executor.submit(()->new TransactionTemplate(transactions).executeWithoutResult(status->{
                    writerPid.set(jdbcTemplate.queryForObject("SELECT pg_backend_pid()",Integer.class));
                    mutate(fixture,action);changed.countDown();awaitLatch(release);
                }));
                Future<?> finalizer=null;
                try{
                    assertThat(changed.await(5,TimeUnit.SECONDS)).isTrue();
                    finalizer=executor.submit(()->rag.finalizeGeneration(fixture.attempt().jobId(),fixture.attempt().attemptId(),null));
                    Future<?> completion=finalizer;
                    assertThatThrownBy(()->completion.get(5,TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                            .hasCauseInstanceOf(IllegalStateException.class);
                }finally{release.countDown();}
                writer.get(10,TimeUnit.SECONDS);
                Future<?> completion=finalizer;
                assertThatThrownBy(()->completion.get(10,TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class).hasCauseInstanceOf(IllegalStateException.class);
                assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE source_id=? AND is_active",
                        Integer.class,fixture.source().id())).isZero();
            }finally{release.countDown();}
        }
    }
    @Test void cachedSourceAndOwnerContextCannotReuseRemovedDescriptionConsent() throws Exception {
        Fixture fixture=fixture(RagSourceType.FICTION);
        assertThatThrownBy(()->RagTestCommands.approve(rag,fixture.source().id(),Set.of("title")))
                .satisfies(error->assertThat(sqlState(error)).isEqualTo("P1701"));
        rag.cancelJob(fixture.attempt().jobId());
        var stale=RagTestCommands.consent(rag.findSource(fixture.source().id()).orElseThrow(),Set.of("title","description"));
        new TransactionTemplate(transactions).executeWithoutResult(status->{
            entityManager.find(com.vhvkhangg.personalprivatevault.rag.internal.domain.RagSource.class,fixture.source().id());
            fiction.find(fixture.domain());
            try(var executor=Executors.newSingleThreadExecutor()){
                executor.submit(()->RagTestCommands.approve(rag, fixture.source().id(),Set.of("title","description","originalTitle"))).get(10,TimeUnit.SECONDS);
            }catch(Exception failure){throw new IllegalStateException(failure);}
            assertThatThrownBy(()->RagTestCommands.approve(rag, fixture.source().id(),Set.of("title")))
                    .isInstanceOf(org.springframework.transaction.IllegalTransactionStateException.class);
        });
        var reduced=RagTestCommands.approve(rag, fixture.source().id(),Set.of("title"));
        assertThat(reduced.authorizationEpoch()).isGreaterThan(fixture.source().authorizationEpoch()+1);
        assertThatThrownBy(()->rag.approveSource(fixture.source().id(),stale))
                .isInstanceOf(com.vhvkhangg.personalprivatevault.rag.api.RagSourceChangedException.class);
        assertThatThrownBy(()->rag.finalizeGeneration(fixture.attempt().jobId(),fixture.attempt().attemptId(),null)).isInstanceOf(IllegalStateException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.staged_outputs WHERE job_id=?",Integer.class,fixture.attempt().jobId())).isZero();
    }
    @Test void concurrentDisjointEdgesCannotCommitAWriteSkewCycle() throws Exception {
        var a=fixture(RagSourceType.FICTION,false);var b=fixture(RagSourceType.FICTION,false);
        var c=fixture(RagSourceType.FICTION,false);var d=fixture(RagSourceType.FICTION,false);
        rag.addDependency(b.source().id(),c.source().id());rag.addDependency(d.source().id(),a.source().id());
        var start=new CountDownLatch(1);var successes=new AtomicInteger();var rejections=new AtomicInteger();
        try(var executor=Executors.newFixedThreadPool(2)){
            List<Future<?>> tasks=new ArrayList<>();
            for(var edge:List.of(List.of(a.source().id(),b.source().id()),List.of(c.source().id(),d.source().id()))){
                tasks.add(executor.submit(()->{awaitLatch(start);try{rag.addDependency(edge.get(0),edge.get(1));successes.incrementAndGet();}
                    catch(IllegalStateException cycle){assertThat(cycle.getMessage()).contains("Cyclic");rejections.incrementAndGet();}}));
            }
            start.countDown();for(var task:tasks)task.get(10,TimeUnit.SECONDS);
            assertThat(successes.get()).isEqualTo(1);assertThat(rejections.get()).isEqualTo(1);
        }finally{start.countDown();}
    }
    @Test void rollbackAfterRealFinalizationDoesNotExposeActiveGeneration() {
        Fixture fixture=fixture(RagSourceType.FILM);
        new TransactionTemplate(transactions).executeWithoutResult(status->{
            assertThatThrownBy(()->rag.finalizeGeneration(fixture.attempt().jobId(),fixture.attempt().attemptId(),null))
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("ambient transaction");
            status.setRollbackOnly();
        });
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM rag.index_generations WHERE source_id=?",Integer.class,fixture.source().id())).isZero();
        assertThat(rag.finalizeGeneration(fixture.attempt().jobId(),fixture.attempt().attemptId(),null).active()).isTrue();
    }
    private static void awaitLatch(CountDownLatch latch) {
        try{if(!latch.await(8,TimeUnit.SECONDS))throw new IllegalStateException("Boundary latch timeout");}
        catch(InterruptedException failure){Thread.currentThread().interrupt();throw new IllegalStateException(failure);}
    }
    private static String sqlState(Throwable error) {
        while(error!=null) {
            if(error instanceof java.sql.SQLException sql)return sql.getSQLState();
            error=error.getCause();
        }
        throw new AssertionError("Expected actual PostgreSQL conflict");
    }
}
