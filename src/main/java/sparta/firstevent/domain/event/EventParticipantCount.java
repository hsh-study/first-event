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

    private long winnerCount;

    private LocalDateTime updatedAt;

    public static EventParticipantCount regist(Long eventId) {
        EventParticipantCount participantCount = new EventParticipantCount();
        participantCount.eventId = eventId;
        participantCount.participantCount = 0;
        participantCount.winnerCount = 0;
        participantCount.updatedAt = LocalDateTime.now();

        return participantCount;
    }

    public void updateWithWinner() {
        this.winnerCount++;
        this.participantCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void update() {
        this.participantCount++;
        this.updatedAt = LocalDateTime.now();
        System.out.println("event participant count update");
    }
}
