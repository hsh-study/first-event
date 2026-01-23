package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.MemberRequestDto;
import sparta.firstevent.application.ports.in.MemberGetUseCase;
import sparta.firstevent.application.ports.in.MemberManageUseCase;
import sparta.firstevent.application.ports.out.MemberRepository;
import sparta.firstevent.domain.member.Member;
import sparta.firstevent.domain.member.PasswordEncoder;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandService implements MemberManageUseCase {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberGetUseCase memberGetUseCase;

    @Override
    public Member regist(MemberRequestDto requestDto) {
        return memberRepository.save(Member.regist(requestDto, passwordEncoder));
    }

    @Override
    public Member withdraw(Long id) {
        Member member = memberGetUseCase.get(id);
        member.withdraw();
        return member;
    }
}
