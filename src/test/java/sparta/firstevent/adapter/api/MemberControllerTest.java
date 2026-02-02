package sparta.firstevent.adapter.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;
import sparta.firstevent.adapter.dto.MemberRequestDto;
import sparta.firstevent.adapter.dto.MemberResponseDto;
import sparta.firstevent.application.ports.in.MemberManageUseCase;
import sparta.firstevent.application.ports.out.MemberRepository;
import sparta.firstevent.domain.member.MemberFixture;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {
    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberManageUseCase memberManageUseCase;
    
    @Test
    void regist() throws JsonProcessingException, UnsupportedEncodingException {
        MemberRequestDto requestDto = MemberFixture.createMemberRequestDto();
        String json = objectMapper.writeValueAsString(requestDto);

        MvcTestResult result = mvcTester.post().uri("/api/members")
            .contentType(MediaType.APPLICATION_JSON).content(json).exchange();

        assertThat(result)
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.id", id -> assertThat(id).isNotNull())
            .hasPathSatisfying("$.nickname", responseNickname -> assertThat(responseNickname).isEqualTo(requestDto.nickname()));

        MemberResponseDto responseDto = objectMapper.readValue(result.getResponse().getContentAsString(), MemberResponseDto.class);

        assertThat(responseDto.id()).isNotNull();
        assertThat(responseDto.nickname()).isEqualTo(requestDto.nickname());

    }
}