package sparta.firstevent.application.ports.in;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.adapter.dto.MemberRequestDto;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;
import sparta.firstevent.domain.event.Participant;
import sparta.firstevent.domain.member.Member;
import sparta.firstevent.domain.member.MemberFixture;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AdminEventGetUseCaseTest {

    @Autowired
    AdminEventGetUseCase adminEventGetUseCase;

    @Autowired
    EventManageUseCase eventManageUseCase;

    @Autowired
    AdminEventManageUseCase adminEventManageUseCase;

    @Autowired
    MemberManageUseCase memberManageUseCase;

    @Autowired
    EntityManager entityManager;

    @Test
    void page() {

        // given
        adminEventManageUseCase.regist(EventFixture.createEventRequestDto("title 1"));
        adminEventManageUseCase.regist(EventFixture.createEventRequestDto("title 2"));
        adminEventManageUseCase.regist(EventFixture.createEventRequestDto("title 3"));

        entityManager.flush();
        entityManager.clear();


        // when
        Pageable page = PageRequest.of(0, 2, Sort.by("id").descending());
        Page<Event> pagedEvents = adminEventGetUseCase.getAll(page);


        // then
        assertThat(pagedEvents).hasSize(2);
        assertThat(pagedEvents.getTotalElements()).isEqualTo(3);
        assertThat(pagedEvents.getContent().get(0).getTitle()).isEqualTo("title 3");
    }
    
    @Test
    void getParticipants() {

        // given
        MemberRequestDto memberRequest = MemberFixture.createMemberRequestDto();
        EventRequestDto eventRequest = EventFixture.createEventRequestDto();
        Member savedMember = memberManageUseCase.regist(memberRequest);
        Event savedEvent = adminEventManageUseCase.regist(eventRequest);

        savedEvent.start();

        // when
        savedEvent.participate(savedMember, EventFixture.determinatorToWinner());

        assertThat(savedEvent.getParticipants().size()).isEqualTo(1);

        entityManager.flush();
        entityManager.clear();

        // then
        List<Participant> participants = adminEventGetUseCase.getParticipants(savedEvent.getId());

        assertThat(participants.size()).isEqualTo(1);
    }

}