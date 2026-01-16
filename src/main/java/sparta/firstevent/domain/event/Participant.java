package sparta.firstevent.domain.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.firstevent.domain.member.Member;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "uk_participant_event", columnNames = {"memberId", "eventId"})})
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, insertable = false, updatable = false)
    private Long eventId;

    @Column(nullable = false)
    private boolean isWinner;

    @Column(nullable = false)
    private LocalDateTime participateAt;

    public static Participant regist(Member member, Event event, Determinator determinator) {
        Participant participant = new Participant();

        participant.memberId = member.getId();
        participant.eventId = event.getId();
        participant.isWinner = determinator.determinate();
        participant.participateAt = LocalDateTime.now();

        return participant;

    }
}
