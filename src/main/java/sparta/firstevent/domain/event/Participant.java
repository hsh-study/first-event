package sparta.firstevent.domain.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.firstevent.domain.member.Member;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Participant {
    private Member member;
    private Event event;
    private boolean isWinner;
    private LocalDateTime participateAt;

    public static Participant regist(Member member, Event event, Determinator determinator) {
        Participant participant = new Participant();

        participant.member = member;
        participant.event = event;
        participant.isWinner = determinator.determinate();
        participant.participateAt = LocalDateTime.now();

        return participant;

    }
}
