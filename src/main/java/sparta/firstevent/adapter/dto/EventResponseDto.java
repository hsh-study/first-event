package sparta.firstevent.adapter.dto;

import sparta.firstevent.domain.event.EventStatus;

public record EventResponseDto(
    Long id, String title, EventStatus status
) {
}
