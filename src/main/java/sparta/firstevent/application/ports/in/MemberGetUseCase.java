package sparta.firstevent.application.ports.in;

import sparta.firstevent.domain.member.Member;

public interface MemberGetUseCase {
    Member get(Long id);
}
