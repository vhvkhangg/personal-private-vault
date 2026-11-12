package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        DelegatingPasswordEncoder delegatingPasswordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return delegatingPasswordEncoder;
    }

    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        return properties.toSecretKey();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey, JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefaultWithIssuer(properties.getIssuer());
        OAuth2TokenValidator<Jwt> claimValidator = jwt -> {
            List<OAuth2Error> errors = new ArrayList<>();
            if (!"1".equals(jwt.getSubject())) {
                errors.add(new OAuth2Error("invalid_token", "Subject must be 1", null));
            }
            String profile = jwt.getClaimAsString("profile");
            if (!"PUBLIC".equals(profile) && !"PRIVATE".equals(profile)) {
                errors.add(new OAuth2Error("invalid_token", "profile claim must be PUBLIC or PRIVATE", null));
            }
            String sid = jwt.getClaimAsString("sid");
            if (sid == null) {
                errors.add(new OAuth2Error("invalid_token", "sid claim is required", null));
            } else {
                try {
                    UUID.fromString(sid);
                } catch (Exception e) {
                    errors.add(new OAuth2Error("invalid_token", "sid claim must be valid UUID", null));
                }
            }
            Object gen = jwt.getClaim("generation");
            if (jwt.getExpiresAt() == null) {
                errors.add(new OAuth2Error("invalid_token", "exp claim is required", null));
            }
            if (!isValidGeneration(gen) || hasFloatingOrMalformedWireGeneration(jwt)) {
                errors.add(new OAuth2Error("invalid_token", "generation claim must be positive 64-bit integer", null));
            }
            if (jwt.getId() == null || jwt.getId().isBlank()) {
                errors.add(new OAuth2Error("invalid_token", "jti claim is required", null));
            }
            if (errors.isEmpty()) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(errors);
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator, claimValidator));
        return decoder;
    }

    private static boolean hasFloatingOrMalformedWireGeneration(Jwt jwt) {
        try {
            String tokenValue = jwt.getTokenValue();
            if (tokenValue == null) {
                return false;
            }
            String[] parts = tokenValue.split("\\.");
            if (parts.length < 2) {
                return false;
            }
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String json = new String(decoded, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json).get("generation");
            if (node == null) {
                return true;
            }
            if (!node.isIntegralNumber() || !node.canConvertToLong()) {
                return true;
            }
            return node.asLong() <= 0;
        } catch (Exception e) {
            return true;
        }
    }

    public static Long parseGeneration(Object genObj) {
        if (genObj == null) {
            return null;
        }
        if (genObj instanceof Double || genObj instanceof Float) {
            return null;
        }
        if (genObj instanceof Long l) {
            return l > 0 ? l : null;
        }
        if (genObj instanceof Integer i) {
            return i > 0 ? (long) i : null;
        }
        if (genObj instanceof Short s) {
            return s > 0 ? (long) s : null;
        }
        if (genObj instanceof Byte b) {
            return b > 0 ? (long) b : null;
        }
        if (genObj instanceof BigInteger bi) {
            if (bi.compareTo(BigInteger.ZERO) > 0 && bi.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
                return bi.longValue();
            }
            return null;
        }
        if (genObj instanceof BigDecimal bd) {
            try {
                if (bd.scale() == 0 && bd.compareTo(BigDecimal.ONE) >= 0 && bd.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) <= 0) {
                    return bd.longValueExact();
                }
            } catch (Exception ignored) {}
            return null;
        }
        return null;
    }

    public static boolean isValidGeneration(Object genObj) {
        return parseGeneration(genObj) != null;
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(secretKey);
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public org.springframework.security.web.AuthenticationEntryPoint authenticationEntryPoint() {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .findAndRegisterModules();
        return (request, response, authException) -> {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
            com.vhvkhangg.personalprivatevault.ApiResponse<Void> body =
                    com.vhvkhangg.personalprivatevault.ApiResponses.error("AUTHENTICATION_REQUIRED", "Full authentication is required to access this resource");
            objectMapper.writeValue(response.getWriter(), body);
        };
    }

    @Bean
    public org.springframework.security.web.access.AccessDeniedHandler accessDeniedHandler() {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .findAndRegisterModules();
        return (request, response, accessDeniedException) -> {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
            com.vhvkhangg.personalprivatevault.ApiResponse<Void> body =
                    com.vhvkhangg.personalprivatevault.ApiResponses.error("ACCESS_DENIED", "Access is denied");
            objectMapper.writeValue(response.getWriter(), body);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SessionAuthorityFilter sessionAuthorityFilter,
            org.springframework.security.web.AuthenticationEntryPoint entryPoint,
            org.springframework.security.web.access.AccessDeniedHandler accessDeniedHandler,
            @org.springframework.beans.factory.annotation.Value("${ppv.drive.enabled:false}") boolean driveEnabled,
            @org.springframework.beans.factory.annotation.Value("${ppv.security.google-signin.enabled:false}") boolean googleSignInEnabled) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/auth/bootstrap/status").permitAll()
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/bootstrap").permitAll()
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/login").permitAll()
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/profiles/login").permitAll()
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/revoke").permitAll();
                    if (driveEnabled) {
                        auth.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/drive/oauth/callback").permitAll();
                    }
                    if (googleSignInEnabled) {
                        auth
                                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/google/start").permitAll()
                                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/auth/google/callback").permitAll()
                                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/google/handoff").permitAll()
                                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/google/link/start").hasAuthority("PROFILE_PRIVATE");
                    }
                    auth
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/sessions/elevate-private").hasAuthority("PROFILE_PUBLIC")
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/sessions/downgrade-public").hasAuthority("PROFILE_PRIVATE")
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/profiles/public-credential").hasAuthority("PROFILE_PRIVATE")
                            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/private-pin/verify").hasAuthority("PROFILE_PRIVATE")
                            .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/auth/private-pin").hasAuthority("PROFILE_PRIVATE")
                            .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ASYNC, jakarta.servlet.DispatcherType.ERROR).hasAuthority("PROFILE_PRIVATE")
                            .anyRequest().hasAuthority("PROFILE_PRIVATE");
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterAfter(sessionAuthorityFilter, org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );
        return http.build();
    }
}
