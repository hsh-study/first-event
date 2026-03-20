package sparta.firstevent.adapter.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventParticipationMessage {

    private Long eventId;
    private Long memberId;

    private LocalDateTime timestamp;

    public static EventParticipationMessage of(Long eventId, Long memberId) {
        EventParticipationMessage message = new EventParticipationMessage();
        message.eventId = eventId;
        message.memberId = memberId;
        message.timestamp = LocalDateTime.now();
        return message;
    }
}
