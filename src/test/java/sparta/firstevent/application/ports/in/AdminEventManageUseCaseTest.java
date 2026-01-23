package sparta.firstevent.application.ports.in;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;
import sparta.firstevent.domain.event.EventStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class AdminEventManageUseCaseTest {

    @Autowired
    AdminEventManageUseCase adminEventManageUseCase;

    @Autowired
    EventManageUseCase eventManageUseCase;

    @Autowired
    AdminEventGetUseCase adminEventGetUseCase;

    @Autowired
    EntityManager entityManager;

    @Test
    void terminate() {
        Event event = eventManageUseCase.regist(EventFixture.createEventRequestDto());

        entityManager.flush();
        entityManager.clear();

        adminEventManageUseCase.terminate(event.getId());

        entityManager.flush();
        entityManager.clear();

        Event foundEvent = adminEventGetUseCase.get(event.getId());

        assertThat(foundEvent.getStatus()).isEqualTo(EventStatus.FINISHED);
    }

    @Test
    void terminateFail() {
        Event event = eventManageUseCase.regist(EventFixture.createEventRequestDto());
        adminEventManageUseCase.terminate(event.getId());

        entityManager.flush();
        entityManager.clear();

        Event foundEvent = adminEventGetUseCase.get(event.getId());

        assertThatThrownBy(() -> adminEventManageUseCase.terminate(foundEvent.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("종료된 이벤트는 강제 종료할 수 없습니다.");
    }
}
