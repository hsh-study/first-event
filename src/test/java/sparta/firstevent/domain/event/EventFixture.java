package sparta.firstevent.domain.event;

import sparta.firstevent.adapter.dto.EventRequestDto;

import java.time.LocalDateTime;

public class EventFixture {

    public static EventRequestDto createEventRequestDto() {
        return createEventRequestDto("title length is");
    }

    public static EventRequestDto createEventRequestDto(String title) {
        return new EventRequestDto(title, "desc", 10, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    }

    public static Event registEvent() {
        return registEventWithCapa(10);
    }

    public static Event registEventWithCapa(Integer capacity) {
        return Event.regist("title", "desc", capacity,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));
    }

    public static Event registEventNullStartAt() {
        return Event.regist("title", "desc", 10, null, LocalDateTime.now().plusDays(1));
    }

    public static Determinator determinatorToWinner() {
        return new Determinator() {
            @Override
            public boolean determinate() {
                return true;
            }
        };
    }

    public static Determinator determinatorToLoser() {
        return new Determinator() {
            @Override
            public boolean determinate() {
                return true;
            }
        };
    }
}
