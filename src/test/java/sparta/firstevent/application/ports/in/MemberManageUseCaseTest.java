package sparta.firstevent.application.ports.in;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.application.ports.out.MemberRepository;
import sparta.firstevent.domain.member.Member;
import sparta.firstevent.domain.member.MemberFixture;
import sparta.firstevent.domain.member.MemberStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional(readOnly = true, rollbackFor = Exception.class)
class MemberManageUseCaseTest {

    @Autowired
    MemberManageUseCase memberManageUseCase;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void regist() {

        Member savedMember = memberManageUseCase.regist(MemberFixture.createMemberRequestDto());

        Member foundMember = memberRepository.findById(savedMember.getId()).orElseThrow();

        assertThat(foundMember.getId()).isNotNull();
        assertThat(foundMember.getEmail()).isEqualTo(savedMember.getEmail());

    }

    @Test
    void registFail() {
        Member savedMember = memberManageUseCase.regist(MemberFixture.createMemberRequestDto());

        assertThatThrownBy(() -> memberManageUseCase.regist(MemberFixture.createMemberRequestDto()))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void withdraw() {
        Member savedMember = memberManageUseCase.regist(MemberFixture.createMemberRequestDto());

        memberManageUseCase.withdraw(savedMember.getId());

        Member foundMember = memberRepository.findById(savedMember.getId()).orElseThrow();

        assertThat(foundMember.getStatus()).isEqualTo(MemberStatus.INACTIVE);

    }

}