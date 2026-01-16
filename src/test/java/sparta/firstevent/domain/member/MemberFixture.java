package sparta.firstevent.domain.member;

public class MemberFixture {
    public static Member registMember() {
        return registMember("test@firstevent.kr");
    }

    public static Member registMember(String email) {
        return Member.regist(email, "1234", "nickname", passwordEncoder());
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
