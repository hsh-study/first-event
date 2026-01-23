package sparta.firstevent.application.ports.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.Participant;

import java.util.List;

public interface AdminEventGetUseCase {
    Page<Event> getAll(Pageable pageable);

    Event get(Long id);

    List<Participant> getParticipants(Long eventId);

    List<Participant> getWinners(Long eventId);
}
