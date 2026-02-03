package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.ParticipantCursorResponseDto;
import sparta.firstevent.adapter.dto.ParticipantResponseDto;
import sparta.firstevent.application.ports.in.ParticipantGetUseCase;
import sparta.firstevent.application.ports.out.EventParticipantCountRepository;
import sparta.firstevent.application.ports.out.ParticipantRepository;
import sparta.firstevent.domain.event.EventParticipantCount;
import sparta.firstevent.domain.event.Participant;

import java.util.List;

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

        return participantRepository.findAllByEventId(eventId, pageable);
    }

    @Override
    public ParticipantCursorResponseDto getAllByCursor(Long eventId, Long cursor, int size) {
        // size + 1 개를 조회하여 다음 페이지 존재 여부 확인
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        List<Participant> participants;
        if (cursor == null) {
            // 첫 페이지 조회
            participants = participantRepository.findByEventIdOrderByIdDesc(eventId, pageRequest);
        } else {
            // 커서 이후 페이지 조회
            participants = participantRepository.findByEventIdAndIdLessThanOrderByIdDesc(eventId, cursor, pageRequest);
        }

        // 다음 페이지 존재 여부 확인
        boolean hasNext = participants.size() > size;

        // 토탈 count
        EventParticipantCount eventParticipantCount = eventParticipantCountRepository.findByEventId(eventId)
            .orElseThrow(() -> new IllegalArgumentException("id에 해당하는 이벤트 참여 카운트 정보가 없습니다."));

        // 실제 반환할 데이터는 size 만큼만
        List<ParticipantResponseDto> contents = participants.stream()
                .limit(size)
                .map(ParticipantResponseDto::from)
                .toList();

        // nextCursor는 마지막 항목의 ID
        Long nextCursor = hasNext && !contents.isEmpty()
                ? contents.get(contents.size() - 1).id()
                : null;

        return ParticipantCursorResponseDto.of(contents, eventParticipantCount.getParticipantCount(), nextCursor);
    }
}
