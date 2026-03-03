package sparta.firstevent.adapter.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import sparta.firstevent.application.ports.out.EventViewCounterRepository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisEventViewCounterRepository implements EventViewCounterRepository {
    private final StringRedisTemplate redisTemplate;

    static final String LOCK_KEY_FORMAT = "lock::event::%s::member::%s";
    static final String COUNT_KEY_FORMAT = "view::count::event::%s";

    @Override
    public Long get(Long eventId) {
        String key = String.format(COUNT_KEY_FORMAT, eventId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return 0L;
        return Long.parseLong(value);
    }

    @Override
    public Long increment(Long eventId) {
        String key = String.format(COUNT_KEY_FORMAT, eventId);
        return redisTemplate.opsForValue().increment(key);
    }

    @Override
    public boolean lock(Long eventId, Long memberId, Duration duration) {
        String key = String.format(LOCK_KEY_FORMAT, eventId, memberId);
        return Boolean.TRUE.equals(
            redisTemplate.opsForValue().setIfAbsent(key, "locked", duration)
        );
    }

    @Override
    public void deleteAll() {
        redisTemplate.delete(redisTemplate.keys("*"));
    }
}
