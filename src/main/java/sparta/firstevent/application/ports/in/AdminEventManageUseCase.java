package sparta.firstevent.application.ports.in;

import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.domain.event.Event;

public interface AdminEventManageUseCase {

    Event terminate(Long id);

    Event regist(EventRequestDto dto);

    Event update(Long id, EventRequestDto eventRequestDto);

    void delete(Long id);

    void start(Long id);
}
