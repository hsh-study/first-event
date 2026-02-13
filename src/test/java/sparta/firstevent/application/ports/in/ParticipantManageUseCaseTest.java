package sparta.firstevent.application.ports.in;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.adapter.dto.MemberRequestDto;
import sparta.firstevent.application.ports.out.EventParticipantCountRepository;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.application.ports.out.MemberRepository;
import sparta.firstevent.application.ports.out.ParticipantRepository;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;
import sparta.firstevent.domain.event.EventParticipantCount;
import sparta.firstevent.domain.member.Member;
import sparta.firstevent.domain.member.MemberFixture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void apply_concurrency() throws InterruptedException {

        int memberCount = 1000;

        List<Member> members = new ArrayList<>();
        for (int i = 0; i < memberCount; i++) {
            members.add(memberRepository.save(MemberFixture.registMemberWithoutId("test"+i+"@firstevent.kr")));
        }

        Event savedEvent = eventRepository.save(EventFixture.registEventWithCapa(200));
        savedEvent.start();
        eventRepository.save(savedEvent);
        Long savedEventId = savedEvent.getId();

        eventParticipantCountRepository.save(EventParticipantCount.regist(savedEventId));

        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        CountDownLatch countDownLatch = new CountDownLatch(memberCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < memberCount; i++) {
            Member member = members.get(i);
            Long memberId = member.getId();

            executorService.execute(() -> {
                try {
                    participantManageUseCase.apply(savedEventId, memberId);
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

        long participantCount = participantRepository.countByEventId(savedEvent.getId());

        EventParticipantCount eventParticipantCount = eventParticipantCountRepository.findByEventId(savedEventId).orElseThrow();
        System.out.println("실제 참여자 수: " + participantCount);
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());
        System.out.println("당첨자 수: " + eventParticipantCount.getWinnerCount());
        System.out.println("참여자 수: " + eventParticipantCount.getParticipantCount());

        assertThat(successCount.get() + failCount.get()).isEqualTo(memberCount);

        // race condition, concurrency

    }
}