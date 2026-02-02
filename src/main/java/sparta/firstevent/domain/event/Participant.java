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

    @Column(name="event_id")
    private Long eventId;

    @Column(nullable = false)
    private boolean isWinner;

    @Column(nullable = false)
    private LocalDateTime participateAt;

    public static Participant regist(Long memberId, Long eventId, Determinator determinator) {
        Participant participant = new Participant();

        participant.memberId = memberId;
        participant.eventId = eventId;
        participant.isWinner = determinator.determinate();
        participant.participateAt = LocalDateTime.now();

        return participant;

    }
}
