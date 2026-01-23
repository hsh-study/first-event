package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.application.ports.in.EventGetUseCase;
import sparta.firstevent.application.ports.in.EventManageUseCase;
import sparta.firstevent.application.ports.in.MemberGetUseCase;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.domain.event.Determinator;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventStatus;
import sparta.firstevent.domain.event.Participant;
import sparta.firstevent.domain.member.Member;

@Service
@Transactional
@RequiredArgsConstructor
public class EventCommandService implements EventManageUseCase {
    private final EventRepository eventRepository;
    private final MemberGetUseCase memberGetUseCase;
    private final EventGetUseCase eventGetUseCase;

    private final Determinator determinator;

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
    public Participant apply(Long eventId, Long memberId) {
        Event event = eventGetUseCase.get(eventId);
        Member member = memberGetUseCase.get(memberId);

        event.participate(member, determinator);
        return null;
    }
}
