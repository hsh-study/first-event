package sparta.firstevent.adapter.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.EventRequestDto;
import sparta.firstevent.application.ports.in.AdminEventGetUseCase;
import sparta.firstevent.application.ports.out.EventRepository;
import sparta.firstevent.domain.event.EventFixture;

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
    AdminEventGetUseCase adminEventGetUseCase;

    @Test
    void registEvent() throws JsonProcessingException {
        EventRequestDto eventRequestDto = EventFixture.createEventRequestDto();

        assertThat(mockMvcTester.post().uri("/api/admin/events").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(eventRequestDto)).exchange()
        ).hasStatusOk();
    }
}
