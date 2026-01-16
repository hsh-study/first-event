package sparta.firstevent.domain.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.firstevent.domain.member.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 600)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT") // clob, blob
    private String description;

    @Column(nullable = false)
    private Integer capacity;

    @Embedded
    private EventPeriod period;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Participant> participants = new ArrayList<>();

    public static Event regist(String title, String description, Integer capacity,
                         LocalDateTime startAt, LocalDateTime endAt) {

        Event event = new Event();
        event.title = title;
        event.description = description;
        event.capacity = capacity;

        event.period = EventPeriod.of(startAt, endAt);

        event.status = EventStatus.PENDING;

        return event;
    }

    public void start() {
        this.status = EventStatus.STARTED;
        this.period.start();
    }

    public void finish() {
        this.status = EventStatus.FINISHED;
    }

    public void update(String title, String description, Integer capacity,
                       LocalDateTime startAt, LocalDateTime endAt) {
        validToUpdate();

        this.title = title;
        this.description = description;
        this.capacity = capacity;
        this.period.update(startAt, endAt);
    }

    private void validToUpdate() {
        if (this.status == EventStatus.STARTED) {
            throw new IllegalStateException("시작된 이벤트는 수정할 수 없습니다.");
        }
    }

    public void participate(Member member, Determinator determinator) {
        validToParticipate(member);
        participants.add(Participant.regist(member, this, determinator));
    }

    private void validToParticipate(Member member) {

        if (this.status != EventStatus.STARTED) {
            throw new IllegalStateException("시작된 이벤트가 아니면 참여할 수 없습니다.");
        }

        int winnerCount = 0;
        for (Participant participant : participants) {
            if(participant.getMemberId().equals(member.getId())) {
                throw new IllegalArgumentException("이벤트에는 중복 참여할 수 없습니다.");
            }

            winnerCount += participant.isWinner() ? 1 : 0;
        }

        if (winnerCount >= capacity) {
            this.finish();
            throw new IllegalStateException("당첨자가 초과하여 이벤트가 종료되었습니다.");
        }
    }
}
