package sparta.firstevent.application.ports.out;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import sparta.firstevent.domain.event.EventParticipantCount;

import java.util.Optional;

public interface EventParticipantCountRepository extends Repository<EventParticipantCount, Long> {
    Optional<EventParticipantCount> findByEventId(Long eventId);

    EventParticipantCount save(EventParticipantCount count);

    @Modifying
    @Query("UPDATE EventParticipantCount c SET c.participantCount = c.participantCount + 1, c.winnerCount = c.winnerCount + 1 WHERE c.eventId = :eventId")
    void updateCountWithWinner(@Param("eventId") Long eventId);

    @Modifying
    @Query("UPDATE EventParticipantCount c SET c.participantCount = c.participantCount + 1 WHERE c.eventId = :eventId")
    void updateCount(@Param("eventId") Long eventId);
}
