package sparta.firstevent.adapter.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import sparta.firstevent.adapter.dto.CursorPage;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.application.ports.in.AdminEventGetUseCase;
import sparta.firstevent.application.ports.in.AdminEventManageUseCase;
import sparta.firstevent.application.ports.in.ParticipantGetUseCase;
import sparta.firstevent.domain.event.Determinator;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;
import sparta.firstevent.domain.event.Participant;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@WebMvcTest(AdminEventController.class)
class AdminEventControllerMockTest {

    @MockitoBean
    AdminEventManageUseCase adminEventManageUseCase;

    @MockitoBean
    AdminEventGetUseCase adminEventGetUseCase;

    @MockitoBean
    ParticipantGetUseCase participantGetUseCase;

    @Autowired
    MockMvcTester mockMvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void registEvent() throws JsonProcessingException {

        // given
        Event event = EventFixture.registEvent();
        ReflectionTestUtils.setField(event, "id", 1L);

        EventRequestDto eventRequestDto = EventFixture.createEventRequestDto();
        when(adminEventManageUseCase.regist(any())).thenReturn(event);

        // when , then
        assertThat(mockMvcTester.post().uri("/api/admin/events").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(eventRequestDto)).exchange()
        ).hasStatusOk().bodyJson().extractingPath("$.id").isNotNull();

        verify(adminEventManageUseCase).regist(any());
    }

    @Test
    void getParticipants() {
        // given
        Long eventId = 1L;
        Participant participant1 = createParticipant(1L, 100L, eventId);
        Participant participant2 = createParticipant(2L, 200L, eventId);
        
        Page<Participant> participantPage = new PageImpl<>(
            List.of(participant1, participant2),
            PageRequest.of(0, 20),
            2
        );
        
        when(participantGetUseCase.getAll(anyLong(), any())).thenReturn(participantPage);

        // when & then
        assertThat(mockMvcTester.get().uri("/api/admin/events/1/participants?page=0&size=20"
            , eventId).exchange())
            .hasStatusOk()
            .bodyJson()
            .extractingPath("$.content").asArray().hasSize(2);
        
        verify(participantGetUseCase).getAll(anyLong(), any());
    }

    @Test
    void getParticipantsWithCursor() {
        Long eventId = 1L;
        Participant participant1 = createParticipant(1L, 100L, eventId);
        Participant participant2 = createParticipant(2L, 200L, eventId);
        Participant participant3 = createParticipant(3L, 300L, eventId);
        List<Participant> participants = List.of(participant1, participant2);

        CursorPage<Participant> participantPage = new CursorPage<>(participants, 3L, 3, false);

        given(participantGetUseCase.getAllByCursor(anyLong(), any(), anyInt())).willReturn(participantPage);

        assertThat(mockMvcTester.get().uri("/api/admin/events/1/participants/cursor?size=2"
            , eventId).exchange())
            .hasStatusOk()
            .apply(MockMvcResultHandlers.print())
            .bodyJson()
            .extractingPath("$.totalElements").asNumber().isEqualTo(3);

        verify(participantGetUseCase, times(1)).getAllByCursor(anyLong(), any(), anyInt());
    }

    private Participant createParticipant(Long id, Long memberId, Long eventId) {
        Participant participant = Participant.regist(memberId, eventId, determinator());
        ReflectionTestUtils.setField(participant, "id", id);
        ReflectionTestUtils.setField(participant, "participateAt", LocalDateTime.now());
        return participant;
    }

    private Determinator determinator() {
        return new Determinator() {
            @Override
            public boolean determinate() {
                return true;
            }
        };
    }

}