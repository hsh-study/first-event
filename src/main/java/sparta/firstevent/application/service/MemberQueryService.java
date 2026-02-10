package sparta.firstevent.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sparta.firstevent.application.ports.in.MemberGetUseCase;
import sparta.firstevent.application.ports.out.MemberRepository;
import sparta.firstevent.domain.member.Member;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberQueryService implements MemberGetUseCase {
    private final MemberRepository memberRepository;

    @Override
    public Member get(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("id에 해당하는 회원정보가 없습니다."));
    }

    @Override
    public List<Member> getAll() {
        return memberRepository.findAll();
    }
}
