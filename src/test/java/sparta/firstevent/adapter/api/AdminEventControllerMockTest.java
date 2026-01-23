package sparta.firstevent.adapter.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.application.ports.in.AdminEventGetUseCase;
import sparta.firstevent.application.ports.in.AdminEventManageUseCase;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.domain.event.Event;
import sparta.firstevent.domain.event.EventFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(AdminEventController.class)
class AdminEventControllerMockTest {

    @MockitoBean
    AdminEventManageUseCase adminEventManageUseCase;

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
        ).hasStatusOk().bodyJson().extractingPath("$.id").isEqualTo(event.getId());

        verify(adminEventManageUseCase).regist(any());
    }

}