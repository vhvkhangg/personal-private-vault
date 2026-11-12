package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security;

import com.vhvkhangg.personalprivatevault.authentication.internal.application.session.SessionAuthority;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.session.SessionAuthorityService;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionAuthorityFilter extends GenericFilterBean {

    private static final Logger log = LoggerFactory.getLogger(SessionAuthorityFilter.class);

    private final SessionAuthorityService sessionAuthorityService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final Clock clock;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            String sub = jwt.getSubject();
            String profileStr = jwt.getClaimAsString("profile");
            String sidStr = jwt.getClaimAsString("sid");
            Object genObj = jwt.getClaim("generation");

            if (!"1".equals(sub) || profileStr == null || sidStr == null || genObj == null || jwt.getExpiresAt() == null) {
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(httpRequest, httpResponse,
                        new InsufficientAuthenticationException("Invalid or incomplete JWT claims"));
                return;
            }

            LoginProfile profile;
            UUID sessionId;
            long generation;
            try {
                profile = LoginProfile.valueOf(profileStr);
                sessionId = UUID.fromString(sidStr);
                Long genVal = SecurityConfiguration.parseGeneration(genObj);
                if (genVal == null) {
                    throw new IllegalArgumentException("generation must be a positive 64-bit integer");
                }
                generation = genVal;
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(httpRequest, httpResponse,
                        new InsufficientAuthenticationException("Malformed JWT claims"));
                return;
            }

            Optional<SessionAuthority> authorityOpt;
            try {
                authorityOpt = sessionAuthorityService.findAuthoritySnapshot(sessionId);
            } catch (Exception ex) {
                log.error("Database authority lookup failure during admission: {}", ex.getClass().getSimpleName());
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(httpRequest, httpResponse,
                        new InsufficientAuthenticationException("Authority lookup failure"));
                return;
            }

            if (authorityOpt.isEmpty() || !authorityOpt.get().isValid(clock.instant(), profile, generation)) {
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(httpRequest, httpResponse,
                        new InsufficientAuthenticationException("Session is invalid, revoked, expired, or epoch mismatch"));
                return;
            }

            SessionAuthority authority = authorityOpt.get();
            var authorities = List.of(new SimpleGrantedAuthority("PROFILE_" + profile.name()));
            JwtAuthenticationToken updatedAuth = new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
            SecurityContextHolder.getContext().setAuthentication(updatedAuth);
            httpRequest.setAttribute("PPV_SESSION_AUTHORITY", authority);
        }

        chain.doFilter(request, response);
    }
}
