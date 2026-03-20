package sparta.firstevent.adapter.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;
import sparta.firstevent.adapter.dto.EventParticipationMessage;
import sparta.firstevent.application.ports.in.ParticipantManageUseCase;
import sparta.firstevent.domain.event.Participant;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventParticipationConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private static final String EVENT_PARTICIPATION_KEY = "event:participation";
    private static final String CONSUMBER_GROUP_NAME = "participation-processor";
    private static final String CONSUMER_NAME_PREFIX = "consumer-";

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private final RedisTemplate<String, String> streamTemplate;
    private final ParticipantManageUseCase participantManageUseCase;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        createConsumerGroupIfNotExists();
        registerConsumer();
        container.start();
        log.info("Event participation consumer started: groupName={}, consumerNamePrefix={}", CONSUMBER_GROUP_NAME, CONSUMER_NAME_PREFIX);
    }

    private void createConsumerGroupIfNotExists() {
        try {
            streamTemplate.opsForStream().createGroup(EVENT_PARTICIPATION_KEY, CONSUMBER_GROUP_NAME);
            log.info("Created consumer group: {}", CONSUMBER_GROUP_NAME);
        } catch (Exception e) {
            log.info("Consumer group already exists: {}", CONSUMBER_GROUP_NAME);
        }
    }

    private void registerConsumer() {
        String consumerName = CONSUMER_NAME_PREFIX + UUID.randomUUID().toString().substring(0, 8);

        container.receive(
            Consumer.from(CONSUMBER_GROUP_NAME, consumerName),
            StreamOffset.create(EVENT_PARTICIPATION_KEY, ReadOffset.lastConsumed()),
            this
        );
    }

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        String recordId = record.getId().toString();

        try {
            EventParticipationMessage message = parseMessage(record.getValue());

            log.info("Received event participation message: recordId={}, eventId={}, memberId={}",
                recordId, message.getEventId(), message.getMemberId());

            updateParticipationStatus(recordId, "PROCESSING");

            Participant participant = participantManageUseCase.applyWithRecordLock(
                message.getEventId(),
                message.getMemberId());

            updateParticipationStatus(recordId, "SUCCESS", Map.of(
                "participantId", participant.getId(),
                "isWinner", participant.isWinner()
            ));

            streamTemplate.opsForStream().acknowledge(
                EVENT_PARTICIPATION_KEY,
                CONSUMBER_GROUP_NAME,
                record.getId());

            log.info("Success processing event participation message: recordId={}", recordId);
        } catch (Exception e) {
            log.error("Error processing event participation message: recordId={}, error={}", recordId, e.getMessage());
            updateParticipationStatus(recordId, "FAILED",
                Map.of("error", e.getMessage()));
        }

    }

    private EventParticipationMessage parseMessage(Map<String, String> value) {
        return objectMapper.convertValue(value, EventParticipationMessage.class);
    }

    private void updateParticipationStatus(String recordId, String status) {
        updateParticipationStatus(recordId, status, Map.of());
    }

    private void updateParticipationStatus(String recordId, String status, Map<String, Object> fields) {
        String key = "event:participation:status:" + recordId;
        Map<String, Object> statusMap = new HashMap<>(Map.of(
            "status", status,
            "updateAt", String.valueOf(System.currentTimeMillis())
        ));
        statusMap.putAll(fields);

        streamTemplate.opsForHash().putAll(key, statusMap);
    }

    @PreDestroy
    public void destroy() {
        if (container != null) {
            container.stop();
        }
        log.info("Event participation consumer stopped");
    }
}
