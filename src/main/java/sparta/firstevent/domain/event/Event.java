package sparta.firstevent.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sparta.firstevent.domain.member.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Event {
    private String title;
    private String description;
    private Integer capacity;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private EventStatus status;

    private List<Participant> participants = new ArrayList<>();

    public static Event regist(String title, String description, Integer capacity,
                         LocalDateTime startAt, LocalDateTime endAt) {

        Event event = new Event();
        event.title = title;
        event.description = description;
        event.capacity = capacity;
        event.startAt = startAt;
        event.endAt = endAt;
        event.status = EventStatus.PENDING;

        return event;
    }

    public void start() {
        this.status = EventStatus.STARTED;
        this.startAt = LocalDateTime.now();
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
        this.startAt = startAt;
        this.endAt = endAt;
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
            if(participant.getMember().getEmail().equals(member.getEmail())) {
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
