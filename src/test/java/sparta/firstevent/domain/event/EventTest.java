package sparta.firstevent.domain.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sparta.firstevent.domain.member.Member;
import sparta.firstevent.domain.member.MemberFixture;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EventTest {

    Event event;
    Determinator determinator;

    @BeforeEach
    void setUp() {
        event = EventFixture.registEvent();

        determinator = EventFixture.determinatorToWinner();
    }

    @Test
    void regist() {

        assertThat(event.getTitle()).isEqualTo("title");
        assertEquals("title", event.getTitle());

    }

    @Test
    void modify() {

        event.update("title2", "desc", 10,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertThat(event.getTitle()).isEqualTo("title2");

    }

    @Test
    void modifyFail() {

        event.start();

        assertThatThrownBy(() ->event.update("title2", "desc", 10,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("시작된 이벤트는 수정할 수 없습니다.");
    }

    @Test
    void finish() {
        event.finish();

        assertThat(event.getStatus()).isEqualTo(EventStatus.FINISHED);
    }


    @Test
    @DisplayName("일정의 시작일시가 null 일때 테스트")
    void startAtNull() {

        assertThatThrownBy(() -> EventFixture.registEventNullStartAt())
            .isInstanceOf(NullPointerException.class);
        
    }

}