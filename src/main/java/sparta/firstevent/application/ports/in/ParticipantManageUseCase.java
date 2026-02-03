package sparta.firstevent.application.ports.in;

import sparta.firstevent.domain.event.Participant;

public interface ParticipantManageUseCase {
    Participant apply(Long eventId, Long memberId);

    Participant directApply(Long eventId, Long memberId);

    Participant applyWithPessimisticLock(Long eventId, Long memberId);

    Participant applyWithOptimisticLock(Long eventId, Long memberId);
}
