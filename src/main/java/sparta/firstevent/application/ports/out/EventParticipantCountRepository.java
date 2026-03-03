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
    @Query("SELECT e FROM EventParticipantCount e WHERE e.eventId = :eventId")
    Optional<EventParticipantCount> findByEventIdWithLock(@Param("eventId") Long eventId);

    EventParticipantCount save(EventParticipantCount participantCount);

    @Modifying
    @Query("""
        UPDATE EventParticipantCount e
        SET e.participantCount = e.participantCount + 1,
            e.winnerCount = e.winnerCount + 1
        WHERE e.eventId = :eventId
    """)
    int updateWithWinner(Long eventId);

    @Modifying
    @Query("""
        UPDATE EventParticipantCount e
        SET e.participantCount = e.participantCount + 1
        WHERE e.eventId = :eventId
    """)
    int update(Long eventId);
}
