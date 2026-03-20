package sparta.firstevent.adapter.stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import sparta.firstevent.adapter.dto.EventParticipationMessage;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventParticipationPublisher {

    private static final String EVENT_PARTICIPATION_KEY = "event:participation";
    private final RedisTemplate<String, String> streamTemplate;

    public String publishEventParticipation(Long eventId, Long memberId) {
        EventParticipationMessage message = EventParticipationMessage.of(eventId, memberId);

        MapRecord<String, String, String> record = StreamRecords
            .newRecord()
            .ofMap(toMap(message))
            .withStreamKey(EVENT_PARTICIPATION_KEY);

        var recordId = streamTemplate.opsForStream().add(record);

        assert recordId != null;

        log.info("Event participation published: recordId={}, eventId={}, memberId={}",
            recordId, eventId, memberId);

        saveParticipationStatus(recordId.toString());

        return recordId.toString();
    }

    private void saveParticipationStatus(String recordId) {
        String key = "event:participation:status:" + recordId;
        streamTemplate.opsForHash().putAll(key, Map.of(
            "status", "PENDING",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));

        // ttl 1시간
        streamTemplate.expire(key, Duration.ofHours(1));
    }

    private Map<String, String> toMap(EventParticipationMessage message) {
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Map<String, String> rawMap = objectMapper.convertValue(message, new TypeReference<Map<String, String>>() {});
        return rawMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> String.valueOf(entry.getValue())
            ));
    }
}
