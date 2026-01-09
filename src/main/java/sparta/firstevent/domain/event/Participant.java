package sparta.firstevent.domain.event;

import lombok.Getter;
import sparta.firstevent.domain.member.Member;

import java.time.LocalDateTime;

@Getter
public class Participant {
    private Member member;
    private Event event;
    private LocalDateTime participateAt;

    public Participant(Member member, Event event) {
        this.member = member;
        this.event = event;
        participateAt = LocalDateTime.now();
    }
}
