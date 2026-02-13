package sparta.firstevent.adapter.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    @DeleteMapping("/{id}")
    public void withdraw(@PathVariable Long id) {
        memberManageUseCase.withdraw(id);
    }
}
