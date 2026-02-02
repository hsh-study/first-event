package sparta.firstevent.adapter.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.adapter.dto.EventResponseDto;
import sparta.firstevent.adapter.dto.ParticipantResponseDto;
import sparta.firstevent.application.ports.in.AdminEventGetUseCase;
import sparta.firstevent.application.ports.in.AdminEventManageUseCase;
import sparta.firstevent.application.ports.in.ParticipantGetUseCase;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.Participant;

@RequestMapping("/api/admin/events")
@RestController
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventManageUseCase adminEventManageUseCase;
    private final AdminEventGetUseCase adminEventGetUseCase;
    private final ParticipantGetUseCase participantGetUseCase;

    @PostMapping
    public EventResponseDto registEvent(@Valid @RequestBody EventRequestDto requestDto) {
        Event event = adminEventManageUseCase.regist(requestDto);
        return new EventResponseDto(event.getId(), event.getTitle(), event.getStatus());
    }

    @GetMapping("/{eventId}/participants")
    public Page<ParticipantResponseDto> getParticipants(@PathVariable Long eventId, @PageableDefault Pageable pageable) {
        Page<Participant> participants = participantGetUseCase.getAll(eventId, pageable);

        return participants.map(ParticipantResponseDto::from);
    }

}
