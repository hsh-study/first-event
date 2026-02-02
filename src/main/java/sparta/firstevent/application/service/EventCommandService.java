package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.application.ports.in.EventGetUseCase;
import sparta.firstevent.application.ports.in.EventManageUseCase;
import sparta.firstevent.application.ports.in.MemberGetUseCase;
import sparta.firstevent.domain.event.Determinator;

@Service
@Transactional
@RequiredArgsConstructor
public class EventCommandService implements EventManageUseCase {
    private final MemberGetUseCase memberGetUseCase;
    private final EventGetUseCase eventGetUseCase;

    private final Determinator determinator;

}
