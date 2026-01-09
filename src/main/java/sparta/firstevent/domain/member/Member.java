package sparta.firstevent.domain.member;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Member {
    private String email;
    private String password;
    private String nickname;
    private MemberStatus status;
    private MemberRole role;
    private LocalDateTime registAt;

    public Member(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.status = MemberStatus.ACTIVE;
        this.role = MemberRole.USER;
        registAt = LocalDateTime.now();
    }
}
