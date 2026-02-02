package sparta.firstevent.adapter.dto;

import sparta.firstevent.domain.event.Participant;

import java.time.LocalDateTime;

public record ParticipantResponseDto (
    Long id, Long memberId, boolean isWinner, LocalDateTime participateAt
) {

    public static ParticipantResponseDto from(Participant participant) {
        return new ParticipantResponseDto(participant.getId(), participant.getMemberId(), participant.isWinner(), participant.getParticipateAt());
    }
}
