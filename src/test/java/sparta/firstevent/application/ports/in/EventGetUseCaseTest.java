package sparta.firstevent.application.ports.in;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;
import sparta.firstevent.domain.event.EventStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EventGetUseCaseTest {
    @Autowired
    EventManageUseCase eventManageUseCase;

    @Autowired
    AdminEventManageUseCase adminEventManageUseCase;

    @Autowired
    EventGetUseCase eventGetUseCase;
    
    @Test
    void page() {
        adminEventManageUseCase.regist(EventFixture.createEventRequestDto("title 1"));
        adminEventManageUseCase.regist(EventFixture.createEventRequestDto("title 2"));
        adminEventManageUseCase.regist(EventFixture.createEventRequestDto("title 3"));

        Pageable page = PageRequest.of(0, 2, Sort.by("id").descending());

        Page<Event> pagedEvents = eventGetUseCase.getAll(page);

        assertThat(pagedEvents).hasSize(2);
        assertThat(pagedEvents.getTotalElements()).isEqualTo(3);
        assertThat(pagedEvents.getContent().get(0).getTitle()).isEqualTo("title 3");
    }

    @Test
    void checkEventStatus() {
        Event savedEvent = adminEventManageUseCase.regist(EventFixture.createEventRequestDto());

        Event foundEvent = eventGetUseCase.getWithStatus(savedEvent.getId(), EventStatus.PENDING);

        assertThat(foundEvent.getStatus()).isEqualTo(EventStatus.PENDING);
        assertThat(foundEvent.getTitle()).isEqualTo(savedEvent.getTitle());
    }

}