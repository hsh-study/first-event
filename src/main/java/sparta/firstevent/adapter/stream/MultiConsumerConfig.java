package sparta.firstevent.adapter.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import sparta.firstevent.application.ports.in.ParticipantManageUseCase;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultiConsumerConfig {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private final RedisTemplate<String, String> streamTemplate;
    private final ParticipantManageUseCase participantManageUseCase;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void registMultipleConsumers() {
        for (int i = 1; i <= 3; i++) {
            String consumerName = "consumer-" + i;

            EventParticipationConsumer consumer = new EventParticipationConsumer(
                container, streamTemplate, participantManageUseCase, objectMapper);

            container.receive(
                Consumer.from("participation-processor", consumerName),
                StreamOffset.create("event:participation", ReadOffset.lastConsumed()),
                consumer
            );

            log.info("Registered consumer: {}", consumerName);
        }
    }
}
