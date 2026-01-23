package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sparta.firstevent.application.ports.in.AdminEventGetUseCase;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.Participant;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEventQueryService implements AdminEventGetUseCase {
    private final EventRepository eventRepository;

    @Override
    public Page<Event> getAll(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    @Override
    public Event get(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("id에 해당하는 이벤트가 없습니다."));
    }

    @Override
    public List<Participant> getParticipants(Long eventId) {
//        Event event = eventRepository.findById(eventId)
//            .orElseThrow(() -> new IllegalArgumentException("id에 해당하는 이벤트가 존재하지 않습니다."));
//        return event.getParticipants();

        return eventRepository.findAllParticipants(eventId);
    }

    @Override
    public List<Participant> getWinners(Long eventId) {
        return List.of();
    }
}
