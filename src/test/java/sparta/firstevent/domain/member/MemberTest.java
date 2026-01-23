package sparta.firstevent.domain.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {
    Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.registMember();
    }
    
    @Test
    void regist() {
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void withdraw() {
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        member.withdraw();

        assertThat(member.getStatus()).isEqualTo(MemberStatus.INACTIVE);
    }

}