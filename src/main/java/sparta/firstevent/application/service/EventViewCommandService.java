package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sparta.firstevent.application.ports.in.EventViewManageUseCase;
import sparta.firstevent.application.ports.out.EventViewCounterRepository;
import sparta.firstevent.application.ports.out.EventViewRepository;
import sparta.firstevent.domain.event.EventView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventViewCommandService implements EventViewManageUseCase {

    private final EventViewRepository eventViewRepository;
    private final EventViewCounterRepository eventViewCounterRepository;

    @Override
    public Long viewEvent(Long eventId, Long memberId) {

        boolean isLocked = eventViewCounterRepository.lock(eventId, memberId, Duration.ofMinutes(10));
        if (!isLocked) {
            return eventViewCounterRepository.get(eventId);
        }

        Long viewCount = eventViewCounterRepository.increment(eventId);

        if (viewCount % 100 == 0) {
            Optional<EventView> eventView = eventViewRepository.findByEventId(eventId);

            if (eventView.isPresent()) {
                eventViewRepository.save(eventView.get().update(viewCount, LocalDateTime.now()));
            } else {
                eventViewRepository.save(EventView.regist(eventId, viewCount, LocalDateTime.now()));
            }
        }

        return viewCount;
    }
}
