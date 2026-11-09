package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.authentication.internal.domain.GoogleHandoffTicket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface GoogleHandoffTicketRepository extends JpaRepository<GoogleHandoffTicket, String> {

    Optional<GoogleHandoffTicket> findByTicketHash(String ticketHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM GoogleHandoffTicket t WHERE t.ticketHash = :ticketHash")
    Optional<GoogleHandoffTicket> findByTicketHashForUpdate(@Param("ticketHash") String ticketHash);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE GoogleHandoffTicket t SET t.status = 'CONSUMED', t.consumedAt = :now WHERE t.ticketHash = :ticketHash AND t.status = 'PENDING' AND t.expiresAt > :now")
    int consumePendingTicket(@Param("ticketHash") String ticketHash, @Param("now") Instant now);

    @Query("SELECT COUNT(t) FROM GoogleHandoffTicket t WHERE t.status = 'PENDING' AND t.expiresAt > :now")
    long countActivePendingTickets(@Param("now") Instant now);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "UPDATE ppv_google_handoff_tickets SET status = 'EXPIRED' " +
            "WHERE status = 'PENDING' AND expires_at <= :now", nativeQuery = true)
    int expireOldTickets(@Param("now") Instant now);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "DELETE FROM ppv_google_handoff_tickets WHERE ticket_hash IN (" +
            "SELECT ticket_hash FROM ppv_google_handoff_tickets " +
            "WHERE status IN ('CONSUMED', 'EXPIRED') " +
            "AND expires_at < :cutoff " +
            "LIMIT :limit)", nativeQuery = true)
    int pruneTerminalTickets(@Param("cutoff") Instant cutoff, @Param("limit") int limit);
}
