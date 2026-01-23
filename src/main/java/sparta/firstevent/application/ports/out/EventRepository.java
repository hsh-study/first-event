package sparta.firstevent.application.ports.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventStatus;
import sparta.firstevent.domain.event.Participant;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends Repository<Event, Long> {
    Event save(Event event);

    Optional<Event> findById(Long id);

    Optional<Event> findByIdAndStatus(Long id, EventStatus status);

    Page<Event> findAll(Pageable pageable);

    void delete(Event event);

    @Query("SELECT p FROM Event e join e.participants p WHERE e.id = :eventId")
    List<Participant> findAllParticipants(@Param("eventId") Long eventId);

}
