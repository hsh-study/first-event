package sparta.firstevent.application.ports.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import sparta.firstevent.domain.event.Participant;

public interface ParticipantRepository extends Repository<Participant, Long> {
//    Long countByEventId(Long eventId);

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    long countByEventIdAndIsWinnerIsTrue(Long eventId);

    Participant save(Participant participant);

    long countByEventId(Long eventId);

    // select * from participant where event_id = ? order by participate_at desc limit 5 offset 0
    Page<Participant> findAllBy(Long eventId, Pageable pageable);
}
