package sparta.firstevent.adapter.dto;

import sparta.firstevent.domain.member.Member;

public record MemberResponseDto (
    Long id, String nickname
){

    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(member.getId(), member.getNickname());
    }
}
