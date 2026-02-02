package sparta.firstevent.application.ports.in;

import sparta.firstevent.domain.event.Participant;

public interface ParticipantManageUseCase {
    Participant apply(Long eventId, Long memberId);
}
