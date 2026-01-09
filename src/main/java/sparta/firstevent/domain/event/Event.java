package sparta.firstevent.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Event {
    private String title;
    private String description;
    private Integer capacity;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private EventStatus status;

    public Event(String title, String description, Integer capacity,
                 LocalDateTime startAt, LocalDateTime endAt) {
        this.title = title;
        this.description = description;
        this.capacity = capacity;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = EventStatus.PENDING;
    }

}
