package sparta.firstevent.application.ports.in;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.application.ports.out.MemberRepository;
import sparta.firstevent.domain.member.MemberFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberGetUseCaseTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberGetUseCase memberGetUseCase;

    @Autowired
    EntityManager entityManager;

    @Test
    void getAll() {
        for (int i = 0; i < 10; i++) {
            memberRepository.save(MemberFixture.registMemberWithoutId("test"+i+"@firstevent.kr"));
        }

        entityManager.flush();
        entityManager.clear();

        assertThat(memberGetUseCase.getAll()).hasSize(10);

    }
}