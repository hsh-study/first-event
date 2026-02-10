package sparta.firstevent.application.ports.in;

import sparta.firstevent.domain.member.Member;

import java.util.List;

public interface MemberGetUseCase {
    Member get(Long id);

    List<Member> getAll();
}
