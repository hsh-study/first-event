package sparta.firstevent.domain.event;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class EventView {

    @Id
    private Long eventId;
    private long viewCount;
    private LocalDateTime updatedAt;

    public static EventView regist(Long eventId, Long viewCount, LocalDateTime now) {
        EventView eventView = new EventView();
        eventView.eventId = eventId;
        eventView.viewCount = viewCount;
        eventView.updatedAt = now;
        return eventView;
    }

    public EventView update(Long count, LocalDateTime now) {
        this.viewCount = viewCount + count;
        this.updatedAt = now;
        return this;
    }
}
