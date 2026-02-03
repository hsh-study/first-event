package sparta.firstevent.application.ports.out;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import sparta.firstevent.domain.event.EventParticipantCount;

import java.util.Optional;

public interface EventParticipantCountRepository extends Repository<EventParticipantCount, Long> {
    Optional<EventParticipantCount> findByEventId(Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM EventParticipantCount c WHERE c.eventId = :eventId")
    Optional<EventParticipantCount> findByEventIdWithLock(@Param("eventId") Long eventId);

    EventParticipantCount save(EventParticipantCount count);

    @Modifying
    @Query("UPDATE EventParticipantCount c SET c.participantCount = c.participantCount + 1, c.winnerCount = c.winnerCount + 1 WHERE c.eventId = :eventId")
    void updateCountWithWinner(@Param("eventId") Long eventId);

    @Modifying
    @Query("UPDATE EventParticipantCount c SET c.participantCount = c.participantCount + 1 WHERE c.eventId = :eventId")
    void updateCount(@Param("eventId") Long eventId);
}
