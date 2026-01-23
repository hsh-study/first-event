package sparta.firstevent.application.ports.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventStatus;

import java.util.List;

public interface EventGetUseCase {
    Page<Event> getAll(Pageable pageable);

    Event get(Long id);

    Event getWithStatus(Long id, EventStatus status);
}
