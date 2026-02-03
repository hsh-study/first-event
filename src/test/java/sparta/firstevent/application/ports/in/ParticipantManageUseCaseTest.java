package sparta.firstevent.application.ports.in;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.EventRequestDto;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ParticipantManageUseCaseTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EventRepository eventRepository;

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
    @Transactional
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

        EventParticipantCount count = eventParticipantCountRepository.findByEventId(savedEvent.getId()).orElseThrow();

        assertThat(participantRepository.countByEventId(participateEvent.getId())).isEqualTo(count.getParticipantCount());
    }

    @Test
    void apply_concurrency() throws InterruptedException {
        // Given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Event savedEvent = eventRepository.save(EventFixture.registEvent());
        Event startEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        startEvent.start();
        eventRepository.save(startEvent);

        eventParticipantCountRepository.save(EventParticipantCount.regist(savedEvent.getId()));

        // 1000명의 멤버 생성
        for (int i = 0; i < threadCount; i++) {
            Member member = memberRepository.save(MemberFixture.registMemberWithoutId("test" + i + "@firstevent.kr"));
            Long memberId = member.getId();

            executorService.execute(() -> {
                try {
                    participantManageUseCase.apply(savedEvent.getId(), memberId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 준비될 때까지 대기
        executorService.shutdown();

        // Then
        Event event = eventRepository.findById(savedEvent.getId()).orElseThrow();
        long participantCount = participantRepository.countByEventId(savedEvent.getId());

        System.out.println("실제 참여자 수: " + participantCount);
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());

        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);

        // EventParticipantCount가 생성되었을 경우에만 검증
        eventParticipantCountRepository.findByEventId(savedEvent.getId())
            .ifPresent(count -> {
                System.out.println("카운트 테이블 값: " + count.getParticipantCount());
                assertThat(count.getParticipantCount()).isEqualTo(participantCount)
                    .withFailMessage("동시성 문제 발생! 실제: %d, 카운트: %d", participantCount, count.getParticipantCount());
            });
    }

    @Test
    void directApply_concurrency() throws InterruptedException {
        // Given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Event savedEvent = eventRepository.save(EventFixture.registEvent());
        Event startEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        startEvent.start();
        eventRepository.save(startEvent);

        eventParticipantCountRepository.save(EventParticipantCount.regist(savedEvent.getId()));

        // 1000명의 멤버 생성
        for (int i = 0; i < threadCount; i++) {
            Member member = memberRepository.save(MemberFixture.registMemberWithoutId("test" + i + "@firstevent.kr"));
            Long memberId = member.getId();

            executorService.execute(() -> {
                try {
                    participantManageUseCase.directApply(savedEvent.getId(), memberId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then
        Event event = eventRepository.findById(savedEvent.getId()).orElseThrow();
        long participantCount = participantRepository.countByEventId(savedEvent.getId());

        System.out.println("실제 참여자 수: " + participantCount);
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());

        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);

        eventParticipantCountRepository.findByEventId(savedEvent.getId())
            .ifPresent(count -> {
                System.out.println("카운트 테이블 값: " + count.getParticipantCount());
                assertThat(count.getParticipantCount()).isEqualTo(participantCount)
                    .withFailMessage("동시성 문제 발생! 실제: %d, 카운트: %d", participantCount, count.getParticipantCount());
            });
    }

    @Test
    void applyWithPessimistic_concurrency() throws InterruptedException {
        // Given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Event savedEvent = eventRepository.save(EventFixture.registEvent());
        Event startEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        startEvent.start();
        eventRepository.save(startEvent);

        eventParticipantCountRepository.save(EventParticipantCount.regist(savedEvent.getId()));

        // 1000명의 멤버 생성
        for (int i = 0; i < threadCount; i++) {
            Member member = memberRepository.save(MemberFixture.registMemberWithoutId("test" + i + "@firstevent.kr"));
            Long memberId = member.getId();

            executorService.execute(() -> {
                try {
                    participantManageUseCase.applyWithPessimisticLock(savedEvent.getId(), memberId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 준비될 때까지 대기
        executorService.shutdown();

        // Then
        Event event = eventRepository.findById(savedEvent.getId()).orElseThrow();
        long participantCount = participantRepository.countByEventId(savedEvent.getId());

        System.out.println("실제 참여자 수: " + participantCount);
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());

        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);

        // EventParticipantCount가 생성되었을 경우에만 검증
        eventParticipantCountRepository.findByEventId(savedEvent.getId())
            .ifPresent(count -> {
                System.out.println("카운트 테이블 값: " + count.getParticipantCount());
                assertThat(count.getParticipantCount()).isEqualTo(participantCount)
                    .withFailMessage("동시성 문제 발생! 실제: %d, 카운트: %d", participantCount, count.getParticipantCount());
            });
    }

    @Test
    void applyWithOptimistic_concurrency() throws InterruptedException {
        // Given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Event savedEvent = eventRepository.save(EventFixture.registEvent());
        Event startEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        startEvent.start();
        eventRepository.save(startEvent);

        eventParticipantCountRepository.save(EventParticipantCount.regist(savedEvent.getId()));

        // 1000명의 멤버 생성
        for (int i = 0; i < threadCount; i++) {
            Member member = memberRepository.save(MemberFixture.registMemberWithoutId("test" + i + "@firstevent.kr"));
            Long memberId = member.getId();

            executorService.execute(() -> {
                try {
                    participantManageUseCase.applyWithOptimisticLock(savedEvent.getId(), memberId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 준비될 때까지 대기
        executorService.shutdown();

        // Then
        Event event = eventRepository.findById(savedEvent.getId()).orElseThrow();
        long participantCount = participantRepository.countByEventId(savedEvent.getId());

        System.out.println("실제 참여자 수: " + participantCount);
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());

        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);

        // EventParticipantCount가 생성되었을 경우에만 검증
        eventParticipantCountRepository.findByEventId(savedEvent.getId())
            .ifPresent(count -> {
                System.out.println("카운트 테이블 값: " + count.getParticipantCount());
                assertThat(count.getParticipantCount()).isEqualTo(participantCount)
                    .withFailMessage("동시성 문제 발생! 실제: %d, 카운트: %d", participantCount, count.getParticipantCount());
            });
    }
}