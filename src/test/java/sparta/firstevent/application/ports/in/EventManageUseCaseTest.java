package sparta.firstevent.application.ports.in;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.adapter.dto.MemberRequestDto;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;
import sparta.firstevent.domain.member.Member;
import sparta.firstevent.domain.member.MemberFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class EventManageUseCaseTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    EventManageUseCase eventManageUseCase;

    @Autowired
    AdminEventManageUseCase adminEventManageUseCase;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    MemberManageUseCase memberManageUseCase;

    EventRequestDto eventRequest;
    MemberRequestDto memberRequest;
    @Autowired
    private EventGetUseCase eventGetUseCase;

    @BeforeEach
    void setUp() {
        eventRequest = EventFixture.createEventRequestDto();
        memberRequest = MemberFixture.createMemberRequestDto();
    }

    @Test
    void regist() {
        Event savedEvent = adminEventManageUseCase.regist(eventRequest);
        Long id = savedEvent.getId();

        assertThat(id).isNotNull();
        assertThat(savedEvent.getTitle()).isEqualTo(eventRequest.getTitle());
    }

    @Test
    void modify() {
        String title = "modified title";
        Event savedEvent = adminEventManageUseCase.regist(eventRequest);
        Long id = savedEvent.getId();

        entityManager.flush();
        entityManager.clear();

        Event updatedEvent = adminEventManageUseCase.update(id, EventFixture.createEventRequestDto(title));

        assertThat(updatedEvent.getTitle()).isEqualTo(title);
    }

    @Test
    void delete() {
        Event savedEvent = adminEventManageUseCase.regist(eventRequest);
        Long id = savedEvent.getId();

        assertThat(eventRepository.findById(id)).isNotEmpty();

        adminEventManageUseCase.delete(id);

        entityManager.flush();
        entityManager.clear();

        assertThat(eventRepository.findById(id)).isEmpty();
    }

    @Test
    void deleteFail() {
        Event savedEvent = adminEventManageUseCase.regist(eventRequest);
        Long id = savedEvent.getId();

        assertThat(eventRepository.findById(id)).isNotEmpty();

        savedEvent.start();

        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> adminEventManageUseCase.delete(id))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("진행중인 이벤트는 삭제할 수 없습니다.");
    }

    @Test
    void apply() {

        Member savedMember = memberManageUseCase.regist(memberRequest);
        Event savedEvent = adminEventManageUseCase.regist(eventRequest);
        entityManager.flush();
        entityManager.clear();

        Event startEvent = eventGetUseCase.get(savedEvent.getId());
        adminEventManageUseCase.start(startEvent.getId());
        entityManager.flush();
        entityManager.clear();

        Event participateEvent = eventGetUseCase.get(savedEvent.getId());
        eventManageUseCase.apply(participateEvent.getId(), savedMember.getId());

        assertThat(participateEvent.getParticipants().size()).isEqualTo(1);

        entityManager.flush();
        entityManager.clear();

        Event targetEvent = eventGetUseCase.get(savedEvent.getId());

        assertThat(targetEvent.getParticipants().size()).isEqualTo(1);
    }



//    @Test
//    void registStub() {
//
//        EventManageUseCase eventManageUseCase = new EventCommandService(new EventRepositoryStub());
//        EventRequestDto requestDto = new EventRequestDto(
//            "test title",
//            "test description",
//            10,
//            LocalDateTime.now(),
//            LocalDateTime.now().plusHours(1)
//        );
//
//        Event savedEvent = eventManageUseCase.regist(requestDto);
//        assertThat(savedEvent.getId()).isNotNull();
//
//    }
//
//    @Test
//    void regist() {
//        EventRepository repository = mock(EventRepository.class);
//
//        EventManageUseCase eventManageUseCase = new EventCommandService(repository);
//        EventRequestDto requestDto = new EventRequestDto(
//            "test title",
//            "test description",
//            10,
//            LocalDateTime.now(),
//            LocalDateTime.now().plusHours(1)
//        );
//
//        Event savedEvent = eventManageUseCase.regist(requestDto);
//
//        verify(repository).save(any());
//    }
//
//    static class EventRepositoryStub implements EventRepository {
//
//
//        @Override
//        public Event save(Event event) {
//            ReflectionTestUtils.setField(event, "id", 1L);
//            return event;
//        }
//    }

}