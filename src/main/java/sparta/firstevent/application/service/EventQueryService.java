package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sparta.firstevent.application.ports.in.EventGetUseCase;
import sparta.firstevent.application.ports.in.EventViewManageUseCase;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventStatus;

@Service
@RequiredArgsConstructor
public class EventQueryService implements EventGetUseCase {
    private final EventRepository eventRepository;
    private final EventViewManageUseCase eventViewManageUseCase;

    @Override
    public Page<Event> getAll(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    @Override
    public Event get(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("id에 해당하는 이벤트가 없습니다."));
    }

    @Override
    public Event getWithViewCount(Long eventId, Long memberId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("id에 해당하는 이벤트가 없습니다."));
        eventViewManageUseCase.viewEvent(eventId, memberId);

        return event;
    }

    @Override
    public Event getWithStatus(Long id, EventStatus status) {
        return eventRepository.findByIdAndStatus(id, status)
            .orElseThrow(() -> new IllegalArgumentException("id에 해당하는 이벤트가 없습니다."));
    }
}
