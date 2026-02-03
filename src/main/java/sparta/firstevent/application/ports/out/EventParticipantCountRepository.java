package sparta.firstevent.application.ports.out;

import org.springframework.data.repository.Repository;
import sparta.firstevent.domain.event.EventParticipantCount;

import java.util.Optional;

public interface EventParticipantCountRepository extends Repository<EventParticipantCount, Long> {
    Optional<EventParticipantCount> findByEventId(Long eventId);

    EventParticipantCount save(EventParticipantCount participantCount);
}
