package sparta.firstevent.adapter.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import sparta.firstevent.adapter.api.docs.EventApi;
import sparta.firstevent.adapter.dto.ApiErrResponse;
import sparta.firstevent.adapter.dto.EventResponseDto;
import sparta.firstevent.adapter.dto.ParticipantResponseDto;
import sparta.firstevent.application.ports.in.EventGetUseCase;
import sparta.firstevent.application.ports.in.ParticipantManageUseCase;
import sparta.firstevent.domain.event.Event;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController implements EventApi {
    private final ParticipantManageUseCase participantManageUseCase;
    private final EventGetUseCase eventGetUseCase;

    @PostMapping("/{eventId}/participate/{memberId}")
    public ParticipantResponseDto participate(Long eventId, Long memberId) {
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
}
