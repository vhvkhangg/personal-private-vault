package com.vhvkhangg.personalprivatevault.authentication.fixture;

import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** Disposable DP-A data and application fixtures. Never connects to an Owner database. */
public final class ProfileAuthFixture {
    private ProfileAuthFixture() { }

    public static void seedDomain(JdbcTemplate jdbc) {
        jdbc.update("INSERT INTO currencies(code,name,symbol) VALUES ('USD','US Dollar','$') ON CONFLICT DO NOTHING");
        jdbc.update("INSERT INTO countries(code,name_en) VALUES ('VN','Vietnam') ON CONFLICT DO NOTHING");
        jdbc.update("INSERT INTO platforms(id,name,kind) VALUES (901,'DP-A synthetic platform','WEB') ON CONFLICT(id) DO NOTHING");
        jdbc.update("INSERT INTO vault_entries(id,entry_type) VALUES (901,'PERSON'),(902,'NOTE'),(903,'EXTERNAL_ACCOUNT')");
        jdbc.update("INSERT INTO persons(id,name,nationality_code,notes) VALUES (901,'Synthetic person','VN','preserve person values')");
        jdbc.update("INSERT INTO notes(id,title,content_markdown,frontmatter) VALUES (902,'Synthetic note','# Preserve content','{\"synthetic\":true}')");
        jdbc.update("INSERT INTO external_accounts(id,platform_id,ownership,account_type,username) VALUES (903,901,'OWNED','SOCIAL','synthetic-account')");
        jdbc.update("INSERT INTO wallets(id,name,type,currency_code,opening_balance) VALUES (901,'Synthetic wallet','CASH','USD',123.4567)");
        jdbc.update("INSERT INTO diary_entries(id,entry_date,title,content_markdown) VALUES (901,'2026-01-02','Synthetic diary','preserve diary content')");
        jdbc.update("INSERT INTO personal_profiles(id,name,relationship,is_self) VALUES (901,'Synthetic owner','SELF',true)");
        jdbc.update("INSERT INTO tags(id,name) VALUES (901,'synthetic-tag')");
        jdbc.update("INSERT INTO vault_entry_tags(vault_entry_id,tag_id) VALUES (902,901)");
        jdbc.update("INSERT INTO app_settings(id,default_currency_code,pagination_size) VALUES (1,'USD',37) ON CONFLICT(id) DO UPDATE SET pagination_size=37");
    }

    /** Canonical row values (including empty tables), not counts. SQL failures must fail the test. */
    public static Map<String, String> domainSnapshot(JdbcTemplate jdbc) {
        List<String> tables = jdbc.queryForList("""
                SELECT tablename FROM pg_tables WHERE schemaname='public'
                  AND tablename NOT LIKE 'ppv_%' AND tablename NOT LIKE '%rag%'
                  AND tablename NOT IN ('flyway_schema_history','refresh_tokens') ORDER BY tablename
                """, String.class);
        Map<String, String> result = new LinkedHashMap<>();
        for (String table : tables) {
            if (!table.matches("[a-z_]+")) throw new IllegalArgumentException("Unexpected test table name");
            result.put(table, jdbc.queryForObject("SELECT coalesce(jsonb_agg(v ORDER BY v::text),'[]'::jsonb)::text FROM (SELECT to_jsonb(t) v FROM \""
                    + table + "\" t) s", String.class));
        }
        return result;
    }

    public static ConfigurableApplicationContext restoredApplication(String url, String user, String password, String key) {
        return new SpringApplicationBuilder(PersonalPrivateVaultApplication.class).run(
                "--server.address=127.0.0.1", "--server.port=0",
                "--spring.datasource.url=" + url, "--spring.datasource.username=" + user,
                "--spring.datasource.password=" + password, "--spring.jpa.hibernate.ddl-auto=validate",
                "--spring.datasource.hikari.maximum-pool-size=3",
                "--ppv.security.jwt.secret=" + key,
                "--ppv.drive.enabled=false", "--ppv.drive.files.enabled=false",
                "--ppv.security.google-signin.enabled=false");
    }

    public static final class MutableClock extends Clock {
        private final AtomicReference<Instant> now = new AtomicReference<>(Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        public void set(Instant value) { now.set(value); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) {
            if (!ZoneOffset.UTC.equals(zone)) throw new IllegalArgumentException("UTC fixture only");
            return this;
        }
        @Override public Instant instant() { return now.get(); }
    }
}
