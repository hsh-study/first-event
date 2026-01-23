package sparta.firstevent.application.ports.in;

import sparta.firstevent.adapter.dto.MemberRequestDto;
import sparta.firstevent.domain.member.Member;

public interface MemberManageUseCase {
    Member regist(MemberRequestDto requestDto);
}
