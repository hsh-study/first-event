package sparta.firstevent.adapter.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.adapter.dto.EventResponseDto;
import sparta.firstevent.application.ports.in.AdminEventManageUseCase;
import sparta.firstevent.domain.event.Event;

@RequestMapping("/api/admin/events")
@RestController
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventManageUseCase adminEventManageUseCase;

    @PostMapping
    public EventResponseDto registEvent(@Valid @RequestBody EventRequestDto requestDto) {
        Event event = adminEventManageUseCase.regist(requestDto);
        return new EventResponseDto(event.getId(), event.getTitle(), event.getStatus());
    }
}
