package sparta.firstevent.application.ports.out;

import java.time.Duration;

public interface EventViewCounterRepository {
    Long get(Long eventId);
    Long increment(Long eventId);

    // lock 을 정상적으로 획득하면 한번도 해당 이벤트를 조회하지 않은 사람
    boolean lock(Long eventId, Long memberId, Duration duration);

    void deleteAll();
}
