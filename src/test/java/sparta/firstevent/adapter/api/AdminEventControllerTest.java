package sparta.firstevent.adapter.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.application.ports.in.AdminEventGetUseCase;
import sparta.firstevent.application.ports.in.ParticipantManageUseCase;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.application.ports.out.MemberRepository;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;
import sparta.firstevent.domain.member.Member;
import sparta.firstevent.domain.member.MemberFixture;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminEventControllerTest {

    @Autowired
    MockMvcTester mockMvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ParticipantManageUseCase participantManageUseCase;

    @Autowired
    AdminEventGetUseCase adminEventGetUseCase;

    @Autowired
    EntityManager entityManager;

    @Test
    void registEvent() throws JsonProcessingException {
        EventRequestDto eventRequestDto = EventFixture.createEventRequestDto();

        assertThat(mockMvcTester.post().uri("/api/admin/events").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(eventRequestDto)).exchange()
        ).hasStatusOk();
    }

    @Test
    void getParticipantsByCursor_첫_페이지_조회() {
        // given
        Event event = eventRepository.save(EventFixture.registEvent());
        entityManager.flush();

        Event startEvent = eventRepository.findById(event.getId()).orElseThrow();
        startEvent.start();
        eventRepository.save(startEvent);
        entityManager.flush();

        // 5개의 참가자 등록
        for (int i = 0; i < 5; i++) {
            Member m = memberRepository.save(MemberFixture.registMemberWithoutId("test"+i+"@firstevent.kr"));
            participantManageUseCase.apply(startEvent.getId(), m.getId());
        }
        entityManager.flush();

        // when
        MvcTestResult result = mockMvcTester.get()
                .uri("/api/admin/events/" + startEvent.getId() + "/participants/cursor?size=3")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.contents", contents -> assertThat(contents).asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(3))
                .hasPathSatisfying("$.nextCursor", cursor -> assertThat(cursor).isNotNull());
    }

    @Test
    void getParticipantsByCursor_커서로_다음_페이지_조회() throws UnsupportedEncodingException, JsonProcessingException {
        // given
        Event event = eventRepository.save(EventFixture.registEvent());
        entityManager.flush();

        Event startEvent = eventRepository.findById(event.getId()).orElseThrow();
        startEvent.start();
        eventRepository.save(startEvent);
        entityManager.flush();

        // 5개의 참가자 등록
        for (int i = 0; i < 5; i++) {
            Member m = memberRepository.save(MemberFixture.registMemberWithoutId("test"+i+"@firstevent.kr"));
            participantManageUseCase.apply(startEvent.getId(), m.getId());
        }
        entityManager.flush();

        // 첫 페이지 조회하여 커서 획득
        MvcTestResult firstPage = mockMvcTester.get()
                .uri("/api/admin/events/" + startEvent.getId() + "/participants/cursor?size=3")
                .exchange();

        String firstPageBody = firstPage.getResponse().getContentAsString();
        Long nextCursor = objectMapper.readTree(firstPageBody).get("nextCursor").asLong();

        // when - 커서를 이용한 두 번째 페이지 조회
        MvcTestResult secondPage = mockMvcTester.get()
                .uri("/api/admin/events/" + startEvent.getId() + "/participants/cursor?cursor=" + nextCursor + "&size=3")
                .exchange();

        // then
        assertThat(secondPage)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.contents", contents -> assertThat(contents).asList().hasSize(2))
                .hasPathSatisfying("$.nextCursor", cursor -> assertThat(cursor).isNull()); // 마지막 페이지
    }

    @Test
    void getParticipantsByCursor_참가자가_없을_때() {
        // given
        Event event = eventRepository.save(EventFixture.registEvent());
        entityManager.flush();

        Event startEvent = eventRepository.findById(event.getId()).orElseThrow();
        startEvent.start();
        eventRepository.save(startEvent);
        entityManager.flush();

        // when - 참가자가 없는 이벤트 조회
        MvcTestResult result = mockMvcTester.get()
                .uri("/api/admin/events/" + startEvent.getId() + "/participants/cursor?size=10")
                .exchange();

        // then
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.contents", contents -> assertThat(contents).asList().isEmpty())
                .hasPathSatisfying("$.nextCursor", cursor -> assertThat(cursor).isNull());
    }
}
