package sparta.firstevent.application.ports.in;

import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.Participant;

public interface EventManageUseCase {
    Event regist(EventRequestDto dto);

    Event update(Long id, EventRequestDto eventRequestDto);

    void delete(Long id);

    Participant apply(Long eventId, Long memberId);
}
