package sparta.firstevent.adapter.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingMessageRetryScheduler {

    private static final String EVENT_PARTICIPATION_KEY = "event:participation";
    private static final String CONSUMBER_GROUP_NAME = "participation-processor";
    private static final Duration IDEL_TIME = Duration.ofMinutes(5);
    private static final int MAX_RETRY_COUNT = 3;

    private final RedisTemplate<String, String> streamTemplate;

    @Scheduled(fixedDelay = 60000)
    public void retryPendingMessages() {
        try {
            PendingMessages pendingMessage = streamTemplate.opsForStream()
                .pending(EVENT_PARTICIPATION_KEY, CONSUMBER_GROUP_NAME,
                    Range.unbounded(), 100L);

            if (pendingMessage.isEmpty()) return;

            for (PendingMessage e : pendingMessage) {
                if (e.getElapsedTimeSinceLastDelivery().compareTo(IDEL_TIME) < 0) {
                    continue;
                }

                if (e.getTotalDeliveryCount() >= MAX_RETRY_COUNT) {
                    log.warn("Message exceeded retry count, moving to DLQ: {}", e.getId());

                    moveToDLQ(e);
                    acknowledge(e.getId().toString());

                    continue;
                }

                log.info("Retry pending message: id={}, deliveryCount={}", e.getId(), e.getTotalDeliveryCount());

                streamTemplate.opsForStream().claim(
                    EVENT_PARTICIPATION_KEY,
                    CONSUMBER_GROUP_NAME,
                    "retry-consumer",
                    Duration.ofMillis(0),
                    e.getId());
            }

        } catch (Exception e) {
            log.error("Failed to retry pending messages", e);
        }
    }

    private void moveToDLQ(PendingMessage e) {
        streamTemplate.opsForStream().add(
            EVENT_PARTICIPATION_KEY +":dlq",
            Map.of("originalId", e.getId().toString())
        );
    }

    private void acknowledge(String recordId) {
        streamTemplate.opsForStream().acknowledge(EVENT_PARTICIPATION_KEY, CONSUMBER_GROUP_NAME, recordId);
    }
}
