package sparta.firstevent.domain.event;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Embeddable
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class EventPeriod {

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    private EventPeriod(LocalDateTime startAt, LocalDateTime endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static EventPeriod of(LocalDateTime startAt, LocalDateTime endAt) {
        Objects.requireNonNull(startAt);
        Objects.requireNonNull(endAt);

        if (startAt.isAfter(endAt)) {
            throw new IllegalArgumentException("시작일시는 종료일시 보다 늦을 수 없습니다.");
        }
        return new EventPeriod(startAt, endAt);
    }

    public void start() {
        this.startAt = LocalDateTime.now();
    }

    public void update(LocalDateTime startAt, LocalDateTime endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }
}
