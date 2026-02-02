package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.application.ports.in.ParticipantGetUseCase;
import sparta.firstevent.application.ports.out.ParticipantRepository;
import sparta.firstevent.domain.event.Participant;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipantQueryService implements ParticipantGetUseCase {

    private final ParticipantRepository participantRepository;

    @Override
    public Long countWinner(Long eventId) {
        return participantRepository.countByEventIdAndIsWinnerIsTrue(eventId);
    }

    @Override
    public boolean exists(Long eventId, Long memberId) {
        return participantRepository.existsByEventIdAndMemberId(eventId, memberId);
    }

    @Override
    public Page<Participant> getAll(Long eventId, Pageable pageable) {

        return participantRepository.findAllBy(eventId, pageable);
    }
}
