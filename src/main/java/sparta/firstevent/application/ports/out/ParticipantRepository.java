package sparta.firstevent.application.ports.out;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import sparta.firstevent.application.ports.out.projections.ParticipantProjection;
import sparta.firstevent.domain.event.Participant;

import java.util.List;

public interface ParticipantRepository extends Repository<Participant, Long> {
//    Long countByEventId(Long eventId);

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    long countByEventIdAndIsWinnerIsTrue(Long eventId);

    Participant save(Participant participant);

    long countByEventId(Long eventId);

    @Query(value = "select id, event_id from participant where event_id = :eventId", nativeQuery = true )
    List<ParticipantProjection> findAllByEventId(@Param("eventId") Long eventId, Pageable pageable);

    @Query(value = "select id, event_id from participant where event_id = :eventId", nativeQuery = true )
    List<Participant> findFirstByEventId(@Param("eventId") Long eventId, Pageable pageable);

    @Query(value = "select id, event_id from participant where event_id = :eventId and id < :cursor", nativeQuery = true )
    List<Participant> findAllByEventIdWithCursor(@Param("eventId") Long eventId, @Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT p FROM Participant p WHERE p.id IN :ids")
    List<Participant> findAllByIdIn(@Param("ids") List<Long> ids);
}
