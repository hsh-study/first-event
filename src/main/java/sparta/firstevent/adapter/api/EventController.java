package sparta.firstevent.adapter.api;

import io.swagger.v3.oas.annotations.headers.Header;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sparta.firstevent.adapter.api.docs.EventApi;
import sparta.firstevent.adapter.dto.EventParticipantCountResponseDto;
import sparta.firstevent.adapter.dto.EventResponseDto;
import sparta.firstevent.adapter.dto.ParticipantResponseDto;
import sparta.firstevent.adapter.stream.EventParticipationPublisher;
import sparta.firstevent.application.ports.in.EventGetUseCase;
import sparta.firstevent.application.ports.in.ParticipantManageUseCase;
import sparta.firstevent.domain.event.Event;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController implements EventApi {
    private final ParticipantManageUseCase participantManageUseCase;
    private final EventGetUseCase eventGetUseCase;

    private final EventParticipationPublisher participationPublisher;

    @PostMapping("/{eventId}/participate/async/{memberId}")
    public ResponseEntity<Map<String, String>> participateAsync(
        @PathVariable Long eventId,
        @PathVariable Long memberId) {
        String recordId = participationPublisher.publishEventParticipation(eventId, memberId);
        return ResponseEntity.ok(Map.of(
            "recordId", recordId,
            "message", "참여 요청이 접수되었습니다."
            )
        );
    }

    @PostMapping("/{eventId}/participate/{memberId}")
    public ParticipantResponseDto participate(@PathVariable Long eventId, @PathVariable Long memberId) {
        return ParticipantResponseDto.from(participantManageUseCase.apply(eventId, memberId));
    }

    @GetMapping
    public Page<EventResponseDto> getAll(Pageable pageable) {
        Page<Event> events = eventGetUseCase.getAll(pageable);

        return events.map(e -> EventResponseDto.fromEntity(e));
    }

    @GetMapping("/{id}")
    public EventResponseDto getEvent(@PathVariable Long id) {
        return EventResponseDto.fromEntity(eventGetUseCase.get(id));
    }

    @GetMapping("/{id}/with-count")
    public EventParticipantCountResponseDto getEventWithCount(@PathVariable Long id) {
        return eventGetUseCase.getWithCount(id);
    }
}
