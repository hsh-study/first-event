package sparta.firstevent.domain.event;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class EventParticipantCount {

    @Id
    private Long eventId;

    private long participantCount;

    private int winnerCount;

    private LocalDateTime updatedAt;

    public static EventParticipantCount regist(Long eventId) {
        EventParticipantCount eventParticipantCount = new EventParticipantCount();
        eventParticipantCount.eventId = eventId;
        return eventParticipantCount;
    }

    public void update(long participantCount, int winnerCount, LocalDateTime updatedAt) {
        this.participantCount = participantCount;
        this.winnerCount = winnerCount;
        this.updatedAt = updatedAt;
    }
}
