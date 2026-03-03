package sparta.firstevent.application.ports.in;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.application.ports.out.EventViewCounterRepository;
import sparta.firstevent.application.ports.out.EventViewRepository;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;
import sparta.firstevent.domain.event.EventStatus;
import sparta.firstevent.domain.event.EventView;
import sparta.firstevent.domain.member.Member;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EventGetUseCaseTest {
    @Autowired
    EventManageUseCase eventManageUseCase;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    EventViewCounterRepository eventViewCounterRepository;

    @Autowired
    EventViewRepository eventViewRepository;

    @Autowired
    AdminEventManageUseCase adminEventManageUseCase;

    @Autowired
    EventGetUseCase eventGetUseCase;
    
    @Test
    void page() {
        eventRepository.save(EventFixture.registEvent("title 1"));
        eventRepository.save(EventFixture.registEvent("title 2"));
        eventRepository.save(EventFixture.registEvent("title 3"));

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

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void getWithViewCount() throws InterruptedException {
        long start = System.currentTimeMillis();

        eventViewCounterRepository.deleteAll();
        Event event = eventRepository.save(EventFixture.registEvent("title 1"));

        int memberCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(50);
        CountDownLatch countDownLatch = new CountDownLatch(memberCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < memberCount; i++) {
            long memberId = 1000 + i;

            executorService.execute(() -> {
                try {
                    eventGetUseCase.getWithViewCount(event.getId(), memberId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }

            });
        }

        countDownLatch.await();
        executorService.shutdown();

        EventView eventView = eventViewRepository.findByEventId(event.getId()).orElseThrow();

        assertThat(eventView.getViewCount()).isEqualTo(memberCount);

        long end = System.currentTimeMillis();
        System.out.println("수행 시간 : " + (end - start));
    }

    @Test
    void viewDuplicateUser() {
        eventViewCounterRepository.deleteAll();

        Event event = eventRepository.save(EventFixture.registEvent("title 1"));
        eventGetUseCase.getWithViewCount(event.getId(), 1L);
        eventGetUseCase.getWithViewCount(event.getId(), 1L);

        Long count = eventViewCounterRepository.get(event.getId());

        assertThat(count).isEqualTo(1L);
    }
}