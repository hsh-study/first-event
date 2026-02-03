package sparta.firstevent.application.ports.in;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.ParticipantCursorResponseDto;
import sparta.firstevent.application.ports.out.EventParticipantCountRepository;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.application.ports.out.MemberRepository;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;
import sparta.firstevent.domain.event.EventParticipantCount;
import sparta.firstevent.domain.member.Member;
import sparta.firstevent.domain.member.MemberFixture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ParticipantGetUseCaseTest {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventParticipantCountRepository eventParticipantCountRepository;

    @Autowired
    ParticipantManageUseCase participantManageUseCase;

    @Autowired
    ParticipantGetUseCase participantGetUseCase;

    @Autowired
    EntityManager entityManager;

    @Test
    void getAllByCursor() {
        int memberCount = 5;
        int size = 3;
        Event event = eventRepository.save(EventFixture.registEvent());
        event.start();
        eventRepository.save(event);

        entityManager.flush();
        entityManager.clear();


        for (int i = 0; i < memberCount; i++) {
            Member member = memberRepository.save(MemberFixture.registMemberWithoutId("test"+i+"@firstevent.kr"));
            participantManageUseCase.apply(event.getId(), member.getId());
        }

        entityManager.flush();
        entityManager.clear();

        EventParticipantCount eventParticipantCount = eventParticipantCountRepository.findByEventId(event.getId()).orElseThrow();
        assertThat(eventParticipantCount.getParticipantCount()).isEqualTo(memberCount);

        ParticipantCursorResponseDto firstResponseDto = participantGetUseCase.getAllByCursor(event.getId(), null, size);

        assertThat(firstResponseDto.nextCursor()).isNotNull();
        assertThat(firstResponseDto.contents()).hasSize(size);

        ParticipantCursorResponseDto nextResponseDto = participantGetUseCase.getAllByCursor(event.getId(), firstResponseDto.nextCursor(), size);
        assertThat(nextResponseDto.contents()).hasSize(2);
        assertThat(nextResponseDto.nextCursor()).isNull();

    }

}