package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.application.ports.in.EventGetUseCase;
import sparta.firstevent.application.ports.in.MemberGetUseCase;
import sparta.firstevent.application.ports.in.ParticipantGetUseCase;
import sparta.firstevent.application.ports.in.ParticipantManageUseCase;
import sparta.firstevent.application.ports.out.EventParticipantCountRepository;
import sparta.firstevent.application.ports.out.ParticipantRepository;
import sparta.firstevent.domain.event.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipantCommandService implements ParticipantManageUseCase {

    private final EventGetUseCase eventGetUseCase;
    private final MemberGetUseCase memberGetUseCase;
    private final ParticipantGetUseCase participantGetUseCase;

    private final ParticipantRepository participantRepository;
    private final EventParticipantCountRepository eventParticipantCountRepository;

    private final Determinator determinator;


    @Override
    public Participant apply(Long eventId, Long memberId) {
        validateApply(eventId, memberId);

        Participant participant = participantRepository.save(Participant.regist(memberId, eventId, determinator));

        EventParticipantCount count = eventParticipantCountRepository.findByEventId(eventId)
            .orElse(EventParticipantCount.regist(eventId));
        count.update(count.getParticipantCount() + 1, participant.isWinner() ? count.getWinnerCount() + 1 : count.getWinnerCount(), LocalDateTime.now());
        eventParticipantCountRepository.save(count);

        return participant;
    }

    @Override
    public Participant directApply(Long eventId, Long memberId) {
        validateApply(eventId, memberId);

        Participant participant = participantRepository.save(Participant.regist(memberId, eventId, determinator));

        Optional<EventParticipantCount> count = eventParticipantCountRepository.findByEventId(eventId);
        if (count.isEmpty()) {
            eventParticipantCountRepository.save(EventParticipantCount.regist(eventId));
        }

        if(participant.isWinner()) {
            eventParticipantCountRepository.updateCountWithWinner(eventId);
        } else {
            eventParticipantCountRepository.updateCount(eventId);
        }

        return participant;
    }

    private void validateApply(Long eventId, Long memberId) {
        Event event = eventGetUseCase.get(eventId);
        memberGetUseCase.get(memberId);
        Long participantCount = participantGetUseCase.countWinner(eventId);

        if (!event.getStatus().equals(EventStatus.STARTED)) {
            throw new IllegalStateException("진행중인 이벤트가 아닙니다.");
        }

        if (participantCount >= event.getCapacity()) {
            throw new IllegalStateException("당첨자 수에 도달하여 이벤트가 종료되었습니다.");
        }

        if (participantGetUseCase.exists(eventId, memberId)) {
            throw new IllegalStateException("이벤트에 중복 참여할 수 없습니다.");
        }
    }
}
