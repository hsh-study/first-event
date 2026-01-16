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

    private Member(String email, String password, String nickname, PasswordEncoder passwordEncoder) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.status = MemberStatus.ACTIVE;
        this.role = MemberRole.USER;
        registAt = LocalDateTime.now();
    }

    public static Member regist(String email, String password, String nickname, PasswordEncoder passwordEncoder) {
        return new Member(email, password, nickname, passwordEncoder);
    }

    public void withdraw() {
        this.status = MemberStatus.INACTIVE;
    }
}
