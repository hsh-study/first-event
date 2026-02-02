package sparta.firstevent.application.ports.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sparta.firstevent.domain.event.Participant;

public interface ParticipantGetUseCase {
    Long countWinner(Long eventId);
    boolean exists(Long eventId, Long memberId);
    Page<Participant> getAll(Long eventId, Pageable pageable);
}
