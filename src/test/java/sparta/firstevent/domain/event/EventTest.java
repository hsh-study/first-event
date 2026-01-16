package sparta.firstevent.domain.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sparta.firstevent.domain.member.Member;
import sparta.firstevent.domain.member.MemberFixture;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

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
    void participate() {
        Member member = MemberFixture.registMember();

        event.start();
        event.participate(member, determinator);

        assertThat(event.getParticipants().size()).isEqualTo(1);
    }

    @Test
    void participateFail() {
        // 동일 회원이 중복 참여하는 경우
        Event event = EventFixture.registEventWithCapa(2);

        Member member = MemberFixture.registMember();

        event.start();
        event.participate(member, determinator);

        assertThatThrownBy(() -> event.participate(member, determinator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이벤트에는 중복 참여할 수 없습니다.");

        // 참여자수가 만족되어 이벤트가 종료되었는데 참여하는 경우
        Member member2 = MemberFixture.registMember("test2@firstevent.kr");

        Event event2 = EventFixture.registEventWithCapa(1);

        event2.start();
        event2.participate(member, determinator);

        assertThatThrownBy(() -> event2.participate(member2, determinator))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("당첨자가 초과하여 이벤트가 종료되었습니다.");
    }
    
    @Test
    @DisplayName("일정의 시작일시가 null 일때 테스트")
    void startAtNull() {

        assertThatThrownBy(() -> EventFixture.registEventNullStartAt())
            .isInstanceOf(NullPointerException.class);
        
    }

}