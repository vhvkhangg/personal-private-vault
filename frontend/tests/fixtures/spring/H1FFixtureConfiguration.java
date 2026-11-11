package com.vhvkhangg.personalprivatevault.fixture;

import com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication;
import com.vhvkhangg.personalprivatevault.authentication.fixture.FakeGoogleSignInServer;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.oauth.GoogleSignInProviderClient;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInProperties;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInResources;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleDriveServer;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleOAuthServer;
import com.vhvkhangg.personalprivatevault.drive.fixture.FakeGoogleThumbnailServer;
import com.vhvkhangg.personalprivatevault.drive.internal.files.application.DriveFileAuthority;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileHttpClient;
import com.vhvkhangg.personalprivatevault.drive.internal.files.http.DriveFileResources;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.DriveOAuthClient;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.net.URI;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration(proxyBeanMethods = false)
@Import(PersonalPrivateVaultApplication.class)
public class H1FFixtureConfiguration {

    public static volatile FakeGoogleSignInServer fakeGoogle;
    public static volatile FakeGoogleOAuthServer fakeOAuth;
    public static volatile FakeGoogleDriveServer fakeDrive;
    public static volatile FakeGoogleThumbnailServer fakeThumbnail;
    public static volatile Path spoolDir;

    public static volatile CountDownLatch refreshPauseLatch;
    public static volatile CountDownLatch refreshEnteredLatch;
    public static volatile CountDownLatch handoffPauseLatch;
    public static volatile CountDownLatch handoffEnteredLatch;
    public static volatile long clockOffsetSeconds;
    public static volatile boolean failRevoke;
    public static final AtomicInteger refreshRequests = new AtomicInteger();
    public static final AtomicInteger springRequests = new AtomicInteger();
    public static volatile String heldResponsePath;
    public static volatile CountDownLatch responseEntered;
    public static volatile CountDownLatch responseRelease;

    @Bean
    @Primary
    public Clock fixtureClock() {
        return new Clock() {
            @Override public ZoneId getZone() { return ZoneOffset.UTC; }
            @Override public Clock withZone(ZoneId zone) {
                if (!ZoneOffset.UTC.equals(zone)) throw new IllegalArgumentException("UTC only");
                return this;
            }
            @Override public Instant instant() { return Instant.now().plusSeconds(clockOffsetSeconds); }
        };
    }

    @Bean
    @Primary
    public GoogleSignInProviderClient testGoogleSignInProviderClient(
            GoogleSignInProperties properties,
            Clock clock,
            GoogleSignInResources resources
    ) {
        return new GoogleSignInProviderClient(
                properties,
                clock,
                URI.create(fakeGoogle.getTokenUri()),
                URI.create(fakeGoogle.getJwksUri()),
                resources
        );
    }

    @Bean
    @Primary
    public DriveOAuthClient testDriveOAuthClient(Clock clock) {
        return new DriveOAuthClient(
                "s2-http-client",
                "synthetic-secret",
                fakeOAuth.getTokenEndpoint(),
                fakeOAuth.getRevokeEndpoint(),
                fakeOAuth.getJwksUri(),
                clock,
                null
        );
    }

    @Bean
    @Primary
    public DriveFileHttpClient testDriveFileHttpClient(DriveFileAuthority authority) {
        return new DriveFileHttpClient(authority, fakeDrive.origin(), fakeThumbnail.origin());
    }

    @Bean
    @Primary
    public DriveFileResources testDriveFileResources() {
        return new DriveFileResources(
                spoolDir,
                new DriveFileResources.Limits(
                        Duration.ofSeconds(4),
                        Duration.ofSeconds(15),
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(22)
                )
        );
    }

    @Bean
    public FilterRegistrationBean<Filter> testPauseFilter() {
        Filter filter = (request, response, chain) -> {
            HttpServletRequest req = (HttpServletRequest) request;
            String uri = req.getRequestURI();
            springRequests.incrementAndGet();
            if ("/api/v1/auth/revoke".equals(uri) && failRevoke) {
                ((HttpServletResponse) response).setStatus(503);
                return;
            }
            if ("/api/v1/auth/refresh".equals(uri)) refreshRequests.incrementAndGet();
            if ("/api/v1/auth/refresh".equals(uri) && refreshPauseLatch != null) {
                if (refreshEnteredLatch != null) {
                    refreshEnteredLatch.countDown();
                }
                try {
                    refreshPauseLatch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else if ("/api/v1/auth/google/handoff".equals(uri) && handoffPauseLatch != null) {
                if (handoffEnteredLatch != null) {
                    handoffEnteredLatch.countDown();
                }
                try {
                    handoffPauseLatch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (uri.equals(heldResponsePath)) {
                ContentCachingResponseWrapper cached = new ContentCachingResponseWrapper((HttpServletResponse) response);
                chain.doFilter(request, cached);
                if (cached.getContentSize() > 1024 * 1024) throw new IllegalStateException("Test response hold exceeded bound");
                responseEntered.countDown();
                try { responseRelease.await(10, TimeUnit.SECONDS); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                cached.copyBodyToResponse();
            } else {
                chain.doFilter(request, response);
            }
        };
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(-100);
        reg.addUrlPatterns("/api/v1/*");
        return reg;
    }
}
