package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.application.ports.in.AdminEventGetUseCase;
import sparta.firstevent.application.ports.in.AdminEventManageUseCase;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventStatus;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminEventCommandService implements AdminEventManageUseCase {
    private final AdminEventGetUseCase eventGetUseCase;
    private final EventRepository eventRepository;

    @Override
    public Event terminate(Long id) {
        Event event = eventGetUseCase.get(id);
        if (event.getStatus() == EventStatus.FINISHED) {
            throw new IllegalStateException("종료된 이벤트는 강제 종료할 수 없습니다.");
        }
        event.finish();
        return event;
    }

    @Override
    public Event regist(EventRequestDto requestDto) {
        return eventRepository.save(Event.regist(requestDto));
    }

    @Override
    public Event update(Long id, EventRequestDto eventRequestDto) {
        Event event = eventGetUseCase.get(id);
        event.update(eventRequestDto);
        return event;
    }

    @Override
    public void delete(Long id) {
        Event event = eventGetUseCase.get(id);

        if (event.getStatus() == EventStatus.STARTED) {
            throw new IllegalStateException("진행중인 이벤트는 삭제할 수 없습니다.");
        }

        eventRepository.delete(event);
    }

    @Override
    public void start(Long id) {
        Event event = eventGetUseCase.get(id);
        event.start();
    }
}
