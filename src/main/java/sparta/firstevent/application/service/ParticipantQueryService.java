package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.CursorPage;
import sparta.firstevent.application.ports.in.ParticipantGetUseCase;
import sparta.firstevent.application.ports.out.EventParticipantCountRepository;
import sparta.firstevent.application.ports.out.projections.ParticipantProjection;
import sparta.firstevent.application.ports.out.ParticipantRepository;
import sparta.firstevent.domain.event.EventParticipantCount;
import sparta.firstevent.domain.event.Participant;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipantQueryService implements ParticipantGetUseCase {

    private final ParticipantRepository participantRepository;
    private final EventParticipantCountRepository eventParticipantCountRepository;

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
        List<ParticipantProjection> projections = participantRepository.findAllByEventId(eventId, pageable);
        
        List<Long> ids = projections.stream()
                .map(ParticipantProjection::getId)
                .toList();
        
        List<Participant> participants = participantRepository.findAllByIdIn(ids);
        
        Map<Long, Participant> participantMap = participants.stream()
                .collect(Collectors.toMap(Participant::getId, p -> p));
        
        List<Participant> orderedParticipants = ids.stream()
                .map(participantMap::get)
                .toList();

        EventParticipantCount participantCount = eventParticipantCountRepository.findByEventId(eventId)
            .orElseThrow(() -> new IllegalArgumentException("이벤트에 해당하는 참여자 수 정보가 없습니다."));
        
        return new PageImpl<>(orderedParticipants, pageable, participantCount.getParticipantCount());
    }

    @Override
    public CursorPage<Participant> getAllByCursor(Long eventId, Long cursor, int size) {

        List<Participant> participants = null;

        Pageable pageable = Pageable.ofSize(size + 1);

        if (cursor == null) {
            participants = participantRepository.findFirstByEventId(eventId, pageable);
        } else {
            participants = participantRepository.findAllByEventIdWithCursor(eventId, cursor, pageable);
        }

        Long lastCursor = null;
        boolean isLast = false;

        if (participants.size() > size) {
            lastCursor = participants.get(size - 1).getId();
        } else {
            isLast = true;
        }

        EventParticipantCount participantCount = eventParticipantCountRepository.findByEventId(eventId)
            .orElseThrow(() -> new IllegalArgumentException("이벤트에 해당하는 참여자 수 정보가 없습니다."));

        return new CursorPage<>(participants.subList(0, size), lastCursor, participantCount.getParticipantCount(), isLast);
    }
}
