package sparta.firstevent.adapter.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sparta.firstevent.adapter.dto.MemberRequestDto;
import sparta.firstevent.adapter.dto.MemberResponseDto;
import sparta.firstevent.application.ports.in.MemberGetUseCase;
import sparta.firstevent.application.ports.in.MemberManageUseCase;
import sparta.firstevent.domain.member.Member;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberGetUseCase memberGetUseCase;
    private final MemberManageUseCase memberManageUseCase;

    @PostMapping
    public MemberResponseDto regist(@Valid @RequestBody MemberRequestDto requestDto) {
        Member member = memberManageUseCase.regist(requestDto);

        return MemberResponseDto.from(member);
    }
}
