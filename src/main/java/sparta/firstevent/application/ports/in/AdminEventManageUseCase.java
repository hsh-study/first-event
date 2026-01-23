package sparta.firstevent.application.ports.in;

import sparta.firstevent.domain.event.Event;

public interface AdminEventManageUseCase {

    Event terminate(Long id);
}
