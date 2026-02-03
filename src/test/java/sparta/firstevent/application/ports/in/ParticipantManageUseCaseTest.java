package sparta.firstevent.application.ports.in;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.adapter.dto.MemberRequestDto;
import sparta.firstevent.application.ports.out.EventParticipantCountRepository;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.application.ports.out.MemberRepository;
import sparta.firstevent.application.ports.out.ParticipantRepository;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;
import sparta.firstevent.domain.member.Member;
import sparta.firstevent.domain.member.MemberFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ParticipantManageUseCaseTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventGetUseCase eventGetUseCase;

    @Autowired
    ParticipantManageUseCase participantManageUseCase;

    @Autowired
    ParticipantRepository participantRepository;

    @Autowired
    EventParticipantCountRepository eventParticipantCountRepository;

    @Autowired
    EntityManager entityManager;

    EventRequestDto eventRequest;
    MemberRequestDto memberRequest;

    @BeforeEach
    void setUp() {
        eventRequest = EventFixture.createEventRequestDto();
        memberRequest = MemberFixture.createMemberRequestDto();
    }

    @Test
    void apply() {

        Member savedMember = memberRepository.save(MemberFixture.registMemberWithoutId());
        Event savedEvent = eventRepository.save(EventFixture.registEvent());

        Long savedMemberId = savedMember.getId();

        entityManager.flush();
        entityManager.clear();

        Event startEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        startEvent.start();

        eventRepository.save(startEvent);
        entityManager.flush();
        entityManager.clear();

        Event participateEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        participantManageUseCase.apply(participateEvent.getId(), savedMemberId);

        assertThat(participantRepository.countByEventId(participateEvent.getId())).isEqualTo(1);
        assertThat(eventParticipantCountRepository.findByEventId(participateEvent.getId()).get().getParticipantCount()).isEqualTo(1);
    }
}