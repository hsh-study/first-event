package sparta.firstevent.adapter.stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EventParticipationPublisherTest {

    @Autowired
    EventParticipationPublisher eventParticipationPublisher;

    @Autowired
    RedisTemplate<String, String> streamTemplate;

    @Test
    void publishEventParticipation() {
        // given
        Long eventId = 1L;
        Long memberId = 100L;

        // when
        String recordId = eventParticipationPublisher.publishEventParticipation(eventId, memberId);

        // then
        assertThat(recordId).isNotNull();

        Long size = streamTemplate.opsForStream().size("event:participation");
        assertThat(size).isGreaterThan(0L);
    }

}