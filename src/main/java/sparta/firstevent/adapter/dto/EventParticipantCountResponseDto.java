package sparta.firstevent.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import sparta.firstevent.domain.event.EventStatus;

public record EventParticipantCountResponseDto(
    @Schema(description = "이벤트 아이디", example = "1")
    Long id,
    @Schema(description = "이벤트 제목", example = "선착순 당첨자 100명 커피쿠폰")
    String title,
    @Schema(description = "당첨자 정원", example = "100")
    Integer capacity,
    @Schema(description = "이벤트 상태", example = "PENDING: 대기중, STARTED: 진행중, FINISHED: 종료")
    EventStatus status,
    @Schema(description = "이벤트 참여 수", example = "1")
    long participantCount,
    @Schema(description = "이벤트 낙첨 수", example = "1")
    long winnerCount
) {
}
