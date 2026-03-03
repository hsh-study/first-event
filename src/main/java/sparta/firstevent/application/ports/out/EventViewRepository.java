package sparta.firstevent.application.ports.out;

import org.springframework.data.repository.Repository;
import sparta.firstevent.domain.event.EventView;

import java.util.Optional;

public interface EventViewRepository extends Repository<EventView, Long> {
    EventView save(EventView eventView);
    Optional<EventView> findByEventId(Long id);
}
