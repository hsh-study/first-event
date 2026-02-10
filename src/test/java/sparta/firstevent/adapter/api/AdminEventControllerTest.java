package sparta.firstevent.adapter.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.application.ports.out.EventParticipantCountRepository;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.application.ports.out.MemberRepository;
import sparta.firstevent.application.ports.out.ParticipantRepository;
import sparta.firstevent.domain.event.*;
import sparta.firstevent.domain.member.Member;
import sparta.firstevent.domain.member.MemberFixture;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
    ParticipantRepository participantRepository;

    @Autowired
    EventParticipantCountRepository eventParticipantCountRepository;

    @Autowired
    Determinator determinator;

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
    void getParticipants() throws UnsupportedEncodingException, JsonProcessingException {
        Event event = eventRepository.save(EventFixture.registEvent());

        eventParticipantCountRepository.save(EventParticipantCount.regist(event.getId()));

        List<Member> members = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            members.add(memberRepository.save(MemberFixture.registMemberWithoutId("test"+i+"@firstevent.kr")));
        }

        for (Member member : members) {
            Long eventId = event.getId();
            Participant participant = participantRepository.save(Participant.regist(member.getId(), eventId, determinator));
            EventParticipantCount participantCount = eventParticipantCountRepository.findByEventId(eventId)
                .orElse(EventParticipantCount.regist(eventId));

            if (participant.isWinner()) {
                participantCount.updateWithWinner();
            } else {
                participantCount.update();
            }

            eventParticipantCountRepository.save(participantCount);
        }

        entityManager.flush();

        /*
        MvcTestResult result = mvcTester.post().uri("/api/members").contentType(MediaType.APPLICATION_JSON)
                .content(requestJson).exchange();

        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.memberId", notNull())
                .hasPathSatisfying("$.email", isEqualTo(request.email()));

        MemberRegisterResponse response =
                objectMapper.readValue(result.getResponse().getContentAsString(), MemberRegisterResponse.class);

                assertThat(response.memberId()).isNotNull();
         */

        MvcTestResult result = mockMvcTester.get().uri("/api/admin/events/{id}/participants", event.getId())
            .contentType(MediaType.APPLICATION_JSON).exchange();

        assertThat(result)
            .hasStatusOk()
            .apply(MockMvcResultHandlers.print())
            .bodyJson()
            .hasPathSatisfying("$.totalElements", value -> Assertions.assertThat(value).isEqualTo(10))
            .hasPathSatisfying("$.content", value -> Assertions.assertThat(value).isNotEmpty());
    }
    
}
