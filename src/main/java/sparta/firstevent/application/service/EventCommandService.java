package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.application.ports.in.EventGetUseCase;
import sparta.firstevent.application.ports.in.EventManageUseCase;
import sparta.firstevent.application.ports.in.MemberGetUseCase;
import sparta.firstevent.domain.event.Determinator;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.Participant;
import sparta.firstevent.domain.member.Member;

@Service
@Transactional
@RequiredArgsConstructor
public class EventCommandService implements EventManageUseCase {
    private final MemberGetUseCase memberGetUseCase;
    private final EventGetUseCase eventGetUseCase;

    private final Determinator determinator;

    @Override
    public Participant apply(Long eventId, Long memberId) {
        Event event = eventGetUseCase.get(eventId);
        Member member = memberGetUseCase.get(memberId);

        event.participate(member, determinator);
        return null;
    }
}
