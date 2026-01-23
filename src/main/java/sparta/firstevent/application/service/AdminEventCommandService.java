package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sparta.firstevent.application.ports.in.AdminEventGetUseCase;
import sparta.firstevent.application.ports.in.AdminEventManageUseCase;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventStatus;

@Service
@RequiredArgsConstructor
public class AdminEventCommandService implements AdminEventManageUseCase {
    private final AdminEventGetUseCase eventGetUseCase;

    @Override
    public Event terminate(Long id) {
        Event event = eventGetUseCase.get(id);
        if (event.getStatus() == EventStatus.FINISHED) {
            throw new IllegalStateException("종료된 이벤트는 강제 종료할 수 없습니다.");
        }
        event.finish();
        return event;
    }
}
