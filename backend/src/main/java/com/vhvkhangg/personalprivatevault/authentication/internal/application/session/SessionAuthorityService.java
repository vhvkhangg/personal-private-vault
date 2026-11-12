package com.vhvkhangg.personalprivatevault.authentication.internal.application.session;

import com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionAuthorityService {

    private static final String SNAPSHOT_QUERY = """
            SELECT s.id, s.owner_id, s.active_profile, s.generation, s.public_credential_epoch,
                   s.expires_at, s.revoked_at, c.credential_epoch
            FROM ppv_login_sessions s
            JOIN ppv_public_login_credential c ON c.owner_id = s.owner_id
            WHERE s.id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public Optional<SessionAuthority> findAuthoritySnapshot(UUID sessionId) {
        if (sessionId == null) {
            return Optional.empty();
        }
        List<SessionAuthority> results = jdbcTemplate.query(
                SNAPSHOT_QUERY,
                (rs, rowNum) -> mapRow(rs),
                sessionId
        );
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    private SessionAuthority mapRow(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        short ownerId = rs.getShort("owner_id");
        String profileStr = rs.getString("active_profile");
        LoginProfile profile = LoginProfile.valueOf(profileStr);
        long generation = rs.getLong("generation");
        Long publicEpoch = (Long) rs.getObject("public_credential_epoch");
        Timestamp expTs = rs.getTimestamp("expires_at");
        Instant expiresAt = expTs != null ? expTs.toInstant() : Instant.MIN;
        Timestamp revTs = rs.getTimestamp("revoked_at");
        Instant revokedAt = revTs != null ? revTs.toInstant() : null;
        long currentCredentialEpoch = rs.getLong("credential_epoch");

        return new SessionAuthority(
                id,
                ownerId,
                profile,
                generation,
                publicEpoch,
                expiresAt,
                revokedAt,
                currentCredentialEpoch
        );
    }
}
