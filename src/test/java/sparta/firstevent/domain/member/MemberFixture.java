package sparta.firstevent.domain.member;

import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {
    static Long userId = 1L;
    public static Member registMember() {
        Member member = registMember("test@firstevent.kr");
        ReflectionTestUtils.setField(member, "id", userId);
        return member;
    }

    public static Member registMember(String email) {
        Member member = Member.regist(email, "1234", "nickname", passwordEncoder());
        ReflectionTestUtils.setField(member, "id", ++userId);
        return member;
    }

    public static PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(String rawPassword) {
                return rawPassword + "secret";
            }

            @Override
            public boolean matches(String rawPassword, String encodedPassword) {
                return encode(rawPassword).equals(encodedPassword);
            }
        };
    }
}
