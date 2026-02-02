package sparta.firstevent.adapter.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.firstevent.adapter.dto.ParticipantResponseDto;
import sparta.firstevent.application.ports.in.ParticipantManageUseCase;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {
    private final ParticipantManageUseCase participantManageUseCase;

    @PostMapping("/{eventId}/participate/{memberId}")
    public ParticipantResponseDto participate(Long eventId, Long memberId) {
        return ParticipantResponseDto.from(participantManageUseCase.apply(eventId, memberId));
    }
}
